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
package org.bitbucket.mlopatkin.android.liblogcat;

import java.util.ArrayList;
import java.util.List;

/**
 * This filter performs AND operation upon all included filters.
 * 
 */
public class ComposeFilter extends AbstractFilter implements LogRecordFilter {

    private List<LogRecordFilter> filters = new ArrayList<LogRecordFilter>();

    public ComposeFilter(LogRecordFilter a, LogRecordFilter b) {
        filters.add(a);
        filters.add(b);
    }

    public ComposeFilter append(LogRecordFilter filter) {
        filters.add(filter);
        return this;
    }

    @Override
    public boolean include(LogRecord record) {
        for (LogRecordFilter filter : filters) {
            if (!filter.include(record)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Composite:<br>");
        for (LogRecordFilter filter : filters) {
            builder.append(filter).append("<br>");
        }
        return builder.toString();
    }

    @Override
    protected void dumpFilter(FilterData data) {
        for (LogRecordFilter filter : filters) {
            if (filter instanceof AbstractFilter) {
                ((AbstractFilter) filter).dumpFilter(data);
            }
        }
    }
}
