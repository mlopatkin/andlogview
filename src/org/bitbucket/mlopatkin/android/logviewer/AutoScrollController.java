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

import java.awt.Container;
import java.awt.EventQueue;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

class AutoScrollController implements TableModelListener {
    private JTable table;
    private LogRecordTableModel model;

    public AutoScrollController(JTable table, LogRecordTableModel model) {
        this.table = table;
        this.model = model;
        this.model.addTableModelListener(this);
    }

    private Runnable scrollToTheEnd = new Runnable() {
        @Override
        public void run() {
            table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1,
                    TableModelEvent.ALL_COLUMNS, true));
        }
    };

    @Override
    public void tableChanged(final TableModelEvent e) {
        if (e.getType() == TableModelEvent.INSERT) {
            if (shouldScroll) {
                EventQueue.invokeLater(scrollToTheEnd);
            }
        } else {
            shouldScroll = false;
        }
    }

    private boolean shouldScroll;

    private boolean isAtBottom() {
        Container parent = table.getParent();
        int bottom = table.getBounds().height;
        int pHeight = parent.getBounds().height;
        int y = table.getBounds().y;
        boolean atBottom = (pHeight - y) == bottom;
        return atBottom;
    }

    public void notifyBeforeInsert() {
        shouldScroll = isAtBottom();
    }
}
