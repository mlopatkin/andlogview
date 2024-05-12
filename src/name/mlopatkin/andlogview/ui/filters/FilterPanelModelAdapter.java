/*
 * Copyright 2024 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.filters;

import static com.google.common.collect.ImmutableList.toImmutableList;

import name.mlopatkin.andlogview.filters.Filter;
import name.mlopatkin.andlogview.filters.FilterCollection;
import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filterpanel.FilterPanel;
import name.mlopatkin.andlogview.ui.filterpanel.FilterPanelModel;
import name.mlopatkin.andlogview.utils.events.Subject;

import com.google.common.collect.ImmutableList;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.inject.Inject;

/**
 * Adapts {@link FilterModel} to be used in {@link FilterPanel}.
 */
class FilterPanelModelAdapter implements FilterCollection<PanelFilter>, FilterPanelModel<PanelFilter> {
    private final Function<? super Filter, ? extends @Nullable PanelFilter> panelFilterFactory;
    private final Map<Filter, PanelFilter> filters = new HashMap<>();
    private final Subject<FilterPanelModel.FilterPanelModelListener<? super PanelFilter>> listeners = new Subject<>();
    private @MonotonicNonNull FilterModel model;

    @Inject
    FilterPanelModelAdapter(PanelFilterImpl.Factory panelFilterFactory) {
        this((Filter f) -> {
            if (f instanceof FilterFromDialog filterFromDialog) {
                return panelFilterFactory.create(filterFromDialog);
            }
            return null;
        });
    }

    FilterPanelModelAdapter(Function<? super Filter, ? extends @Nullable PanelFilter> panelFilterFactory) {
        this.panelFilterFactory = panelFilterFactory;
    }

    @Inject
    @Override
    public FilterModel.Observer setModel(FilterModel model) {
        this.model = model;
        return FilterCollection.super.setModel(model);
    }

    @Override
    public void addFilter(PanelFilter filter) {
        for (var listener : listeners) {
            listener.onFilterAdded(filter);
        }
    }

    @Override
    public void removeFilter(PanelFilter filter) {
        for (var listener : listeners) {
            listener.onFilterRemoved(filter);
        }
    }

    @Override
    public void replaceFilter(PanelFilter oldFilter, PanelFilter newFilter) {
        for (var listener : listeners) {
            listener.onFilterReplaced(oldFilter, newFilter);
        }
    }

    @Override
    public @Nullable PanelFilter transformFilter(Filter filter) {
        return getOrCreatePanelFilterFor(filter);
    }

    private @Nullable PanelFilter getOrCreatePanelFilterFor(Filter filter) {
        return filters.computeIfAbsent(filter, panelFilterFactory);
    }

    @Override
    public void setFilterEnabled(PanelFilter filter, boolean enabled) {
        filter.setEnabled(enabled);
    }

    @Override
    public void removeFilterForView(PanelFilter filter) {
        filter.delete();
    }

    @Override
    public void addListener(FilterPanelModelListener<? super PanelFilter> listener) {
        listeners.asObservable().addObserver(listener);
    }

    @Override
    public void editFilter(PanelFilter filter) {
        filter.openFilterEditor();
    }

    @Override
    public ImmutableList<PanelFilter> getFilters() {
        return model.getFilters()
                .stream()
                .map(this::getOrCreatePanelFilterFor)
                .filter(Objects::nonNull)
                .collect(toImmutableList());
    }
}
