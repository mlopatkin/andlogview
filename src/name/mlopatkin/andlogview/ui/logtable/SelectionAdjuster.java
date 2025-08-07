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

package name.mlopatkin.andlogview.ui.logtable;

import org.jspecify.annotations.Nullable;

import javax.inject.Inject;

/**
 * Adjusts selection when popup menu is triggered. The selection adjustment is performed according to the following
 * rules:
 * <ul>
 * <li>Nothing is selected
 * <ul>
 * <li>right-click at a line moves the selection to it and opens the context
 * menu for this line</li>
 * <li>right-click at blank opens the context menu for no lines - with disabled
 * items</li>
 * </ul>
 * </li>
 * <li>Some lines are selected
 * <ul>
 * <li>right-click at a selected line opens the context menu for all selected
 * lines</li>
 * <li>right-click at an unselected line moves the selection to it and opens the
 * context menu for this line</li>
 * <li>right-click at blank clears the selection and opens the context menu for
 * no lines - with disabled items</li>
 * </ul>
 * </li>
 * </ul>
 */
class SelectionAdjuster {
    private final SelectedRows selectedRows;

    @Inject
    public SelectionAdjuster(SelectedRows selectedRows) {
        this.selectedRows = selectedRows;
    }

    public void onPopupMenuShown(@Nullable TableRow row) {
        if (row != null) {
            if (!selectedRows.isRowSelected(row)) {
                selectedRows.setSelectedRow(row);
            }
        } else {
            selectedRows.clearSelection();
        }
    }
}
