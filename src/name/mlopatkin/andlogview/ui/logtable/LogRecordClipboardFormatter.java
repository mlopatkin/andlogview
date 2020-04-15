/*
 * Copyright 2018 Mikhail Lopatkin
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

import java.io.IOException;

/**
 * Formatter that converts log records into text suitable for pasting into spreadsheet apps.
 */
public class LogRecordClipboardFormatter {
    private final ColumnTogglesModel availableColumns;
    private final ColumnOrder order;

    public LogRecordClipboardFormatter(ColumnTogglesModel availableColumns, ColumnOrder order) {
        this.availableColumns = availableColumns;
        this.order = order;
    }

    public <T extends Appendable> T formatLogRecord(int rowIndex, LogRecord logRecord, T dest) throws IOException {
        boolean first = true;
        for (Column c : order) {
            if (availableColumns.isColumnAvailable(c)) {
                if (!first) {
                    dest.append('\t');
                }
                dest.append(c.getStrValue(rowIndex, logRecord));
                first = false;
            }
        }
        return dest;
    }
}
