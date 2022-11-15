/*
 * Copyright 2022 the Andlogview authors
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

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.search.AbstractIndexCursor;
import name.mlopatkin.andlogview.search.SearchCursor;
import name.mlopatkin.andlogview.search.SearchDataModel;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;

import javax.swing.JTable;

public class LogTableSearchAdapter implements SearchDataModel<LogRecord, TablePosition> {
    private final JTable table;
    private final LogRecordTableModel logModel;

    public LogTableSearchAdapter(JTable table, LogRecordTableModel logModel) {
        this.table = table;
        this.logModel = logModel;
    }

    @Override
    public SearchCursor<LogRecord, TablePosition> newCursor() {
        return new Cursor();
    }

    private class Cursor extends AbstractIndexCursor<LogRecord, TablePosition> {
        @Override
        protected TablePosition indexToPosition(int index) {
            return TablePosition.fromViewIndex(table, index);
        }

        @Override
        protected int positionToIndex(TablePosition position) {
            return position.getViewIndex(table);
        }

        @Override
        protected LogRecord getValueAtIndex(int index) {
            return logModel.getRowData(table.convertRowIndexToModel(index));
        }

        @Override
        protected int size() {
            return table.getRowCount();
        }
    }
}
