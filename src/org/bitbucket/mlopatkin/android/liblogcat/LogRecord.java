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

import java.util.Date;

public class LogRecord {
    public enum Priority {
        VERBOSE, DEBUG, INFO, WARN, ERROR, FATAL;

        String getLetter() {
            return toString().substring(0, 1);
        }
    }

    private Date time;
    private int pid;
    private int tid;
    private Priority priority;
    private String tag;
    private String message;

    public LogRecord(Date time, int pid, int tid, Priority priority, String tag, String message) {
        this.time = time;
        this.pid = pid;
        this.tid = tid;
        this.priority = priority;
        this.tag = tag;
        this.message = message;
    }

    public Date getTime() {
        return time;
    }

    public int getPid() {
        return pid;
    }

    public int getTid() {
        return tid;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getTag() {
        return tag;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(TimeFormatUtils.convertTimeToString(time)).append('\t');
        b.append(pid).append('\t');
        b.append(tid).append('\t');
        b.append(priority.getLetter()).append('\t');
        b.append(tag).append('\t');
        b.append(message);
        return b.toString();
    }

}
