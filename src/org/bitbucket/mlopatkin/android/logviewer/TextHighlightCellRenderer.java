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

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.bitbucket.mlopatkin.utils.MyStringUtils;

public class TextHighlightCellRenderer implements DecoratingCellRenderer {

    private TableCellRenderer inner;
    private static final String highlightBackgroundColor = "yellow";
    private static final String highlightTextColor = "red";
    private static final String spanBegin = String.format(
            "<span style='color: %s; background-color: %s'>", highlightTextColor,
            highlightBackgroundColor);
    private static final String spanEnd = "</span>";

    private String textToSearch;

    @Override
    public void setInnerRenderer(TableCellRenderer renderer) {
        inner = renderer;
    }

    private String highlightMatches(String value) {
        StringBuilder result = new StringBuilder(value);
        int pos = MyStringUtils.indexOfIgnoreCase(value, textToSearch);
        while (pos != MyStringUtils.NOT_FOUND && pos < result.length()) {
            String val = result.substring(pos, pos + textToSearch.length());
            result.replace(pos, pos + textToSearch.length(), spanBegin + val + spanEnd);
            pos += val.length() + spanBegin.length() + spanEnd.length();
            pos = MyStringUtils.indexOfIgnoreCase(result.toString(), textToSearch, pos);
        }
        result.insert(0, "<html>");
        result.append("</html>");
        return result.toString();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        JLabel result = (JLabel) inner.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
        int modelColumn = table.convertColumnIndexToModel(column);
        if (modelColumn == LogRecordTableModel.COLUMN_MSG
                || modelColumn == LogRecordTableModel.COLUMN_TAG) {
            String valueString = value.toString();
            result.setText(highlightMatches(valueString));
        }

        return result;
    }

    public void setTextToHighLight(String textToSearch) {
        this.textToSearch = textToSearch;
    }
}
