/*
 * Copyright 2013, 2014 Mikhail Lopatkin
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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Date;

import static org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import static org.bitbucket.mlopatkin.android.liblogcat.LogRecord.NO_ID;
import static org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;

/**
 * Some factory methods to construct log records for tests.
 */
public final class LogRecordUtils {
    private LogRecordUtils() {}

    public static LogRecord forPid(int pid) {
        return new LogRecord(new Date(), pid, NO_ID, "", Priority.INFO, "", "");
    }

    public static LogRecord forAppName(String appName) {
        return new LogRecord(new Date(), NO_ID, NO_ID, appName, Priority.INFO, "", "");
    }

    public static LogRecord forTag(String tag) {
        return new LogRecord(new Date(), NO_ID, NO_ID, "", Priority.INFO, tag, "");
    }

    public static LogRecord forMessage(String message) {
        return new LogRecord(new Date(), NO_ID, NO_ID, "", Priority.INFO, "", message);
    }

    public static LogRecord forPriority(Priority priority) {
        return new LogRecord(new Date(), NO_ID, NO_ID, "", priority, "", "");
    }

    public static LogRecord forBuffer(@Nullable Buffer buffer) {
        return new LogRecord(new Date(), NO_ID, NO_ID, "", Priority.INFO, "", "", buffer);
    }

    public static LogRecord forPidAndAppName(int pid, String appName) {
        return new LogRecord(new Date(), pid, NO_ID, appName, Priority.INFO, "", "");
    }

    /**
     * Copies existing record but changes its time to newTime.
     *
     * @param r the record to copy
     * @param newTime the new time to use
     * @return new record with all fields identical to r except time
     */
    public static LogRecord withTime(LogRecord r, Date newTime) {
        return new LogRecord(newTime, r.getPid(), r.getTid(), r.getAppName(), r.getPriority(), r.getTag(),
                r.getMessage(), r.getBuffer());
    }

    public static LogRecord withPid(LogRecord r, int pid) {
        return new LogRecord(r.getTime(), pid, r.getTid(), r.getAppName(), r.getPriority(), r.getTag(), r.getMessage(),
                r.getBuffer());
    }

    public static LogRecord withTid(LogRecord r, int tid) {
        return new LogRecord(r.getTime(), r.getPid(), tid, r.getAppName(), r.getPriority(), r.getTag(), r.getMessage(),
                r.getBuffer());
    }

    public static LogRecord withAppName(LogRecord r, String appName) {
        return new LogRecord(r.getTime(), r.getPid(), r.getTid(), appName, r.getPriority(), r.getTag(), r.getMessage(),
                r.getBuffer());
    }

    public static LogRecord withPriority(LogRecord r, Priority priority) {
        return new LogRecord(r.getTime(), r.getPid(), r.getTid(), r.getAppName(), priority, r.getTag(), r.getMessage(),
                r.getBuffer());
    }

    public static LogRecord withTag(LogRecord r, String tag) {
        return new LogRecord(r.getTime(), r.getPid(), r.getTid(), r.getAppName(), r.getPriority(), tag, r.getMessage(),
                r.getBuffer());
    }

    public static LogRecord withMessage(LogRecord r, String message) {
        return new LogRecord(r.getTime(), r.getPid(), r.getTid(), r.getAppName(), r.getPriority(), r.getTag(), message,
                r.getBuffer());
    }

    public static LogRecord withBuffer(LogRecord r, @Nullable Buffer buffer) {
        return new LogRecord(r.getTime(), r.getPid(), r.getTid(), r.getAppName(), r.getPriority(), r.getTag(),
                r.getMessage(), buffer);
    }
}
