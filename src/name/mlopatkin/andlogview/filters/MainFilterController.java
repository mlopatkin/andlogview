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
import name.mlopatkin.andlogview.utils.MyFutures;

import com.google.common.annotations.VisibleForTesting;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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
    private final Preference<List<SavedFilterData>> savedFiltersPref;
    private final LogModelFilterImpl filter;

    private final List<BaseToggleFilter<?>> filters = new ArrayList<>();
    private final FilterModel filterModel;

    @Inject
    MainFilterController(FilterPanelModel filterPanelModel, IndexFilterCollection indexFilterCollection,
            FilterDialogFactory dialogFactory, ConfigStorage storage, LogModelFilterImpl logModelFilter,
            FilterModel filterModel) {
        this(filterPanelModel, indexFilterCollection, dialogFactory, storage.preference(new FilterListSerializer()),
                logModelFilter, filterModel);
    }

    @VisibleForTesting
    MainFilterController(FilterPanelModel filterPanelModel, IndexFilterCollection indexFilterCollection,
            FilterDialogFactory dialogFactory, Preference<List<SavedFilterData>> savedFiltersPref,
            LogModelFilterImpl logModelFilter, FilterModel filterModel) {
        this.filterPanelModel = filterPanelModel;
        this.dialogFactory = dialogFactory;
        this.savedFiltersPref = savedFiltersPref;
        this.filter = logModelFilter;
        this.filterModel = filterModel;

        indexFilterCollection.asObservable().addObserver(disabledFilter -> {
            for (BaseToggleFilter<?> registeredFilter : filters) {
                if (Objects.equals(registeredFilter.filter, disabledFilter)) {
                    registeredFilter.setEnabled(false);
                }
            }
        });

        filterModel.asObservable().addObserver(indexFilterCollection.createObserver(Function.identity()));

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
                .exceptionally(MyFutures::uncaughtException);
    }

    private DialogPanelFilter createDialogPanelFilter(FilterFromDialog filter) {
        return new DialogPanelFilter(filter);
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

    @Override
    public void addFilter(FilterFromDialog filter) {
        addNewDialogFilter(filter);
    }

    @Override
    public void createFilterWithDialog(FilterFromDialog baseData) {
        dialogFactory.startCreateFilterDialogWithInitialData(baseData)
                .thenAccept(result -> result.ifPresent(this::addFilter))
                .exceptionally(MyFutures::uncaughtException);
    }

    /**
     * Base class for {@link PanelFilter} wrappers for the DialogFilters and others.
     */
    private abstract class BaseToggleFilter<T extends Filter> implements PanelFilter {
        protected final T filter;

        protected BaseToggleFilter(T filter) {
            this.filter = filter;
        }

        @Override
        public boolean isEnabled() {
            return filter.isEnabled();
        }

        @Override
        public void delete() {
            filterModel.removeFilter(filter);
            filters.remove(this);
            notifyFiltersChanged();
        }

        protected void replaceMeWith(BaseToggleFilter<T> replacement) {
            int myPos = filters.indexOf(this);
            if (myPos == -1) {
                // Ignore edit result if |this| filter isn't alive anymore. This can happen if the editor was opened
                // twice for the same filter or if the filter was deleted while editor was open.
                return;
            }
            filters.set(myPos, replacement);
            filterPanelModel.replaceFilter(this, replacement);
            filterModel.replaceFilter(filter, replacement.filter);
            notifyFiltersChanged();
        }

        public BaseToggleFilter<T> addToCollection() {
            filterModel.addFilter(filter);
            return this;
        }

        public abstract SavedFilterData getSerializedVersion();
    }

    private class DialogPanelFilter extends BaseToggleFilter<FilterFromDialog> {
        private @Nullable FilterDialogHandle dialogHandle;

        protected DialogPanelFilter(FilterFromDialog filter) {
            super(filter);
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (filter.isEnabled() != enabled) {
                replaceMeWith(new DialogPanelFilter(enabled ? filter.enabled() : filter.disabled()));
            }
        }

        @Override
        public void openFilterEditor() {
            FilterDialogHandle currentDialogHandle = dialogHandle;
            if (currentDialogHandle != null) {
                currentDialogHandle.bringToFront();
                return;
            }
            dialogHandle = currentDialogHandle = dialogFactory.startEditFilterDialog(this.filter);
            currentDialogHandle.getResult().thenAccept(newFilterOpt -> {
                dialogHandle = null;
                newFilterOpt.ifPresent(newFilter -> {
                    // Keep enabled status of this filter after editing.
                    newFilter.setEnabled(filter.isEnabled());
                    DialogPanelFilter newPanelFilter = createDialogPanelFilter(newFilter);
                    replaceMeWith(newPanelFilter);
                });
            }).exceptionally(MyFutures::uncaughtException);
        }

        @Override
        public String getTooltip() {
            return filter.getTooltip();
        }

        @Override
        public SavedFilterData getSerializedVersion() {
            return new SavedDialogFilterData(filter);
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

        SavedDialogFilterData(FilterFromDialog filterData) {
            super(filterData.isEnabled());
            this.filterData = filterData;
        }

        @Override
        void appendMe(MainFilterController filterController) {
            try {
                filterData.initialize();
                filterData.setEnabled(enabled);
                // TODO(mlopatkin) This is somewhat dangerous because filterController is still in its constructor
                filterController.addNewDialogFilter(filterData);
            } catch (RequestCompilationException e) {
                logger.error("Invalid filter data", e);
            }
        }
    }
}
