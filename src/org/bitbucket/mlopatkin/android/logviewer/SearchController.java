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

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;
import org.bitbucket.mlopatkin.android.logviewer.search.RowSearchStrategy;
import org.bitbucket.mlopatkin.android.logviewer.search.RowSearchStrategyFactory;
import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingRendererTable;

public class SearchController {

    private DecoratingRendererTable table;
    private LogRecordTableModel model;
    private int curRow;
    private SearchResultsHighlightCellRenderer renderer = new SearchResultsHighlightCellRenderer();

    private RowSearchStrategy strategy;

    public SearchController(DecoratingRendererTable table, LogRecordTableModel model) {
        this.table = table;
        this.model = model;
        table.addDecorator(renderer);
    }

    private static final int MODE_FORWARD = 1;
    private static final int MODE_BACKWARD = -1;

    public boolean startSearch(String text) throws RequestCompilationException {
        strategy = RowSearchStrategyFactory.compile(text);
        if (strategy == null) {
            renderer.setHighlightStrategy(null);
            table.repaint();
            return false;
        }
        renderer.setHighlightStrategy(strategy);
        table.repaint();
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
        if (!isActive()) {
            return false;
        }
        if (curRow != table.getSelectedRow()) {
            curRow = table.getSelectedRow();
        }
        int startPos = (scanCurrentRow) ? curRow : (curRow + searchMode);
        if (startPos < 0) {
            // no more rows to search
            return false;
        }
        int endPos = table.getRowCount();
        if (searchMode < 0) {
            endPos = -1;
        }
        for (int i = startPos; i != endPos; i += searchMode) {
            LogRecord record = model.getRowData(table.convertRowIndexToModel(i));
            if (strategy.isRowMatched(record)) {
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
        return strategy != null;
    }
}
