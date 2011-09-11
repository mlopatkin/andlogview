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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.JTable;

/**
 * This class implements context menu for {@link JTable} that follows specific
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

    /**
     * Implementation of this interface is responsible for making context menu
     * content to match with the table state.
     */
    public interface ItemsUpdater {
        /**
         * Called before menu is shown. Implement this method to adjust the
         * context menu's state according to the table's state.
         * 
         * @param source
         *            the table for which the menu will be shown
         */
        void updateItemsState(JTable source);
    }

    private static final int NO_ROW = -1;

    private List<ItemsUpdater> updaters = new ArrayList<ItemsUpdater>();

    /**
     * Adds an updater.
     * 
     * @param updater
     *            a non-{@code null} updater to add
     * @throws NullPointerException
     *             if {@code updater} is null
     */
    public void addItemsUpdater(ItemsUpdater updater) {
        if (updater == null) {
            throw new NullPointerException("updater can't be null");
        }
        updaters.add(updater);
    }

    /**
     * Removes a previously added updater.
     * 
     * @param updater
     *            an updater to remove
     */
    public void removeItemsUpdater(ItemsUpdater updater) {
        updaters.remove(updater);
    }

    @Override
    public void show(Component invoker, int x, int y) {
        if (invoker instanceof JTable) {
            JTable table = (JTable) invoker;
            adjustSelection(table, table.rowAtPoint(new Point(x, y)));
            for (ItemsUpdater updater : updaters) {
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
