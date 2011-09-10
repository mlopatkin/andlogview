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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;
import org.bitbucket.mlopatkin.utils.MyListUtils;

public class LogRecordTableModel extends AbstractTableModel {

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
    public synchronized int getRowCount() {
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

    public synchronized void addRecord(LogRecord record) {
        int pos = Collections.binarySearch(records, record);
        if (pos < 0) {
            pos = -(pos + 1);
        }
        records.add(pos, record);
        fireTableRowsInserted(pos, pos);
    }

    public synchronized void addRecordToEnd(LogRecord record) {
        records.add(record);
        fireTableRowsInserted(records.size() - 1, records.size() - 1);
    }

    public LogRecord getRowData(int row) {
        return records.get(row);
    }

    public synchronized void clear() {
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

    public void assign(List<LogRecord> copy) {
        records = copy;
        fireTableDataChanged();
    }

    public void append(List<LogRecord> newRecords) {
        assert EventQueue.isDispatchThread();
        int oldSize = records.size();
        int firstAffected = MyListUtils.mergeOrdered(records, newRecords);
        if (firstAffected < records.size()) {
            fireTableRowsInserted(oldSize, records.size() - 1);
            fireTableRowsUpdated(firstAffected, records.size() - 1);
        }
    }
}
