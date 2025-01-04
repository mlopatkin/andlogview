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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogRecordHighlighter {
    private final List<ColoringFilter> filters;
    private final List<ColoringFilter> reversedView;

    public LogRecordHighlighter(FilterModel model) {
        filters = fetchFilters(model).collect(Collectors.toCollection(ArrayList::new));

        // TODO(mlopatkin) We're leaking the observer
        model.asObservable().addObserver(new FiltersChangeObserver(this::onFiltersChanged));
        reversedView = Lists.reverse(filters);
    }

    private static Stream<ColoringFilter> fetchFilters(FilterModel model) {
        return model.getFilters()
                .stream()
                .map(LogRecordHighlighter::transformFilter)
                .filter(Objects::nonNull);
    }

    private void onFiltersChanged(FilterModel model) {
        filters.clear();
        fetchFilters(model).forEach(filters::add);
    }

    private static @Nullable ColoringFilter transformFilter(Filter filter) {
        return (ColoringFilter) (FilteringMode.HIGHLIGHT.equals(filter.getMode())
                ? filter
                : null);
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
