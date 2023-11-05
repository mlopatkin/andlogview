/*
 * Copyright 2015 Mikhail Lopatkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package name.mlopatkin.andlogview.filters;

import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.Preference;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.search.RequestCompilationException;
import name.mlopatkin.andlogview.ui.filterdialog.FilterDialogFactory;
import name.mlopatkin.andlogview.ui.filterdialog.FilterDialogHandle;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filterpanel.FilterCreator;
import name.mlopatkin.andlogview.ui.filterpanel.FilterPanelModel;
import name.mlopatkin.andlogview.ui.filterpanel.PanelFilter;
import name.mlopatkin.andlogview.ui.indexfilter.IndexFilterCollection;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.ui.mainframe.popupmenu.MenuFilterCreator;
import name.mlopatkin.andlogview.utils.Threads;

import com.google.common.annotations.VisibleForTesting;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;

/**
 * The filter controller of the main window. It knows about all existing filters and how they should be persisted,
 * toggled and applied.
 */
@MainFrameScoped
public class MainFilterController implements FilterCreator, MenuFilterCreator {
    // TODO separate "A filter for main table" and "bridge between all filters and panel"

    private static final Logger logger = Logger.getLogger(MainFilterController.class);

    private final FilterPanelModel filterPanelModel;
    private final FilterDialogFactory dialogFactory;
    private final IndexFilterCollection indexFilterCollection;
    private final Preference<List<SavedFilterData>> savedFiltersPref;
    private final LogModelFilterImpl filter;

    private final List<BaseToggleFilter<?>> filters = new ArrayList<>();

    @Inject
    MainFilterController(FilterPanelModel filterPanelModel, IndexFilterCollection indexFilterCollection,
            FilterDialogFactory dialogFactory, ConfigStorage storage, LogModelFilterImpl logModelFilter) {
        this(filterPanelModel, indexFilterCollection, dialogFactory, storage.preference(new FilterListSerializer()),
                logModelFilter);
    }

    @VisibleForTesting
    MainFilterController(FilterPanelModel filterPanelModel, IndexFilterCollection indexFilterCollection,
            FilterDialogFactory dialogFactory, Preference<List<SavedFilterData>> savedFiltersPref,
            LogModelFilterImpl logModelFilter) {
        this.filterPanelModel = filterPanelModel;
        this.dialogFactory = dialogFactory;
        this.indexFilterCollection = indexFilterCollection;
        this.savedFiltersPref = savedFiltersPref;
        this.filter = logModelFilter;

        indexFilterCollection.asObservable().addObserver(disabledFilter -> {
            for (BaseToggleFilter<?> registeredFilter : filters) {
                if (Objects.equals(registeredFilter.filter, disabledFilter)) {
                    filterPanelModel.setFilterEnabled(registeredFilter, false);
                }
            }
        });

        for (SavedFilterData savedFilterData : savedFiltersPref.get()) {
            savedFilterData.appendMe(this);
        }
    }

    public void setBufferEnabled(LogRecord.Buffer buffer, boolean enabled) {
        filter.bufferFilter.setBufferEnabled(buffer, enabled);
        notifyFiltersChanged();
    }

    @Override
    public void createFilterWithDialog() {
        dialogFactory.startCreateFilterDialog()
                .thenAccept(newFilter -> newFilter.ifPresent(this::addNewDialogFilter))
                .exceptionally(Threads::uncaughtException);
    }

    private DialogPanelFilter createDialogPanelFilter(FilterFromDialog filter) {
        return new DialogPanelFilter(getFilterCollectionForFilter(filter), filter);
    }

    private DialogPanelFilter addNewDialogFilter(FilterFromDialog filter) {
        DialogPanelFilter dialogPanelFilter = createDialogPanelFilter(filter);
        filters.add(dialogPanelFilter);
        filterPanelModel.addFilter(dialogPanelFilter.addToCollection());
        notifyFiltersChanged();
        return dialogPanelFilter;
    }

    private void notifyFiltersChanged() {
        filter.notifyObservers();
        ArrayList<SavedFilterData> serializedFilters = new ArrayList<>(filters.size());
        for (BaseToggleFilter<?> filter : filters) {
            serializedFilters.add(filter.getSerializedVersion());
        }
        savedFiltersPref.set(serializedFilters);
    }

    private FilterCollection<? super FilterFromDialog> getFilterCollectionForFilter(FilterFromDialog filter) {
        return switch (filter.getMode()) {
            case SHOW, HIDE -> this.filter.filterChain;
            case HIGHLIGHT -> this.filter.highlighter;
            case WINDOW -> indexFilterCollection;
        };
    }

    @Override
    public void addFilter(FilterFromDialog filter) {
        addNewDialogFilter(filter);
    }

    @Override
    public void createFilterWithDialog(FilterFromDialog baseData) {
        dialogFactory.startCreateFilterDialogWithInitialData(baseData)
                .thenAccept(result -> result.ifPresent(this::addFilter))
                .exceptionally(Threads::uncaughtException);
    }

    /**
     * Base class for {@link PanelFilter} wrappers for the DialogFilters and others.
     */
    private abstract class BaseToggleFilter<T extends Predicate<LogRecord>> implements PanelFilter {
        protected final FilteringMode mode;
        protected final FilterCollection<? super T> collection;
        protected final T filter;

        private boolean isEnabled = true;

        protected BaseToggleFilter(FilterCollection<? super T> collection, FilteringMode mode, T filter) {
            this.collection = collection;
            this.mode = mode;
            this.filter = filter;
        }

        @Override
        public boolean isEnabled() {
            return isEnabled;
        }

        @Override
        public void setEnabled(boolean isEnabled) {
            if (isEnabled != this.isEnabled) {
                this.isEnabled = isEnabled;
                collection.setFilterEnabled(mode, filter, isEnabled);
                notifyFiltersChanged();
            }
        }

        @Override
        public void delete() {
            collection.removeFilter(mode, filter);
            filters.remove(this);
            notifyFiltersChanged();
        }

        protected void replaceMeWith(BaseToggleFilter<T> replacement) {
            replacement.isEnabled = isEnabled;
            int myPos = filters.indexOf(this);
            if (myPos == -1) {
                // Ignore edit result if |this| filter isn't alive anymore. This can happen if the editor was opened
                // twice for the same filter. If both edits complete successfully then second one won't find 'this'
                // here, because it was replaced with the result of the first edit. Not so much can be done in this
                // case. Prior to 0.19 the result of the second edit was added as a new filter, 0.19 just crashes.
                // TODO(mlopatkin) proper solution is to disallow opening a second editor but this is much more involved
                //  fix which I don't like to push within 0.20 timeframe.
                return;
            }
            filters.set(myPos, replacement);
            filterPanelModel.replaceFilter(this, replacement);
            if (collection.equals(replacement.collection) && mode == replacement.mode) {
                collection.replaceFilter(mode, filter, replacement.filter);
                collection.setFilterEnabled(mode, replacement.filter, replacement.isEnabled);
            } else {
                collection.removeFilter(mode, filter);
                replacement.addToCollection();
            }
            notifyFiltersChanged();
        }

        public BaseToggleFilter<T> addToCollection() {
            collection.addFilter(mode, filter);
            collection.setFilterEnabled(mode, filter, isEnabled);
            return this;
        }

        public abstract SavedFilterData getSerializedVersion();
    }

    private class DialogPanelFilter extends BaseToggleFilter<FilterFromDialog> {
        private @Nullable FilterDialogHandle dialogHandle;

        protected DialogPanelFilter(FilterCollection<? super FilterFromDialog> collection, FilterFromDialog filter) {
            super(collection, filter.getMode(), filter);
        }

        @Override
        public void openFilterEditor() {
            FilterDialogHandle currentDialogHandle = dialogHandle;
            if (currentDialogHandle != null) {
                currentDialogHandle.bringToFront();
                return;
            }
            dialogHandle = currentDialogHandle = dialogFactory.startEditFilterDialog(this.filter);
            currentDialogHandle.getResult().thenAccept(newFilter -> {
                dialogHandle = null;
                if (newFilter.isPresent()) {
                    DialogPanelFilter newPanelFilter = createDialogPanelFilter(newFilter.get());
                    replaceMeWith(newPanelFilter);
                }
            }).exceptionally(Threads::uncaughtException);
        }

        @Override
        public String getTooltip() {
            return filter.getTooltip();
        }

        @Override
        public SavedFilterData getSerializedVersion() {
            return new SavedDialogFilterData(filter, isEnabled());
        }
    }

    abstract static class SavedFilterData {
        protected final boolean enabled;

        protected SavedFilterData(boolean enabled) {
            this.enabled = enabled;
        }

        abstract void appendMe(MainFilterController filterController);
    }

    static class SavedDialogFilterData extends SavedFilterData {
        private final FilterFromDialog filterData;

        SavedDialogFilterData(FilterFromDialog filterData, boolean enabled) {
            super(enabled);
            this.filterData = filterData;
        }

        @Override
        void appendMe(MainFilterController filterController) {
            try {
                filterData.initialize();
                // TODO(mlopatkin) This is somewhat dangerous because filterController is still in its constructor
                filterController.addNewDialogFilter(filterData).setEnabled(enabled);
            } catch (RequestCompilationException e) {
                logger.error("Invalid filter data", e);
            }
        }
    }
}
