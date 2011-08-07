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

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordFilter;

public class FilterChain {

    private EnumMap<FilteringMode, Set<LogRecordFilter>> filters = new EnumMap<FilteringMode, Set<LogRecordFilter>>(
            FilteringMode.class);

    private boolean include(Set<LogRecordFilter> filters, LogRecord record) {
        for (LogRecordFilter filter : filters) {
            if (filter.include(record)) {
                return true;
            }

        }
        return false;
    }

    public void addFilter(FilteringMode mode, LogRecordFilter filter) {
        Set<LogRecordFilter> concreteFilters = filters.get(mode);
        if (concreteFilters == null) {
            concreteFilters = new HashSet<LogRecordFilter>();
            filters.put(mode, concreteFilters);
        }
        concreteFilters.add(filter);
    }

    public boolean checkFilter(FilteringMode mode, LogRecord record) {
        Set<LogRecordFilter> concreteFilters = filters.get(mode);
        if (concreteFilters == null) {
            return mode.getDefaultResult();
        }
        return include(concreteFilters, record);
    }

    public void removeFilter(LogRecordFilter filter) {
        for (Set<LogRecordFilter> concreteFilters : filters.values()) {
            concreteFilters.remove(filter);
        }
    }

}
