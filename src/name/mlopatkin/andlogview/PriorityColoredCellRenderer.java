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
package name.mlopatkin.andlogview;

import name.mlopatkin.andlogview.logmodel.LogRecord.Priority;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.ui.themes.ThemeColors;
import name.mlopatkin.andlogview.widgets.DecoratingCellRenderer;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class PriorityColoredCellRenderer implements DecoratingCellRenderer {
    private final ThemeColors themeColors;

    private @MonotonicNonNull TableCellRenderer inner;

    public PriorityColoredCellRenderer(ThemeColors themeColors) {
        this.themeColors = themeColors;
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        assert inner != null;
        Component result = inner.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        row = table.convertRowIndexToModel(row);
        LogRecordTableModel model = (LogRecordTableModel) table.getModel();
        Priority priority = model.getRowData(row).getPriority();
        result.setForeground(themeColors.getPriorityForegroundColor(priority));
        return result;
    }

    @Override
    public void setInnerRenderer(TableCellRenderer renderer) {
        inner = renderer;
    }
}
