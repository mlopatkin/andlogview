/*
 * Copyright 2020 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.popupmenu;

import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterFromDialog;

/**
 * An interface to add new filters to the main filter panel.
 */
public interface MenuFilterCreator {
    /**
     * Adds a new filter.
     *
     * @param filter the filter to add
     */
    void addFilter(FilterFromDialog filter);

    /**
     * Opens a filter creation dialog. Dialog fields are pre-filled with data from {@code baseData}.
     *
     * @param baseData the filter with initial data for filter
     */
    void createFilterWithDialog(FilterFromDialog baseData);
}
