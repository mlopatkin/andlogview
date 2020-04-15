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

package org.bitbucket.mlopatkin.android.logviewer.ui.logtable;

import com.google.common.collect.ImmutableList;

/**
 * Model: currently selected records with methods to manipulate selection. Uses {@link TableRow} for performance
 * reasons.
 */
public interface SelectedRows {
    /**
     * Returns a possibly empty list of currently selected rows. Note that the row indices may be updated when new
     * records arrive so it isn't safe to store them for a long time.
     *
     * @return the list of selected rows
     */
    ImmutableList<TableRow> getSelectedRows();

    /**
     * Makes all selected rows unselected.
     */
    void clearSelection();

    /**
     * Makes the give row selected. All previously selected rows become unselected.
     *
     * @param row the row to select
     */
    void setSelectedRow(TableRow row);

    /**
     * Checks if the row is selected
     *
     * @param row the row to check
     * @return {@code true} if the given row is selected
     */
    boolean isRowSelected(TableRow row);
}
