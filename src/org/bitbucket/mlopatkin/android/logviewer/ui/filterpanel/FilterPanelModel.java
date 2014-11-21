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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is a state of the panel with filter buttons. It only knows what PanelFilter provides so it can remove
 * filters, enable or disable them. But it cannot, e.g. persist filters, it is the responsibility of the higher level.
 */
public class FilterPanelModel {

    public interface FilterPanelModelListener {
        void onFilterAdded(PanelFilter newFilter);

        void onFilterRemoved(PanelFilter filter);

        void onFilterReplaced(PanelFilter oldFilter, PanelFilter newFilter);

        void onFilterEnabled(PanelFilter filter, boolean enabled);
    }

    private final Set<FilterPanelModelListener> listeners = new HashSet<>();
    private final List<PanelFilter> filters = new ArrayList<>();

    public void addFilter(PanelFilter filter) {
        filters.add(filter);
        for (FilterPanelModelListener listener : listeners) {
            listener.onFilterAdded(filter);
        }
    }

    public void removeFilter(PanelFilter filter) {
        if (filters.remove(filter)) {
            for (FilterPanelModelListener listener : listeners) {
                listener.onFilterRemoved(filter);
            }
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

    public void setFilterEnabled(PanelFilter filter, boolean enabled) {
        for (FilterPanelModelListener listener : listeners) {
            listener.onFilterEnabled(filter, enabled);
        }
    }

    public void addListener(FilterPanelModelListener listener) {
        listeners.add(listener);
    }
}
