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

package org.bitbucket.mlopatkin.android.logviewer.search;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

abstract class ValueSearcher implements RowSearchStrategy {

    private final HighlightStrategy highlightStrategy;
    private final int columnIndex;

    public ValueSearcher(HighlightStrategy highlightStrategy, int columnIndex) {
        this.highlightStrategy = highlightStrategy;
        this.columnIndex = columnIndex;
    }

    @Override
    public boolean isRowMatched(LogRecord record) {
        return highlightStrategy.isStringMatched(getValue(record));
    }

    protected abstract String getValue(LogRecord record);

    @Override
    public void highlightColumn(LogRecord record, int columnIndex, TextHighlighter columnHighlighter) {
        if (columnIndex == this.columnIndex) {
            highlightStrategy.highlightOccurences(getValue(record), columnHighlighter);
        }
    }

}
