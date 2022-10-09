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

package name.mlopatkin.andlogview.logmodel;

import static name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;
import static name.mlopatkin.andlogview.logmodel.LogRecord.NO_ID;
import static name.mlopatkin.andlogview.logmodel.LogRecord.Priority;

/**
 * Some factory methods to construct log records for tests.
 */
public final class LogRecordUtils {
    // A record without any fields set to anything meaningful, used as a baseline for more specialized records.
    private static final LogRecord DEFAULT_RECORD =
            new LogRecord(null, NO_ID, NO_ID, null, Priority.LOWEST, "", "", null);

    private LogRecordUtils() {}

    public static LogRecord forPid(int pid) {
        return DEFAULT_RECORD.withPid(pid);
    }

    public static LogRecord forAppName(String appName) {
        return DEFAULT_RECORD.withAppName(appName);
    }

    public static LogRecord forTag(String tag) {
        return DEFAULT_RECORD.withTag(tag);
    }

    public static LogRecord forMessage(String message) {
        return DEFAULT_RECORD.withMessage(message);
    }

    public static LogRecord forPriority(Priority priority) {
        return DEFAULT_RECORD.withPriority(priority);
    }

    public static LogRecord forUnknownBuffer() {
        return DEFAULT_RECORD.withoutBuffer();
    }

    public static LogRecord forBuffer(Buffer buffer) {
        return DEFAULT_RECORD.withBuffer(buffer);
    }

    public static LogRecord forPidAndAppName(int pid, String appName) {
        return DEFAULT_RECORD.withPid(pid).withAppName(appName);
    }

    public static LogRecordBuilder logRecord(String message) {
        return new LogRecordBuilder(message);
    }

}
