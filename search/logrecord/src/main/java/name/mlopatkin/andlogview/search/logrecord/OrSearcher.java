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
import name.mlopatkin.andlogview.search.text.TextHighlighter;

import com.google.common.collect.ImmutableList;

import java.util.List;

class OrSearcher implements RowSearchStrategy {
    private final List<RowSearchStrategy> searchers;

    public OrSearcher(List<? extends RowSearchStrategy> searchers) {
        this.searchers = ImmutableList.copyOf(searchers);
    }

    @Override
    public boolean test(LogRecord record) {
        for (var s : searchers) {
            if (s.test(record)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void highlightColumn(LogRecord record, Field<?> field, TextHighlighter columnHighlighter) {
        for (var s : searchers) {
            s.highlightColumn(record, field, columnHighlighter);
        }
    }
}
