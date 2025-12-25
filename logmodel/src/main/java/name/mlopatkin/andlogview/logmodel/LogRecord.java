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
package name.mlopatkin.andlogview.logmodel;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * This class contains all available log record data like timestamp, tag,
 * message, etc.
 */
public class LogRecord {

    private static final Comparator<@Nullable Buffer> NULL_SAFE_BUFFER_COMPARATOR =
            Comparator.nullsFirst(Comparator.naturalOrder());
    private static final Comparator<@Nullable Timestamp> NULL_SAFE_TIME_COMPARATOR =
            Comparator.nullsFirst(Comparator.naturalOrder());

    public static final Comparator<LogRecord> LEGACY_COMPARATOR = (a, b) -> ComparisonChain.start()
            .compare(a.getTime(), b.getTime(), NULL_SAFE_TIME_COMPARATOR)
            .compare(a.getBuffer(), b.getBuffer(), NULL_SAFE_BUFFER_COMPARATOR)
            .result();

    public enum Priority {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL;

        public static Priority fromChar(String next) {
            next = next.trim();
            for (Priority val : values()) {
                if (val.getLetter().equalsIgnoreCase(next)) {
                    return val;
                }
            }
            throw new IllegalArgumentException("Symbol '" + next + "' doesn't correspond to valid priority value");
        }

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
        CRASH("Crash"),
        KERNEL("Kernel");

        private final String name;

        public String getCaption() {
            return name;
        }

        Buffer(String name) {
            this.name = name;
        }
    }

    public static final int NO_ID = -1;

    private final SequenceNumber seqNo;
    private final @Nullable Timestamp time;
    private final int pid;
    private final int tid;
    private final Priority priority;
    private final String tag;
    private final String message;
    private final @Nullable Buffer buffer;
    private final @Nullable String appName;

    LogRecord(
            SequenceNumber seqNo,
            @Nullable Timestamp time,
            int pid,
            int tid,
            @Nullable String appName,
            Priority priority,
            String tag,
            String message,
            @Nullable Buffer buffer) {
        this.seqNo = seqNo;
        this.time = time;
        this.pid = pid;
        this.tid = tid;
        this.appName = appName != null ? CharMatcher.whitespace().trimFrom(appName) : null;
        this.priority = priority;
        this.tag = tag;
        this.message = message;
        this.buffer = buffer;
    }

    public SequenceNumber getSeqNo() {
        return seqNo;
    }

    public @Nullable Timestamp getTime() {
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

    public boolean hasAppName() {
        return appName != null;
    }

    public String getAppName() {
        return appName != null ? appName : "";
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("[").append(buffer != null ? buffer.name() : "?").append("] ");
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
     * Returns a copy of this record with sequence number changed to the integer value.
     *
     * @param seqNo the new sequence number
     * @return the new record
     */
    public LogRecord withSequenceNumber(int seqNo) {
        return new LogRecord(new SequenceNumber(seqNo, buffer), time, pid, tid, appName, priority, tag, message,
                buffer);
    }

    /**
     * Returns a copy of this record with timestamp set to the given timestamp.
     *
     * @param timestamp the new timestamp
     * @return the new record
     */
    public LogRecord withTimestamp(Timestamp timestamp) {
        return new LogRecord(seqNo, Preconditions.checkNotNull(timestamp), pid, tid, appName, priority, tag, message,
                buffer);
    }

    /**
     * Returns a copy of this record without a timestamp.
     *
     * @return the new record
     */
    public LogRecord withoutTimestamp() {
        return new LogRecord(seqNo, null, pid, tid, appName, priority, tag, message, buffer);
    }

    /**
     * Returns a copy of this record with PID set to the given PID.
     *
     * @param pid the new PID
     * @return the new record
     */
    public LogRecord withPid(int pid) {
        return new LogRecord(seqNo, time, pid, tid, appName, priority, tag, message, buffer);
    }

    /**
     * Returns a copy of this record with PID set to {@code NO_ID}.
     *
     * @return the new record
     */
    public LogRecord withoutPid() {
        return new LogRecord(seqNo, time, NO_ID, tid, appName, priority, tag, message, buffer);
    }

    /**
     * Returns a copy of this record with TID set to the given TID.
     *
     * @param tid the new TID
     * @return the new record
     */
    public LogRecord withTid(int tid) {
        return new LogRecord(seqNo, time, pid, tid, appName, priority, tag, message, buffer);
    }

    /**
     * Returns a copy of this record with TID set to {@code NO_ID}.
     *
     * @return the new record
     */
    public LogRecord withoutTid() {
        return new LogRecord(seqNo, time, pid, NO_ID, appName, priority, tag, message, buffer);
    }

    /**
     * Returns a copy of this record with app name set to the given string.
     *
     * @param appName the new app name
     * @return the new record
     */
    public LogRecord withAppName(String appName) {
        return new LogRecord(seqNo, time, pid, tid, Preconditions.checkNotNull(appName), priority, tag, message,
                buffer);
    }

    /**
     * Returns a copy of this record without an app name.
     *
     * @return the new record
     */
    public LogRecord withoutAppName() {
        return new LogRecord(seqNo, time, pid, tid, null, priority, tag, message, buffer);
    }

    /**
     * Returns a copy of this record with priority set to the given priority.
     *
     * @param priority the new priority
     * @return the new record
     */
    public LogRecord withPriority(Priority priority) {
        return new LogRecord(seqNo, time, pid, tid, appName, priority, tag, message, buffer);
    }

    /**
     * Returns a copy of this record with tag set to the given string.
     *
     * @param tag the new tag
     * @return the new record
     */
    public LogRecord withTag(String tag) {
        return new LogRecord(seqNo, time, pid, tid, appName, priority, tag, message, buffer);
    }

    /**
     * Returns a copy of this record with message set to the given string.
     *
     * @param message the new message
     * @return the new record
     */
    public LogRecord withMessage(String message) {
        return new LogRecord(seqNo, time, pid, tid, appName, priority, tag, message, buffer);
    }

    /**
     * Returns a copy of this record with buffer set to the given buffer.
     *
     * @param buffer the new buffer
     * @return the new record
     */
    public LogRecord withBuffer(Buffer buffer) {
        return new LogRecord(seqNo, time, pid, tid, appName, priority, tag, message,
                Preconditions.checkNotNull(buffer));
    }

    /**
     * Returns a copy of this record without a buffer.
     *
     * @return the new record
     */
    public LogRecord withoutBuffer() {
        return new LogRecord(seqNo, time, pid, tid, appName, priority, tag, message, null);
    }
}
