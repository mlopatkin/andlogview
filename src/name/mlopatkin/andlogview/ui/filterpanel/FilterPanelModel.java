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
 * This is a state of the panel with filter buttons. It provides the panel a way to obtain filter view models, as well
 * as to manipulate the available filters.
 *
 * @param <V> the view model type
 */
public interface FilterPanelModel<V extends PanelFilterView> {
    interface FilterPanelModelListener<V extends PanelFilterView> {
        void onFilterAdded(V newFilter);

        void onFilterRemoved(V filter);

        void onFilterReplaced(V oldFilter, V newFilter);
    }

    /**
     * Requests to change enabled state of the filter represented by the view.
     *
     * @param filter the filter view
     * @param enabled the requested status
     */
    void setFilterEnabled(V filter, boolean enabled);

    /**
     * Requests to remove the filter represented by the view.
     *
     * @param filter the filter view
     */
    void removeFilterForView(V filter);

    void addListener(FilterPanelModelListener<? super V> listener);

    /**
     * Requests to edit the filter represented by the view.
     *
     * @param filter the filter view
     */
    void editFilter(V filter);

    ImmutableList<V> getFilters();
}
