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

import com.google.common.base.Preconditions;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
    public PopupMenu(Delegate delegate, SelectionAdjuster selectionAdjuster) {
        this.delegate = delegate;
        this.selectionAdjuster = selectionAdjuster;
    }

    public void attachToTable(LogTable table) {
        table.addMouseListener(createMouseHandler(table));
    }

    private MouseListener createMouseHandler(LogTable table) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showMenuIfNeeded(table, e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showMenuIfNeeded(table, e);
            }
        };
    }

    private void showMenuIfNeeded(LogTable table, MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        Column column = getColumnAt(table, e.getPoint());
        TableRow tableRow = getRowAt(table, e.getPoint());
        selectionAdjuster.onPopupMenuShown(tableRow);
        delegate.showMenu(table, e.getX(), e.getY(), column, tableRow);
    }

    private Column getColumnAt(LogTable table, Point p) {
        int columnIndex = table.columnAtPoint(p);
        Preconditions.checkArgument(columnIndex >= 0, "Unsupported column index=%s", columnIndex);
        TableColumnModel columnModel = table.getColumnModel();

        Object id = columnModel.getColumn(columnIndex).getIdentifier();
        Preconditions.checkState(id instanceof Column, "Unsupported column model %s: got id=%s", columnModel, id);
        return (Column) id;
    }

    private @Nullable TableRow getRowAt(LogTable table, Point p) {
        int rowViewIndex = table.rowAtPoint(p);
        if (rowViewIndex == -1) {
            return null;
        }
        int rowModelIndex = table.convertRowIndexToModel(rowViewIndex);
        assert rowModelIndex >= 0;

        LogRecordTableModel model = (LogRecordTableModel) table.getModel();
        return model.getRow(rowModelIndex);
    }
}
