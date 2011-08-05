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

import java.util.HashSet;
import java.util.Set;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordFilter;

public class FilterChain {

    private Set<LogRecordFilter> highlightFilters = new HashSet<LogRecordFilter>();
    private Set<LogRecordFilter> showFilters = new HashSet<LogRecordFilter>();

    private boolean include(Set<LogRecordFilter> filters, LogRecord record) {
        for (LogRecordFilter filter : filters) {
            if (filter.include(record)) {
                return true;
            }

        }
        return false;
    }

    public boolean shouldHighlight(LogRecord record) {
        return include(highlightFilters, record);
    }

    public boolean shouldShow(LogRecord record) {
        if (showFilters.isEmpty()) {
            return true;
        }
        return include(showFilters, record);
    }

    public void addHighlightFilter(LogRecordFilter filter) {
        highlightFilters.add(filter);
    }

    public void addHideFilter(LogRecordFilter filter) {
        showFilters.add(filter);
    }

    public void removeFilter(LogRecordFilter filter) {
        highlightFilters.remove(filter);
        showFilters.remove(filter);
    }

}
