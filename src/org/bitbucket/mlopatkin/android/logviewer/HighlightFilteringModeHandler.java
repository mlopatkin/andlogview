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
package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.Color;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.FilterController.FilteringModeHandler;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;

class HighlightFilteringModeHandler implements FilteringModeHandler<Color> {

    private class FilterInfo {

        Color color;
        boolean enabled = true;

        public FilterInfo(Color color) {
            this.color = color;
        }
    }

    private Map<Predicate<LogRecord>, FilterInfo> filterColors = Maps.newLinkedHashMap();

    @Override
    public void addFilter(FilteringMode mode, Predicate<LogRecord> filter, Color data) {
        filterColors.put(filter, new FilterInfo(data));
    }

    @Override
    public void removeFilter(FilteringMode mode, Predicate<LogRecord> filter) {
        filterColors.remove(filter);
    }

    @Override
    public void enableFilter(FilteringMode mode, Predicate<LogRecord> filter) {
        filterColors.get(filter).enabled = true;
    }

    @Override
    public void disableFilter(FilteringMode mode, Predicate<LogRecord> filter) {
        filterColors.get(filter).enabled = false;
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

    @Override
    public Color getData(FilteringMode mode, Predicate<LogRecord> filter) {
        return filterColors.get(filter).color;
    }
}