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
package org.bitbucket.mlopatkin.android.logviewer.filters;

import com.google.common.base.Preconditions;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Manages a list of filters and composes them based on their type. At first we hide anything that matches any of the
 * {@link FilteringMode#HIDE}. Then we hide anything that doesn't match at least one of the {@link
 * FilteringMode#SHOW} (if any).
 * <p/>
 * The order in which filters are added to/removed from FilterChain doesn't matter.
 */
public class FilterChain implements FilterCollection<Predicate<LogRecord>> {
    private final SetMultimap<FilteringMode, Predicate<LogRecord>> filters =
            MultimapBuilder.enumKeys(FilteringMode.class).hashSetValues().build();

    private boolean include(FilteringMode mode, LogRecord record) {
        Set<Predicate<LogRecord>> filtersForMode = filters.get(mode);
        if (filtersForMode.isEmpty()) {
            return mode.getDefaultResult();
        }
        for (Predicate<LogRecord> filter : filtersForMode) {
            if (filter.test(record)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addFilter(FilteringMode mode, Predicate<LogRecord> filter) {
        Preconditions.checkArgument(mode == FilteringMode.SHOW || mode == FilteringMode.HIDE);
        filters.put(mode, filter);
    }

    public boolean shouldShow(LogRecord record) {
        return !include(FilteringMode.HIDE, record) && include(FilteringMode.SHOW, record);
    }

    @Override
    public void removeFilter(FilteringMode mode, Predicate<LogRecord> filter) {
        filters.remove(mode, filter);
    }
}
