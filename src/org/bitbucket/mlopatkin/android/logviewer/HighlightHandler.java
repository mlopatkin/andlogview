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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.filters.LogRecordFilter;
import org.bitbucket.mlopatkin.android.logviewer.FilterController.FilteringModeHandler;

class HighlightHandler implements FilteringModeHandler<Color> {
    private class FilterInfo {
        Color color;
        boolean enabled = true;

        public FilterInfo(Color color) {
            this.color = color;
        }
    }

    private Map<LogRecordFilter, FilterInfo> filterColors = new HashMap<LogRecordFilter, FilterInfo>();

    @Override
    public void addFilter(FilteringMode mode, LogRecordFilter filter, Color data) {
        filterColors.put(filter, new FilterInfo(Configuration.ui.highlightColor()));
    }

    @Override
    public void removeFilter(FilteringMode mode, LogRecordFilter filter) {
        filterColors.remove(filter);
    }

    @Override
    public void enableFilter(FilteringMode mode, LogRecordFilter filter) {
        filterColors.get(filter).enabled = true;
    }

    @Override
    public void disableFilter(FilteringMode mode, LogRecordFilter filter) {
        filterColors.get(filter).enabled = false;
    }

    public Color getColor(LogRecord record) {
        for (Entry<LogRecordFilter, FilterInfo> entry : filterColors.entrySet()) {
            LogRecordFilter filter = entry.getKey();
            FilterInfo info = entry.getValue();
            if (info.enabled && filter.include(record)) {
                return info.color;
            }
        }
        return null;
    }
}