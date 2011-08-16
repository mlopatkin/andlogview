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

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTable;

class TablePopupMenuHandler {

    public TablePopupMenuHandler(JTable table) {
        this.table = table;
        addPopup();
    }

    protected int getTargetRow() {
        return table.rowAtPoint(poppedUpAt);
    }

    protected JTable getTable() {
        return table;
    }

    protected JPopupMenu getMenu() {
        return popupMenu;
    }

    protected int getRow() {
        return table.convertRowIndexToModel(getTargetRow());
    }

    private Point poppedUpAt;
    private JTable table;
    private JPopupMenu popupMenu = new JPopupMenu();

    private void addPopup() {
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            private void showMenu(MouseEvent e) {
                poppedUpAt = e.getPoint();
                int row = getTargetRow();
                table.getSelectionModel().setSelectionInterval(row, row);
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }
}
