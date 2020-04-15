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

import com.google.common.collect.ImmutableList;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

import javax.inject.Inject;

/**
 * Basic presenter for popup menu of table items. It provides only "Copy" command.
 */
public class PopupMenuPresenter<T extends PopupMenuPresenter.PopupMenuView> {
    /**
     * Interface for Presenter to talk to View. As a general rule, action is not shown at all if the corresponding
     * setter method wasn't called.
     */
    public interface PopupMenuView {
        /**
         * Enables or disables "Copy" action. There is no callback because typically table handles copy action itself.
         *
         * @param enabled if menu item for copy should be enabled or disabled
         */
        void setCopyActionEnabled(boolean enabled);

        /**
         * Opens popup menu.
         */
        void show();
    }

    protected final SelectedRows selectedRows;

    @Inject
    public PopupMenuPresenter(SelectedRows selectedRows) {
        this.selectedRows = selectedRows;
    }

    /**
     * Performs configuration of the menu view but doesn't show it. Subclasses may override this method to
     * append/replace parent menu configuration.
     *
     * @param view the view to configure
     * @param c the column that was clicked
     * @param row the row that was clicked or {@code null} if click was outside of row
     * @param selection the selected table rows, can be empty. If the row is non-null then selection contains
     *         it.
     */
    protected void configureMenu(T view, Column c, @Nullable TableRow row, List<TableRow> selection) {
        view.setCopyActionEnabled(!selection.isEmpty());
    }

    /**
     * Prepares and shows the context menu.
     * @param view the context menu view
     * @param c the column that was clicked
     * @param row he row that was clicked or {@code null} if click was outside of row
     */
    public final void showContextMenu(T view, Column c, @Nullable TableRow row) {
        ImmutableList<TableRow> selection = this.selectedRows.getSelectedRows();
        assert (row == null && selection.isEmpty()) || (row != null && selectedRows.isRowSelected(
                row)) : "Selection wasn't adjusted";
        configureMenu(view, c, row, selection);
        view.show();
    }
}
