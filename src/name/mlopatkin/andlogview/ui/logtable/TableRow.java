/*
 * Copyright 2020 Mikhail Lopatkin
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

import name.mlopatkin.andlogview.liblogcat.LogRecord;

import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * This class represents a single row in the {@link LogRecordTableModel}.
 */
public final class TableRow {
    private final int rowIndex;
    private final LogRecord record;

    /**
     * Creates a TableRow
     * @param rowIndex the model row index
     * @param record the data
     */
    public TableRow(int rowIndex, LogRecord record) {
        this.rowIndex = rowIndex;
        this.record = record;
    }

    /**
     * @return a model index of this row
     */
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * @return a data for this row
     */
    public LogRecord getRecord() {
        return record;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TableRow)) {
            return false;
        }
        TableRow tableRow = (TableRow) o;
        return rowIndex == tableRow.rowIndex && record.equals(tableRow.record);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowIndex, record);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("rowIndex", rowIndex)
                .add("record", record)
                .toString();
    }
}
