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
package name.mlopatkin.andlogview.filters;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import com.google.common.collect.Lists;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class LogRecordHighlighter implements FilterCollection<ColoringFilter> {
    private final List<ColoringFilter> filters = new ArrayList<>();
    private final List<ColoringFilter> reversedView = Lists.reverse(filters);

    @Override
    public Function<? super Filter, ? extends @Nullable ColoringFilter> createObserverTransformer() {
        return f -> (ColoringFilter) FilteringMode.HIGHLIGHT.filterMode().apply(f);
    }

    @Override
    public void addFilter(ColoringFilter filter) {
        filters.add(filter);
    }

    @Override
    public void removeFilter(ColoringFilter filter) {
        filters.remove(filter);
    }

    @Override
    public void replaceFilter(ColoringFilter oldFilter, ColoringFilter newFilter) {
        var pos = filters.indexOf(oldFilter);
        assert pos >= 0;
        filters.set(pos, newFilter);
    }

    public @Nullable Color getColor(LogRecord record) {
        for (var filter : reversedView) {
            if (filter.isEnabled() && filter.test(record)) {
                return filter.getHighlightColor();
            }
        }
        return null;
    }
}
