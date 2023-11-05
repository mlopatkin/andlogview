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
package name.mlopatkin.andlogview.ui.logtable;

import name.mlopatkin.andlogview.logmodel.LogModel;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecord.Priority;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;

import java.awt.EventQueue;
import java.util.Date;

import javax.inject.Inject;
import javax.swing.table.AbstractTableModel;

@MainFrameScoped
public class LogRecordTableModel extends AbstractTableModel implements LogModel.Observer {
    private LogModel logModel = LogModel.empty();

    @Inject
    public LogRecordTableModel() {}

    public void setLogModel(LogModel logModel) {
        this.logModel.asObservable().removeObserver(this);
        this.logModel = logModel;
        this.logModel.asObservable().addObserver(this);
        fireTableDataChanged();
    }

    public Column getColumn(int columnIndex) {
        return Column.getByColumnIndex(columnIndex);
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public int getRowCount() {
        return logModel.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LogRecord record = getRowData(rowIndex);
        Object value = Column.getByColumnIndex(columnIndex).getValue(rowIndex, record);
        assert value != null;
        return value;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (Column.getByColumnIndex(columnIndex)) {
            case TIME -> Date.class;
            case PRIORITY -> Priority.class;
            default -> super.getColumnClass(columnIndex);
        };
    }

    public TableRow getRow(int row) {
        return new TableRow(row, getRowData(row));
    }

    public LogRecord getRowData(int row) {
        return logModel.getAt(row);
    }

    @Override
    public void onRecordsDiscarded(int oldSize) {
        assert EventQueue.isDispatchThread();
        if (oldSize > 0) {
            fireTableRowsDeleted(0, oldSize - 1);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return switch (Column.getByColumnIndex(columnIndex)) {
            case MESSAGE, PID, TID, TAG, APP_NAME -> true;
            default -> false;
        };
    }

    @Override
    public void onRecordsInserted(int position, int count) {
        int oldSize = getRowCount() - count;
        assert oldSize >= 0;
        fireTableRowsInserted(oldSize, getRowCount() - 1);
        // Notify about update only if we weren't only appending stuff, but inserted something between the existing
        // lines.
        if (position < oldSize) {
            fireTableRowsUpdated(position, getRowCount() - 1);
        }
    }
}
