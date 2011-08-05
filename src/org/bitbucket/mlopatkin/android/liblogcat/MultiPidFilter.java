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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class MultiPidFilter implements LogRecordFilter {

    private Set<Integer> pids = new HashSet<Integer>();

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
        return "PIDs: " + StringUtils.join(pids, ", ");
    }
}
