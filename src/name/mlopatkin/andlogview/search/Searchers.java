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

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;

class OrSearcher implements RowSearchStrategy {
    private final List<RowSearchStrategy> searchers;

    public OrSearcher(RowSearchStrategy... searchers) {
        this.searchers = Arrays.asList(searchers);
    }

    public OrSearcher(List<? extends RowSearchStrategy> searchers) {
        this.searchers = ImmutableList.copyOf(searchers);
    }

    @Override
    public boolean isRowMatched(LogRecord record) {
        for (RowSearchStrategy s : searchers) {
            if (s.isRowMatched(record)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void highlightColumn(LogRecord record, int columnIndex, TextHighlighter columnHighlighter) {
        for (RowSearchStrategy s : searchers) {
            s.highlightColumn(record, columnIndex, columnHighlighter);
        }
    }
}
