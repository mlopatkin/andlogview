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

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Manages a list of filters and composes them based on their type. At first, we hide anything that matches any of the
 * {@link FilteringMode#HIDE}. Then we hide anything that doesn't match at least one of the {@link
 * FilteringMode#SHOW} (if any).
 * <p/>
 * The order in which filters are added to/removed from FilterChain doesn't matter.
 */
public class FilterChain {
    private final SetMultimap<FilteringMode, PredicateFilter> filters;

    public FilterChain(FilterModel model) {
        filters = fetchFilters(model).collect(Multimaps.toMultimap(
                Filter::getMode,
                Function.identity(),
                () -> MultimapBuilder.enumKeys(FilteringMode.class).hashSetValues().build()
        ));

        // TODO(mlopatkin) We leak the observer
        // TODO(mlopatkin) the model supplied to the observer is invalid when change comes from the top-level filter
        //  list
        model.asObservable().addObserver(new FiltersChangeObserver(ignored -> onFiltersChanged(model)));
    }

    private void onFiltersChanged(FilterModel model) {
        filters.clear();
        fetchFilters(model).forEach(filter -> filters.put(filter.getMode(), filter));
    }

    private boolean include(FilteringMode mode, LogRecord record) {
        var filtersForMode = filters.get(mode);
        if (filtersForMode.isEmpty()) {
            return mode.getDefaultResult();
        }
        for (var filter : filtersForMode) {
            if (filter.test(record)) {
                return true;
            }
        }
        return false;
    }

    private static Stream<PredicateFilter> fetchFilters(FilterModel model) {
        return model.getFilters()
                .stream()
                .filter(Filter::isEnabled)
                .map(FilterChain::transformFilter)
                .filter(Objects::nonNull);
    }

    private static @Nullable PredicateFilter transformFilter(Filter filter) {
        var mode = filter.getMode();
        if (FilteringMode.SHOW.equals(mode) || FilteringMode.HIDE.equals(mode)) {
            return (PredicateFilter) filter;
        }
        return null;
    }

    public boolean shouldShow(LogRecord record) {
        return !include(FilteringMode.HIDE, record) && include(FilteringMode.SHOW, record);
    }
}
