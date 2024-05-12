/*
 * Copyright 2014 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.filterpanel;

import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * An implementation of {@link FilterPanelModel}.
 */
@MainFrameScoped
public class FilterPanelModelImpl implements FilterPanelModel<PanelFilter> {
    private final Set<FilterPanelModelListener<? super PanelFilter>> listeners = new HashSet<>();
    private final List<PanelFilter> filters = new ArrayList<>();

    @Inject
    public FilterPanelModelImpl() {}

    /**
     * Adds the new PanelFilter to this model.
     *
     * @param filter the new filter
     */
    public void addFilter(PanelFilter filter) {
        filters.add(filter);
        for (var listener : listeners) {
            listener.onFilterAdded(filter);
        }
    }

    /**
     * Replaces one filter with another. The filter's position isn't affected
     *
     * @param oldFilter the removed filter
     * @param newFilter
     */
    public void replaceFilter(PanelFilter oldFilter, PanelFilter newFilter) {
        int oldPos = filters.indexOf(oldFilter);
        assert oldPos >= 0;

        filters.set(oldPos, newFilter);

        for (var listener : listeners) {
            listener.onFilterReplaced(oldFilter, newFilter);
        }
    }

    public void removeFilter(PanelFilter removedFilter) {
        if (filters.remove(removedFilter)) {
            for (var listener : listeners) {
                listener.onFilterRemoved(removedFilter);
            }
        }
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
        listeners.add(listener);
    }

    @Override
    public void editFilter(PanelFilter filter) {
        filter.openFilterEditor();
    }

    @Override
    public ImmutableList<PanelFilter> getFilters() {
        return ImmutableList.copyOf(filters);
    }
}
