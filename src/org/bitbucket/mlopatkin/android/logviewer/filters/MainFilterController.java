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

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.filters.LogBufferFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.CreateFilterDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.EditFilterDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterDialogFactory;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterFromDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.FilterCreator;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.FilterPanelModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.PanelFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexfilter.IndexFilterCollection;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogModelFilter;
import org.bitbucket.mlopatkin.utils.events.Observable;
import org.bitbucket.mlopatkin.utils.events.Subject;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * The filter controller of the main window. It knows about all existing filters and how they should be persisted,
 * toggled and applied.
 */
@Singleton
public class MainFilterController implements LogModelFilter, FilterCreator {
    // TODO separate "A filter for main table" and "bridge between all filters and panel"
    private final FilterPanelModel filterPanelModel;
    private final FilterDialogFactory dialogFactory;
    private final IndexFilterCollection indexFilterCollection;
    private final Subject<LogModelFilter.Observer> observers = new Subject<>();
    private final FilterChain filterChain = new FilterChain();
    private final LogRecordHighlighter highlighter = new LogRecordHighlighter();

    private final List<BaseToggleFilter<?>> filters = new ArrayList<>();

    private final LogBufferFilter bufferFilter = new LogBufferFilter();

    @Inject
    public MainFilterController(final FilterPanelModel filterPanelModel,
                                IndexFilterCollection indexFilterCollection,
                                FilterDialogFactory dialogFactory) {
        this.filterPanelModel = filterPanelModel;
        this.dialogFactory = dialogFactory;
        this.indexFilterCollection = indexFilterCollection;

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
    }

    public void setBufferEnabled(LogRecord.Buffer buffer, boolean enabled) {
        bufferFilter.setBufferEnabled(buffer, enabled);
        notifyFiltersChanged();
    }

    @Override
    public boolean shouldShowRecord(LogRecord record) {
        return bufferFilter.apply(record) && filterChain.shouldShow(record);
    }

    @Nullable
    @Override
    public Color getHighlightColor(LogRecord record) {
        return highlighter.getColor(record);
    }

    @Override
    public Observable<LogModelFilter.Observer> asObservable() {
        return observers.asObservable();
    }

    @Override
    public void createFilterWithDialog() {
        dialogFactory.startCreateFilterDialog(new CreateFilterDialog.DialogResultReceiver() {
            @Override
            public void onDialogResult(CreateFilterDialog result,
                                       boolean success) {
                if (success) {
                    addNewDialogFilter(result.createFilter());
                }
            }
        });
    }

    private DialogPanelFilter createDialogPanelFilter(FilterFromDialog filter) {
        DialogPanelFilter panelFilter = new DialogPanelFilter(getFilterCollectionForFilter(filter), filter);
        filters.add(panelFilter);
        return panelFilter;
    }

    private void addNewDialogFilter(FilterFromDialog filter) {
        filterPanelModel.addFilter(createDialogPanelFilter(filter).addToCollection());
        notifyFiltersChanged();
    }

    private void notifyFiltersChanged() {
        for (LogModelFilter.Observer observer : observers) {
            observer.onModelChange();
        }
    }

    private FilterCollection<? super FilterFromDialog> getFilterCollectionForFilter(FilterFromDialog filter) {
        switch (filter.getMode()) {
            case SHOW:
            case HIDE:
                return filterChain;
            case HIGHLIGHT:
                return highlighter;
            case WINDOW:
                return indexFilterCollection;
        }
        assert false;
        return null;
    }

    /**
     * Base class for {@link PanelFilter} wrappers for the DialogFilters and others.
     */
    abstract class BaseToggleFilter<T extends Predicate<LogRecord>> implements PanelFilter {
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
            filterPanelModel.removeFilter(this);
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
            } else {
                collection.removeFilter(mode, filter);
                replacement.addToCollection();
            }
            notifyFiltersChanged();
        }

        public BaseToggleFilter<T> addToCollection() {
            collection.addFilter(mode, filter);
            return this;
        }
    }


    class DialogPanelFilter extends BaseToggleFilter<FilterFromDialog> {

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
                                                   Optional<FilterFromDialog> newFilter,
                                                   boolean success) {
                            if (success) {
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
    }
}
