/*
 * Copyright 2011 Mikhail Lopatkin
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
package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JPopupMenu;
import javax.swing.JTable;

/**
 * This class implements popup menu for {@link JTable} that follows specific
 * policy of selecting items with right-click.
 * 
 * <p>
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
public class TablePopupMenu extends JPopupMenu {

    public interface ItemsUpdater {
        void updateItemsState(JTable source);
    }

    private static final int NO_ROW = -1;

    private ItemsUpdater updater;

    public TablePopupMenu(ItemsUpdater updater) {
        this.updater = updater;
    }

    @Override
    public void show(Component invoker, int x, int y) {
        if (invoker instanceof JTable) {
            JTable table = (JTable) invoker;
            adjustSelection(table, table.rowAtPoint(new Point(x, y)));
            if (updater != null) {
                updater.updateItemsState(table);
            }
        }
        super.show(invoker, x, y);
    }

    private void adjustSelection(JTable table, int clickedRow) {
        if (clickedRow == NO_ROW) {
            table.clearSelection();
        } else if (!table.getSelectionModel().isSelectedIndex(clickedRow)) {
            table.getSelectionModel().setSelectionInterval(clickedRow, clickedRow);
        }
    }
}
