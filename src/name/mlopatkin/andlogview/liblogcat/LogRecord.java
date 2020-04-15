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
package name.mlopatkin.andlogview.liblogcat;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Comparator;
import java.util.Date;

/**
 * This class contains all available log record data like timestamp, tag,
 * message, etc.
 */
public class LogRecord implements Comparable<LogRecord> {

    private static final Comparator<@Nullable Buffer> NULL_SAFE_BUFFER_COMPARATOR =
            Comparator.nullsFirst(Comparator.naturalOrder());
    private static final Comparator<@Nullable Date> NULL_SAFE_DATE_COMPARATOR =
            Comparator.nullsFirst(Comparator.naturalOrder());

    public enum Priority {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL;

        public String getLetter() {
            return toString().substring(0, 1);
        }

        public static final Priority LOWEST = VERBOSE;
    }

    public enum Buffer {
        MAIN("Main"),
        SYSTEM("System"),
        RADIO("Radio"),
        EVENTS("Events"),
        CRASH("Crash");

        private final String name;

        public String getCaption() {
            return name;
        }

        Buffer(String name) {
            this.name = name;
        }
    }

    public static final int NO_ID = -1;

    private final @Nullable Date time;
    private final int pid;
    private final int tid;
    private final Priority priority;
    private final String tag;
    private final String message;
    private final @Nullable Buffer buffer;
    private final String appName;

    public LogRecord(@Nullable Date time, int pid, int tid, @Nullable String appName, Priority priority, String tag,
            String message) {
        this(time, pid, tid, appName, priority, tag, message, null);
    }

    public LogRecord(@Nullable Date time, int pid, int tid, @Nullable String appName, Priority priority, String tag,
            String message, @Nullable Buffer buffer) {
        this.time = time;
        this.pid = pid;
        this.tid = tid;
        this.appName = CharMatcher.whitespace().trimFrom(Strings.nullToEmpty(appName));
        this.priority = priority;
        this.tag = tag;
        this.message = message;
        this.buffer = buffer;
    }

    public @Nullable Date getTime() {
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

    public @Nullable Buffer getBuffer() {
        return buffer;
    }

    public String getAppName() {
        return appName;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (time != null) {
            b.append(TimeFormatUtils.convertTimeToString(time)).append('\t');
        }
        if (pid != NO_ID) {
            b.append(pid).append('\t');
        }
        if (tid != NO_ID) {
            b.append(tid).append('\t');
        }
        if (priority != null) {
            b.append(priority.getLetter()).append('\t');
        }
        if (tag != null) {
            b.append(tag).append('\t');
        }
        if (message != null) {
            b.append(message);
        }
        return b.toString();
    }

    /**
     * Performs timestamp comparison in ascending order.
     */
    @Override
    public int compareTo(LogRecord o) {
        return ComparisonChain.start()
                .compare(getTime(), o.getTime(), NULL_SAFE_DATE_COMPARATOR)
                .compare(getBuffer(), o.getBuffer(), NULL_SAFE_BUFFER_COMPARATOR)
                .result();
    }
}
