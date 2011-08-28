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

import java.awt.Color;
import java.awt.Component;
import java.util.EnumMap;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;
import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingCellRenderer;

public class PriorityColoredCellRenderer implements
        DecoratingCellRenderer {

    private static final long serialVersionUID = -5160005091082094580L;

    private static final EnumMap<Priority, Color> COLOR_MAP = new EnumMap<Priority, Color>(
            Priority.class);
    static {
        for(Priority p : Priority.values()) {
            COLOR_MAP.put(p, Configuration.ui.priorityColor(p));
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        Component result = inner.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                row, column);
        row = table.convertRowIndexToModel(row);
        Priority priority = (Priority) table.getModel().getValueAt(row,
                LogRecordTableModel.COLUMN_PRIORITY);
        result.setForeground(COLOR_MAP.get(priority));
        return result;
    }

    private TableCellRenderer inner;

    @Override
    public void setInnerRenderer(TableCellRenderer renderer) {
        inner = renderer;
    }
}
