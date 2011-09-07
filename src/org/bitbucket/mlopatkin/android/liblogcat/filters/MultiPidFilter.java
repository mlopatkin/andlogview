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

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

/**
 * Performs filtering based on the PID value of the record. If multiple pids are
 * specified in the filter it matches any of them.
 */
public class MultiPidFilter extends AbstractFilter implements LogRecordFilter {

    private Set<Integer> pids = new TreeSet<Integer>();

    public MultiPidFilter(int[] pids) {
        for (int pid : pids) {
            this.pids.add(pid);
        }
    }

    @Override
    public boolean include(LogRecord record) {
        return pids.contains(record.getPid());
    }

    @Override
    public String toString() {
        return "PID" + (pids.size() > 1 ? "s" : "") + ": " + StringUtils.join(pids, ", ");
    }

    @Override
    protected void dumpFilter(FilterData data) {
        data.pids = Collections.unmodifiableCollection(pids);
    }
}
