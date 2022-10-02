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

import name.mlopatkin.andlogview.logmodel.BatchRecordsReceiver;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecord.Priority;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.utils.MyListUtils;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.swing.table.AbstractTableModel;

@MainFrameScoped
public class LogRecordTableModel extends AbstractTableModel implements BatchRecordsReceiver<LogRecord> {
    private List<LogRecord> records;

    @Inject
    public LogRecordTableModel() {
        this.records = new ArrayList<>();
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LogRecord record = records.get(rowIndex);
        Object value = Column.getByColumnIndex(columnIndex).getValue(rowIndex, record);
        assert value != null;
        return value;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (Column.getByColumnIndex(columnIndex)) {
            case TIME:
                return Date.class;
            case PRIORITY:
                return Priority.class;
            default:
                return super.getColumnClass(columnIndex);
        }
    }

    public TableRow getRow(int row) {
        return new TableRow(row, getRowData(row));
    }

    public LogRecord getRowData(int row) {
        return records.get(row);
    }

    public void clear() {
        assert EventQueue.isDispatchThread();
        int lastRow = records.size() - 1;
        records.clear();
        if (lastRow >= 0) {
            fireTableRowsDeleted(0, lastRow);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (Column.getByColumnIndex(columnIndex)) {
            case MESSAGE:
            case PID:
            case TID:
            case TAG:
            case APP_NAME:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void setRecords(List<LogRecord> copy) {
        assert EventQueue.isDispatchThread();
        records = new ArrayList<>(copy);
        fireTableDataChanged();
    }

    @Override
    public void addRecords(List<LogRecord> newRecords) {
        assert EventQueue.isDispatchThread();
        int oldSize = records.size();
        int firstAffected = MyListUtils.mergeOrdered(records, newRecords);
        if (firstAffected < records.size()) {
            // we need to do two fireXXX because JTable machinery is very
            // sensitive to the number of inserted rows
            // however to get all rows displayed properly we update all possibly
            // affected rows
            fireTableRowsInserted(oldSize, records.size() - 1);
            fireTableRowsUpdated(firstAffected, records.size() - 1);
        }
    }
}
