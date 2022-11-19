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

package name.mlopatkin.andlogview.search.logrecord;

import name.mlopatkin.andlogview.logmodel.Field;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.search.text.HighlightStrategy;
import name.mlopatkin.andlogview.search.text.TextHighlighter;

class ValueSearcher implements RowSearchStrategy {
    private final HighlightStrategy highlightStrategy;
    private final Field field;

    public ValueSearcher(HighlightStrategy highlightStrategy, Field field) {
        this.highlightStrategy = highlightStrategy;
        this.field = field;
    }

    @Override
    public boolean test(LogRecord record) {
        return highlightStrategy.test(getValue(record));
    }

    private String getValue(LogRecord record) {
        return String.valueOf(field.getValue(record));
    }

    @Override
    public void highlightColumn(LogRecord record, Field field, TextHighlighter columnHighlighter) {
        if (this.field.equals(field)) {
            highlightStrategy.highlightOccurences(getValue(record), columnHighlighter);
        }
    }
}
