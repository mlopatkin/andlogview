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
package name.mlopatkin.andlogview.ui.search.logtable;

import name.mlopatkin.andlogview.TooltipGenerator;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.search.logrecord.RowSearchStrategy;
import name.mlopatkin.andlogview.search.text.TextHighlighter;
import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.widgets.DecoratingCellRenderer;
import name.mlopatkin.andlogview.widgets.UiHelper;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Component;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class SearchResultsHighlightCellRenderer implements DecoratingCellRenderer {
    private @MonotonicNonNull TableCellRenderer inner;
    private @Nullable RowSearchStrategy strategy;

    @Override
    public void setInnerRenderer(TableCellRenderer renderer) {
        inner = renderer;
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int columnIndex) {
        // TODO(mlopatkin) Can we have a better flow?
        assert inner != null;
        JComponent c =
                (JComponent) inner.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, columnIndex);
        LogRecordTableModel model = (LogRecordTableModel) table.getModel();
        LogRecord rowData = model.getRowData(table.convertRowIndexToModel(row));
        Column column = model.getColumn(table.convertColumnIndexToModel(columnIndex));
        if (isSearchableColumn(column)) {
            if (value != null) {
                var field = column.getRecordField();
                // searchable columns always have backing fields.
                assert field != null;
                String text = value.toString();
                if (!UiHelper.isTextFit(c, table, row, columnIndex, text)) {
                    TooltipGenerator tooltip = new TooltipGenerator(text);
                    if (strategy != null) {
                        strategy.highlightColumn(rowData, field, tooltip);
                    }
                    c.setToolTipText(tooltip.getTooltip());
                } else {
                    c.setToolTipText(null);
                }
                if (strategy != null) {
                    TextHighlighter th = (TextHighlighter) c;
                    strategy.highlightColumn(rowData, field, th);
                }
            }
        }
        return c;
    }

    private boolean isSearchableColumn(Column c) {
        return (Objects.equals(c, Column.MESSAGE) || Objects.equals(c, Column.TAG) || Objects.equals(c,
                Column.APP_NAME));
    }

    public void setHighlightStrategy(@Nullable RowSearchStrategy strategy) {
        this.strategy = strategy;
    }
}
