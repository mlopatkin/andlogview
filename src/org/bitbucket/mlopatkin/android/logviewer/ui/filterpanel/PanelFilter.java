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
 * This interface must be implemented by the filter instance that needs to be displayed in filter panel.
 */
public interface PanelFilter {

    // TODO figure out how to implement generic filter subscription
    // scoped eventbus looks good for this purpose

    /**
     * Callbacks for filter edit started from a filter panel.
     */
    public interface FilterEditCallback {
        /**
         * Must be called by editor when user saves a changed filter. The panel will replace oldFilter with newFilter
         * internally. All necessary events will be broadcasted.
         *
         * @param oldFilter original filter value
         * @param newFilter change filter
         */
        void onFilterChanged(PanelFilter oldFilter, PanelFilter newFilter);

        /**
         * Must be called by editor if the user decides not to commit edits.
         *
         * @param oldFilter original filter value
         */
        void onEditCancelled(PanelFilter oldFilter);
    }

    /**
     * Returns a nicely formatted tooltip describing the filter. This tooltip is used for the filter button.
     * @return tooltip for the filter
     */
    String getTooltip();

    void setEnabled(boolean enabled);

    boolean isEnabled();

    void openFilterEditor(FilterEditCallback callback);
}
