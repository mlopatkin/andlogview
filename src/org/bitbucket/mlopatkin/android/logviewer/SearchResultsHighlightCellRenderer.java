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

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.bitbucket.mlopatkin.android.logviewer.search.HighlightStrategy;
import org.bitbucket.mlopatkin.android.logviewer.search.TextHighlighter;
import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingCellRenderer;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;

public class SearchResultsHighlightCellRenderer implements DecoratingCellRenderer {

    private TableCellRenderer inner;
    private HighlightStrategy strategy;

    @Override
    public void setInnerRenderer(TableCellRenderer renderer) {
        inner = renderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        JComponent c = (JComponent) inner.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
        int modelColumn = table.convertColumnIndexToModel(column);
        if (modelColumn == LogRecordTableModel.COLUMN_MSG
                || modelColumn == LogRecordTableModel.COLUMN_TAG) {
            if (value != null) {
                String text = value.toString();
                if (!UiHelper.isTextFit(c, table, row, column, text)) {
                    TooltipGenerator tooltip = new TooltipGenerator(text);
                    if (strategy != null) {
                        strategy.highlightOccurences(text, tooltip);
                    }
                    c.setToolTipText(tooltip.getTooltip());
                } else {
                    c.setToolTipText(null);
                }
                if (strategy != null) {
                    TextHighlighter th = (TextHighlighter) c;
                    strategy.highlightOccurences(text, th);
                }
            }
        }
        return c;
    }

    public void setHighlightStrategy(HighlightStrategy strategy) {
        this.strategy = strategy;
    }
}
