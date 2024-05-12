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

import name.mlopatkin.andlogview.filters.Filter;
import name.mlopatkin.andlogview.filters.FilterCollection;
import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filterpanel.FilterPanelModelImpl;
import name.mlopatkin.andlogview.ui.filterpanel.PanelFilter;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Binds together the {@link FilterPanelModelImpl} and {@link FilterModel}.
 */
public class FilterPanelModelAdapter implements FilterCollection<PanelFilter> {
    private final FilterPanelModelImpl filterPanelModel;
    private final PanelFilterImpl.Factory panelFilterFactory;
    private final Map<FilterFromDialog, PanelFilter> filters = new HashMap<>();

    @Inject
    FilterPanelModelAdapter(
            FilterPanelModelImpl filterPanelModel,
            PanelFilterImpl.Factory panelFilterFactory
    ) {
        this.filterPanelModel = filterPanelModel;
        this.panelFilterFactory = panelFilterFactory;
    }

    @Inject
    @Override
    public FilterModel.Observer setModel(FilterModel model) {
        return FilterCollection.super.setModel(model);
    }

    @Override
    public void addFilter(PanelFilter filter) {
        filterPanelModel.addFilter(filter);
    }

    @Override
    public void removeFilter(PanelFilter filter) {
        filterPanelModel.removeFilter(filter);
    }

    @Override
    public void replaceFilter(PanelFilter oldFilter, PanelFilter newFilter) {
        filterPanelModel.replaceFilter(oldFilter, newFilter);
    }

    @Override
    public @Nullable PanelFilter transformFilter(Filter filter) {
        return getOrCreateFilter(filter);
    }

    private @Nullable PanelFilter getOrCreateFilter(Filter filter) {
        if (!(filter instanceof FilterFromDialog filterFromDialog)) {
            return null;
        }
        return filters.computeIfAbsent(filterFromDialog, this::buildPanelFilter);
    }

    private PanelFilter buildPanelFilter(FilterFromDialog filter) {
        return panelFilterFactory.create(filter);
    }
}
