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

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

import java.awt.Color;
import java.util.Map;
import java.util.Map.Entry;

class LogRecordHighlighter extends AbstractFilterCollection<ColoringFilter> {

    private class FilterInfo {

        Color color;
        boolean enabled = true;

        public FilterInfo(Color color) {
            this.color = color;
        }
    }

    private Map<Predicate<LogRecord>, FilterInfo> filterColors = Maps.newLinkedHashMap();

    @Override
    public void addFilter(FilteringMode mode, ColoringFilter filter) {
        filterColors.put(filter, new FilterInfo(filter.getHighlightColor()));
    }

    @Override
    public void removeFilter(FilteringMode mode, ColoringFilter filter) {
        filterColors.remove(filter);
    }

    @Override
    public void setFilterEnabled(FilteringMode mode, ColoringFilter filter, boolean enable) {
        filterColors.get(filter).enabled = enable;
    }

    public Color getColor(LogRecord record) {
        Color result = null;
        for (Entry<Predicate<LogRecord>, FilterInfo> entry : filterColors.entrySet()) {
            Predicate<LogRecord> filter = entry.getKey();
            FilterInfo info = entry.getValue();
            if (info.enabled && filter.apply(record)) {
                result = info.color;
            }
        }
        return result;
    }
}
