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

import javax.swing.table.AbstractTableModel;

public class PinRecordsTableModel extends AbstractTableModel {

    private static final int COLUMNS_COUNT = 7;

    public static final int COLUMN_ID = 6;
    public static final int COLUMN_TIME = 0;
    public static final int COLUMN_PID = 1;
    public static final int COLUMN_TID = 2;
    public static final int COLUMN_PRIORITY = 3;
    public static final int COLUMN_TAG = 4;
    public static final int COLUMN_MSG = 5;

    private LogRecordTableModel parentModel;

    public PinRecordsTableModel(LogRecordTableModel parentModel) {
        this.parentModel = parentModel;
    }

    @Override
    public int getColumnCount() {
        return COLUMNS_COUNT;
    }

    @Override
    public int getRowCount() {
        return parentModel.getRowCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex < parentModel.getColumnCount()) {
            return parentModel.getValueAt(rowIndex, columnIndex);
        }
        switch (columnIndex) {
        case COLUMN_ID:
            return rowIndex;
        default:
            throw new IllegalArgumentException("Incorrect column number");
        }
    }

    public LogRecordTableModel getParent() {
        return parentModel;
    }
}
