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
package org.bitbucket.mlopatkin.android.liblogcat.filters;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

/**
 * This is an OR-based filter for {@link AppNameFilter} and
 * {@link MultiPidFilter}.
 */
public class AppNameOrPidFilter extends AbstractFilter implements LogRecordFilter {

    private AppNameFilter appNameFilter;
    private MultiPidFilter multiPidFilter;

    private AppNameOrPidFilter(AppNameFilter appNameFilter, MultiPidFilter multiPidFilter) {
        this.appNameFilter = appNameFilter;
        this.multiPidFilter = multiPidFilter;

    }
    @Override
    protected void dumpFilter(FilterData data) {
        appNameFilter.dumpFilter(data);
        multiPidFilter.dumpFilter(data);
    }

    @Override
    public boolean include(LogRecord record) {
        return appNameFilter.include(record) || multiPidFilter.include(record);
    }

    @Override
    public String toString() {
        return appNameFilter.toString() + " OR " + multiPidFilter.toString();
    }

    public static LogRecordFilter or(AppNameFilter appNameFilter, MultiPidFilter multiPidFilter) {
        if (appNameFilter != null && multiPidFilter != null) {
            return new AppNameOrPidFilter(appNameFilter, multiPidFilter);
        }
        if (appNameFilter != null) {
            return appNameFilter;
        }
        if (multiPidFilter != null) {
            return multiPidFilter;
        }
        return null;
    }

}
