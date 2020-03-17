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

package org.bitbucket.mlopatkin.android.logviewer.filters;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.config.ConfigStorage;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.CreateFilterDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.EditFilterDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterDialogFactory;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterFromDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.FilterCreator;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.FilterPanelModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.PanelFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexfilter.IndexFilterCollection;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.MainFrameScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

/**
 * The filter controller of the main window. It knows about all existing filters and how they should be persisted,
 * toggled and applied.
 */
@MainFrameScoped
public class MainFilterController implements FilterCreator {
    // TODO separate "A filter for main table" and "bridge between all filters and panel"

    private static final Logger logger = Logger.getLogger(MainFilterController.class);

    private final FilterPanelModel filterPanelModel;
    private final FilterDialogFactory dialogFactory;
    private final IndexFilterCollection indexFilterCollection;
    private final ConfigStorage storage;
    private final LogModelFilterImpl filter;

    private final List<BaseToggleFilter<?>> filters = new ArrayList<>();

    private static final FilterListSerializer SERIALIZER = new FilterListSerializer();

    @Inject
    public MainFilterController(final FilterPanelModel filterPanelModel,
                                IndexFilterCollection indexFilterCollection,
                                FilterDialogFactory dialogFactory,
                                ConfigStorage storage,
                                LogModelFilterImpl logModelFilter) {
        this.filterPanelModel = filterPanelModel;
        this.dialogFactory = dialogFactory;
        this.indexFilterCollection = indexFilterCollection;
        this.storage = storage;
        this.filter = logModelFilter;

        indexFilterCollection.asObservable().addObserver(new IndexFilterCollection.Observer() {
            @Override
            public void onFilterDisabled(Predicate<LogRecord> filter) {
                for (BaseToggleFilter<?> registeredFilter : filters) {
                    if (Objects.equals(registeredFilter.filter, filter)) {
                        filterPanelModel.setFilterEnabled(registeredFilter, false);
                    }
                }
            }
        });

        for (SavedFilterData savedFilterData : storage.loadConfig(SERIALIZER)) {
            savedFilterData.appendMe(this);
        }
    }

    public void setBufferEnabled(LogRecord.Buffer buffer, boolean enabled) {
        filter.bufferFilter.setBufferEnabled(buffer, enabled);
        notifyFiltersChanged();
    }

    @Override
    public void createFilterWithDialog() {
        dialogFactory.startCreateFilterDialog(new CreateFilterDialog.DialogResultReceiver() {
            @Override
            public void onDialogResult(Optional<FilterFromDialog> filter) {
                if (filter.isPresent()) {
                    addNewDialogFilter(filter.get());
                }
            }
        });
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
        storage.saveConfig(SERIALIZER, serializedFilters);
    }

    private FilterCollection<? super FilterFromDialog> getFilterCollectionForFilter(FilterFromDialog filter) {
        switch (filter.getMode()) {
            case SHOW:
            case HIDE:
                return this.filter.filterChain;
            case HIGHLIGHT:
                return this.filter.highlighter;
            case WINDOW:
                return indexFilterCollection;
        }
        assert false;
        return null;
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
            assert myPos >= 0;
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

        protected DialogPanelFilter(FilterCollection<? super FilterFromDialog> collection,
                                    FilterFromDialog filter) {
            super(collection, filter.getMode(), filter);
        }

        @Override
        public void openFilterEditor() {
            dialogFactory.startEditFilterDialog(
                    filter,
                    new EditFilterDialog.DialogResultReceiver() {
                        @Override
                        public void onDialogResult(FilterFromDialog oldFilter,
                                                   Optional<FilterFromDialog> newFilter) {
                            if (newFilter.isPresent()) {
                                DialogPanelFilter newPanelFilter = createDialogPanelFilter(newFilter.get());
                                replaceMeWith(newPanelFilter);
                            }
                        }
                    });
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

    private static class SavedDialogFilterData extends SavedFilterData {

        private final FilterFromDialog filterData;

        SavedDialogFilterData(FilterFromDialog filterData, boolean enabled) {
            super(enabled);
            this.filterData = filterData;
        }

        @Override
        void appendMe(MainFilterController filterController) {
            try {
                filterData.initialize();
                filterController.addNewDialogFilter(filterData).setEnabled(enabled);
            } catch (RequestCompilationException e) {
                logger.error("Invalid filter data", e);
            }
        }
    }
}
