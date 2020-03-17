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

import com.google.common.collect.Lists;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

class LogRecordHighlighter implements FilterCollection<ColoringFilter> {

    private static class FilterInfo {
        public ColoringFilter filter;
        public boolean isEnabled;

        FilterInfo(ColoringFilter filter, boolean isEnabled) {
            this.filter = filter;
            this.isEnabled = isEnabled;
        }
    }

    private final List<FilterInfo> filters = new ArrayList<>();
    private final List<FilterInfo> reversedView = Lists.reverse(filters);

    @Override
    public void addFilter(FilteringMode mode, ColoringFilter filter) {
        filters.add(new FilterInfo(filter, true));
    }

    @Override
    public void removeFilter(FilteringMode mode, ColoringFilter filter) {
        filters.remove(findInfoForFilter(filter));
    }

    @Override
    public void setFilterEnabled(FilteringMode mode, ColoringFilter filter, boolean enable) {
        FilterInfo info = findInfoForFilter(filter);
        assert info != null;
        info.isEnabled = enable;
    }

    @Override
    public void replaceFilter(FilteringMode mode, ColoringFilter oldFilter, ColoringFilter newFilter) {
        FilterInfo info = findInfoForFilter(oldFilter);
        assert info != null;
        info.filter = newFilter;
    }

    public Color getColor(LogRecord record) {
        for (FilterInfo info : reversedView) {
            if (info.isEnabled && info.filter.apply(record)) {
                return info.filter.getHighlightColor();
            }
        }
        return null;
    }

    @Nullable
    private FilterInfo findInfoForFilter(ColoringFilter filter) {
        for (FilterInfo item : filters) {
            if (item.filter.equals(filter)) {
                return item;
            }
        }
        return null;
    }
}
