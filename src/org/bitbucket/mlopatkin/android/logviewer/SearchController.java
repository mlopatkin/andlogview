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

import org.apache.commons.lang3.StringUtils;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

public class SearchController {

    private DecoratingRendererTable table;
    private LogRecordTableModel model;
    private String text;
    private int curRow;
    private TextHighlightCellRenderer renderer = new TextHighlightCellRenderer();

    public SearchController(DecoratingRendererTable table, LogRecordTableModel model) {
        this.table = table;
        this.model = model;
    }

    private static final int MODE_FORWARD = 1;
    private static final int MODE_BACKWARD = -1;

    public boolean startSearch(String text) {
        this.text = text;
        if (StringUtils.isBlank(text)) {
            table.removeDecorator(renderer);
            return false;
        }
        renderer.setTextToHighLight(text);
        table.addDecorator(renderer);
        curRow = table.getSelectedRow();
        return performSearch(MODE_FORWARD, curRow >= 0);
    }

    public boolean searchNext() {
        return performSearch(MODE_FORWARD, false);
    }

    public boolean searchPrev() {
        return performSearch(MODE_BACKWARD, false);
    }

    private boolean performSearch(int searchMode, boolean scanCurrentRow) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        if (curRow != table.getSelectedRow()) {
            curRow = table.getSelectedRow();
        }
        int startPos = (scanCurrentRow) ? curRow : (curRow + searchMode);
        if (startPos < 0) {
            startPos = 0;
        }
        int endPos = table.getRowCount();
        if (searchMode < 0) {
            endPos = -1;
        }
        for (int i = startPos; i != endPos; i += searchMode) {
            LogRecord record = model.getRowData(table.convertRowIndexToModel(i));
            if (StringUtils.contains(record.getTag(), text)
                    || StringUtils.contains(record.getMessage(), text)) {
                setCurrentRow(i);
                return true;
            }
        }
        return false;
    }

    private void setCurrentRow(int i) {
        curRow = i;
        table.scrollRectToVisible(table.getCellRect(curRow, 0, false));
        table.getSelectionModel().setSelectionInterval(curRow, curRow);
        table.requestFocusInWindow();
    }

    public boolean isActive() {
        return !StringUtils.isBlank(text);
    }
}
