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

package org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel;

import com.google.common.collect.ImmutableList;

import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.MainFrameScoped;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * This is a state of the panel with filter buttons. It only knows what PanelFilter provides so it can remove
 * filters, enable or disable them. But it cannot, e.g. persist filters, it is the responsibility of the higher level.
 */
@MainFrameScoped
public class FilterPanelModel {

    interface FilterPanelModelListener {
        void onFilterAdded(PanelFilterView newFilter);

        void onFilterRemoved(PanelFilterView filter);

        void onFilterReplaced(PanelFilterView oldFilter, PanelFilterView newFilter);

        void onFilterEnabled(PanelFilterView filter, boolean enabled);
    }

    private final Set<FilterPanelModelListener> listeners = new HashSet<>();
    private final List<PanelFilter> filters = new ArrayList<>();

    @Inject
    public FilterPanelModel() {
    }

    public void addFilter(PanelFilter filter) {
        filters.add(filter);
        for (FilterPanelModelListener listener : listeners) {
            listener.onFilterAdded(filter);
        }
    }

    public void replaceFilter(PanelFilter oldFilter, PanelFilter newFilter) {
        int oldPos = filters.indexOf(oldFilter);
        assert oldPos >= 0;

        filters.set(oldPos, newFilter);

        for (FilterPanelModelListener listener : listeners) {
            listener.onFilterReplaced(oldFilter, newFilter);
        }
    }

    // TODO split into public and internal methods
    public void setFilterEnabled(PanelFilterView filter, boolean enabled) {
        getPanelFilterForView(filter).setEnabled(enabled);

        for (FilterPanelModelListener listener : listeners) {
            listener.onFilterEnabled(filter, enabled);
        }
    }

    void removeFilter(PanelFilterView filter) {
        PanelFilter panelFilter = getPanelFilterForView(filter);
        if (filters.remove(panelFilter)) {
            panelFilter.delete();
            for (FilterPanelModelListener listener : listeners) {
                listener.onFilterRemoved(filter);
            }
        }
    }

    void addListener(FilterPanelModelListener listener) {
        listeners.add(listener);
    }

    void editFilter(PanelFilterView filter) {
        getPanelFilterForView(filter).openFilterEditor();
    }

    ImmutableList<PanelFilterView> getFilters() {
        return ImmutableList.<PanelFilterView>copyOf(filters);
    }

    private PanelFilter getPanelFilterForView(PanelFilterView filterView) {
        return (PanelFilter) filterView;
    }
}
