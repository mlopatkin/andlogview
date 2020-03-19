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

    public static LogRecord forBuffer(Buffer buffer) {
        return new LogRecord(new Date(), NO_ID, NO_ID, "", Priority.INFO, "", "", buffer);
    }

    public static LogRecord forPidAndAppName(int pid, String appName) {
        return new LogRecord(new Date(), pid, NO_ID, appName, Priority.INFO, "", "");
    }
}
