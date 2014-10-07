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

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;

public class FilterChain {

    private EnumMap<FilteringMode, Set<Predicate<LogRecord>>> filters = Maps
            .newEnumMap(FilteringMode.class);

    private boolean include(Set<Predicate<LogRecord>> filters, LogRecord record) {
        for (Predicate<LogRecord> filter : filters) {
            if (filter.apply(record)) {
                return true;
            }

        }
        return false;
    }

    public void addFilter(FilteringMode mode, Predicate<LogRecord> filter) {
        Set<Predicate<LogRecord>> concreteFilters = filters.get(mode);
        if (concreteFilters == null) {
            concreteFilters = new HashSet<Predicate<LogRecord>>();
            filters.put(mode, concreteFilters);
        }
        concreteFilters.add(filter);
    }

    public boolean checkFilter(FilteringMode mode, LogRecord record) {
        Set<Predicate<LogRecord>> concreteFilters = filters.get(mode);
        if (concreteFilters == null || concreteFilters.isEmpty()) {
            return mode.getDefaultResult();
        }
        return include(concreteFilters, record);
    }

    public void removeFilter(Predicate<LogRecord> filter) {
        for (Set<Predicate<LogRecord>> concreteFilters : filters.values()) {
            concreteFilters.remove(filter);
        }
    }

}
