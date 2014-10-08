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

/**
 * This is a state of the panel with filter buttons. It only knows what PanelFilter provides so it can remove
 * filters, enable or disable them. But it cannot, e.g. persist filters, it is the responsibility of the higher level.
 */
public class FilterPanelModel {

    public interface FilterPanelModelListener {
        void onFilterAdded(PanelFilter newFilter);

        void onFilterRemoved(PanelFilter filter);

        void onFilterReplaced(PanelFilter oldFilter, PanelFilter newFilter);
    }

    public void addFilter(PanelFilter filter) {
    }

    public void removeFilter(PanelFilter filter) {
    }

    public void replaceFilter(PanelFilter oldFilter, PanelFilter newFilter) {
    }

    public void setFilterEnabled(PanelFilter filter, boolean enabled) {
    }

    public void addListener(FilterPanelModelListener listener) {
    }
}
