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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.MultiPidFilter;
import org.bitbucket.mlopatkin.android.liblogcat.SingleTagFilter;

public class LogRecordPopupMenuHandler {

    private JMenuItem hideWithThisTag = new JMenuItem("Hide with this tag");
    private JMenuItem hideWithThisPid = new JMenuItem("Hide with this pid");
    private JPopupMenu popupMenu = new JPopupMenu();

    private Point p;

    private JTable table;

    public LogRecordPopupMenuHandler(JTable table, final FilterController filterController) {
        hideWithThisTag.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                filterController.addHideFilter(new SingleTagFilter(getLogRecordAtPoint().getTag()));
            }
        });

        hideWithThisPid.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                filterController.addHideFilter(new MultiPidFilter(new int[] { getLogRecordAtPoint()
                        .getPid() }));
            }
        });
        popupMenu.add(hideWithThisTag);
        popupMenu.add(hideWithThisPid);
        addPopup(table, popupMenu);
        this.table = table;
    }

    private void addPopup(Component component, final JPopupMenu popup) {
        component.addMouseListener(new MouseAdapter() {
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
                p = e.getPoint();
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    private LogRecord getLogRecordAtPoint() {
        int row = table.convertRowIndexToModel(table.rowAtPoint(p));
        return ((LogRecordTableModel) table.getModel()).getRowData(row);
    }
}
