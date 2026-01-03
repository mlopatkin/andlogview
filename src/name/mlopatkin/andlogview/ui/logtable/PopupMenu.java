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

import name.mlopatkin.andlogview.widgets.UiHelper;

import com.google.common.base.Preconditions;

import org.jspecify.annotations.Nullable;

import java.awt.Point;

import javax.inject.Inject;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

/**
 * This class manages a popup menu for the table.
 */
public class PopupMenu {
    /**
     * Clients of PopupMenu provide delegate to show actual menu.
     */
    public interface Delegate {
        /**
         * Called when PopupMenu should be shown.
         *
         * @param table the table to show popup menu for
         * @param x the X coordinate (in table's coordinates) of mouse click to show menu at
         * @param y the Y coordinate (in table's coordinates) of mouse click to show menu at
         * @param column the column which was clicked
         * @param row the row which was clicked or {@code null} if the click was outside any row
         */
        void showMenu(JTable table, int x, int y, Column column, @Nullable TableRow row);
    }

    private final Delegate delegate;
    private final SelectionAdjuster selectionAdjuster;

    @Inject
    PopupMenu(Delegate delegate, SelectionAdjuster selectionAdjuster) {
        this.delegate = delegate;
        this.selectionAdjuster = selectionAdjuster;
    }

    void attachToTable(LogTable table) {
        UiHelper.addPopupMenu(table, this::showMenuIfNeeded);
    }

    private void showMenuIfNeeded(LogTable table, @Nullable Point p) {
        if (p == null) {
            showMenuForSelection(table);
            return;
        }

        Column column = getColumnAt(table, p);
        TableRow tableRow = getRowAt(table, p);
        selectionAdjuster.onPopupMenuShown(tableRow);
        delegate.showMenu(table, p.x, p.y, column, tableRow);
    }

    private void showMenuForSelection(LogTable table) {
        var rsm = table.getSelectionModel();
        var csm = table.getColumnModel().getSelectionModel();

        // Lead selection index serves as an index for the focused cell in the table.
        // Focused cell may not be selected.
        int r = rsm.getLeadSelectionIndex();
        int c = csm.getLeadSelectionIndex();

        TableRow tableRow = getRowAt(table, r);

        if (rsm.isSelectedIndex(r) && csm.isSelectedIndex(c)) {
            // We're within the selection. No adjustment needed.
        } else {
            // Focused column is out of selection, need to adjust it first
            selectionAdjuster.onPopupMenuShown(tableRow);
        }

        final int x, y;
        final Column column;
        if (r >= 0 && c >= 0) {
            column = getColumnAt(table, c);

            var cellRect = table.getCellRect(r, c, false);
            table.scrollRectToVisible(cellRect);

            x = cellRect.x + cellRect.width / 2;
            y = cellRect.y + cellRect.height / 2;
        } else {
            column = getColumnAt(table, 0);
            var tableBounds = table.getVisibleRect();

            x = tableBounds.x + tableBounds.width / 2;
            y = tableBounds.y + tableBounds.height / 2;
        }

        delegate.showMenu(table, x, y, column, tableRow);
    }

    private Column getColumnAt(LogTable table, Point p) {
        int columnIndex = table.columnAtPoint(p);
        return getColumnAt(table, columnIndex);
    }

    private Column getColumnAt(LogTable table, int columnIndex) {
        Preconditions.checkArgument(columnIndex >= 0, "Unsupported column index=%s", columnIndex);
        TableColumnModel columnModel = table.getColumnModel();

        Object id = columnModel.getColumn(columnIndex).getIdentifier();
        Preconditions.checkState(id instanceof Column, "Unsupported column model %s: got id=%s", columnModel, id);
        return (Column) id;
    }

    private @Nullable TableRow getRowAt(LogTable table, Point p) {
        int rowViewIndex = table.rowAtPoint(p);
        return getRowAt(table, rowViewIndex);
    }

    private static @Nullable TableRow getRowAt(LogTable table, int rowViewIndex) {
        if (rowViewIndex == -1) {
            return null;
        }
        int rowModelIndex = table.convertRowIndexToModel(rowViewIndex);
        assert rowModelIndex >= 0;

        LogRecordTableModel model = (LogRecordTableModel) table.getModel();
        return model.getRow(rowModelIndex);
    }
}
