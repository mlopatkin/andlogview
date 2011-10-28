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

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;
import org.bitbucket.mlopatkin.utils.MyListUtils;

public class LogRecordTableModel extends AbstractTableModel implements
        BatchRecordsReceiver<LogRecord> {

    private List<LogRecord> records;

    private static final int COLUMNS_COUNT = 7;

    public static final int COLUMN_TIME = 0;
    public static final int COLUMN_PID = 1;
    public static final int COLUMN_TID = 2;
    public static final int COLUMN_PRIORITY = 3;
    public static final int COLUMN_TAG = 4;
    public static final int COLUMN_MSG = 5;
    public static final int COLUMN_LINE = 6;

    public LogRecordTableModel() {
        this.records = new ArrayList<LogRecord>();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS_COUNT;
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LogRecord record = records.get(rowIndex);
        switch (columnIndex) {
        case COLUMN_TIME:
            return record.getTime();
        case COLUMN_PID:
            return record.getPid();
        case COLUMN_TID:
            return record.getTid();
        case COLUMN_PRIORITY:
            return record.getPriority();
        case COLUMN_TAG:
            return record.getTag();
        case COLUMN_MSG:
            return record.getMessage();
        case COLUMN_LINE:
            return rowIndex + 1;
        default:
            throw new IllegalArgumentException("Incorrect column number");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case COLUMN_TIME:
            return Date.class;
        case COLUMN_PRIORITY:
            return Priority.class;
        default:
            return super.getColumnClass(columnIndex);
        }
    }

    @Override
    public void addRecord(LogRecord record) {
        assert EventQueue.isDispatchThread();
        int pos = MyListUtils.getUpperBoundPos(records, record);
        records.add(pos, record);
        fireTableRowsInserted(pos, pos);
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
        switch (columnIndex) {
        case COLUMN_MSG:
        case COLUMN_PID:
        case COLUMN_TID:
        case COLUMN_TAG:
            return true;
        default:
            return false;
        }
    }

    @Override
    public void setRecords(List<LogRecord> copy) {
        assert EventQueue.isDispatchThread();
        records = copy;
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
