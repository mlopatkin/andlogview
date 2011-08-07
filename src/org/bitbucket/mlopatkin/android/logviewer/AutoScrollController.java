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

import java.awt.Rectangle;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordDataSourceListener;

class AutoScrollController implements LogRecordDataSourceListener, TableModelListener {
    private static final int THRESHOLD = Configuration.ui.autoscrollThreshold();

    private JTable table;
    private LogRecordTableModel model;

    public AutoScrollController(JTable table, LogRecordTableModel model) {
        this.table = table;
        this.model = model;
        this.model.addTableModelListener(this);
    }

    @Override
    public void onNewRecord(LogRecord record) {
        shouldScroll = isAtBottom();
        model.addRecord(record);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (shouldScroll) {
            Rectangle bottomRect = table.getBounds();
            bottomRect.y = bottomRect.height - THRESHOLD;
            table.scrollRectToVisible(bottomRect);
        }
    }

    private boolean shouldScroll;

    private boolean isAtBottom() {
        int bottom = table.getBounds().height;
        int top = table.getVisibleRect().y;
        int height = table.getVisibleRect().height;
        int delta = Math.abs(bottom - (top + height));
        boolean atBottom = delta <= THRESHOLD;
        return atBottom;
    }

}
