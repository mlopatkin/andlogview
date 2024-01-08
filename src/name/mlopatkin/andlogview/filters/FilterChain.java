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

import com.google.common.base.Preconditions;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

/**
 * Manages a list of filters and composes them based on their type. At first, we hide anything that matches any of the
 * {@link FilteringMode#HIDE}. Then we hide anything that doesn't match at least one of the {@link
 * FilteringMode#SHOW} (if any).
 * <p/>
 * The order in which filters are added to/removed from FilterChain doesn't matter.
 */
public class FilterChain implements FilterCollection<Filter> {
    private final SetMultimap<FilteringMode, Filter> filters =
            MultimapBuilder.enumKeys(FilteringMode.class).hashSetValues().build();

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

    @Override
    public void addFilter(FilteringMode mode, Filter filter) {
        Preconditions.checkArgument(isSupportedMode(mode));
        filters.put(mode, filter);
    }

    public boolean shouldShow(LogRecord record) {
        return !include(FilteringMode.HIDE, record) && include(FilteringMode.SHOW, record);
    }

    @Override
    public void removeFilter(FilteringMode mode, Filter filter) {
        filters.remove(mode, filter);
    }

    private boolean isSupportedMode(FilteringMode mode) {
        return mode == FilteringMode.SHOW || mode == FilteringMode.HIDE;
    }
}
