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

package name.mlopatkin.andlogview.ui.filterpanel;

import com.google.common.collect.ImmutableList;

/**
 * This is a state of the panel with filter buttons. It only knows what PanelFilter provides, so it can remove
 * filters, enable or disable them. But it cannot, e.g. persist filters, it is the responsibility of the higher level.
 */
public interface FilterPanelModel {
    interface FilterPanelModelListener {
        void onFilterAdded(PanelFilterView newFilter);

        void onFilterRemoved(PanelFilterView filter);

        void onFilterReplaced(PanelFilterView oldFilter, PanelFilterView newFilter);
    }

    /**
     * Requests to change enabled state of the filter represented by the view.
     *
     * @param filter the filter view
     * @param enabled the requested status
     */
    void setFilterEnabled(PanelFilterView filter, boolean enabled);

    /**
     * Requests to remove the filter represented by the view.
     *
     * @param filter the filter view
     */
    void removeFilterForView(PanelFilterView filter);

    void addListener(FilterPanelModelListener listener);

    /**
     * Requests to edit the filter represented by the view.
     *
     * @param filter the filter view
     */
    void editFilter(PanelFilterView filter);

    ImmutableList<PanelFilterView> getFilters();
}
