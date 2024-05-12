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

package name.mlopatkin.andlogview.ui.filterpanel;

/**
 * The instance of this interface represents a filter inside the FilterPanel. It provides information about how to
 * display filter as a button.
 */
public interface PanelFilterView {
    /**
     * Returns a nicely formatted tooltip describing the filter. This tooltip is used for the filter button.
     *
     * @return tooltip for the filter
     */
    String getTooltip();

    /**
     * Returns the current state of the filter. The enabled filter is active, so it is applied to the log data.
     *
     * @return {@code true} if the filter is enabled
     */
    boolean isEnabled();
}
