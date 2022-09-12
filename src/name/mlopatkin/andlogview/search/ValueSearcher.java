/*
 * Copyright 2013 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.search;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.ui.logtable.Column;

class ValueSearcher implements RowSearchStrategy {
    private final HighlightStrategy highlightStrategy;
    private final Column column;

    public ValueSearcher(HighlightStrategy highlightStrategy, Column column) {
        this.highlightStrategy = highlightStrategy;
        this.column = column;
    }

    @Override
    public boolean isRowMatched(LogRecord record) {
        return highlightStrategy.test(getValue(record));
    }

    private String getValue(LogRecord record) {
        return String.valueOf(column.getValue(0, record));
    }

    @Override
    public void highlightColumn(LogRecord record, int columnIndex, TextHighlighter columnHighlighter) {
        if (columnIndex == column.getIndex()) {
            highlightStrategy.highlightOccurences(getValue(record), columnHighlighter);
        }
    }
}
