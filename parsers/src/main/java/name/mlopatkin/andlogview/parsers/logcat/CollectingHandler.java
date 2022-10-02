/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.parsers.logcat;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;
import name.mlopatkin.andlogview.logmodel.Timestamp;
import name.mlopatkin.andlogview.parsers.ParserControl;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.function.IntFunction;

/**
 * The event handler that creates {@link LogRecord} instances for each parsed line and forwards them into
 * {@link #logRecord(LogRecord)} method. If the buffer and PID-to-Process lookup function are provided, then these will
 * be used for the created LogRecords.
 * <p>
 * The class is itself thread-safe if the provided {@code appNameLookup} is thread-safe. Subclasses may have
 * non-thread-safe implementations.
 */
public abstract class CollectingHandler implements LogcatParseEventsHandler {
    private static final @Nullable String NO_APP_NAME = null;

    private final @Nullable Buffer buffer;
    private final IntFunction<String> appNameLookup;

    public CollectingHandler() {
        this(null, id -> NO_APP_NAME);
    }

    public CollectingHandler(IntFunction<String> appNameLookup) {
        this(null, appNameLookup);
    }

    public CollectingHandler(Buffer buffer) {
        this(buffer, id -> NO_APP_NAME);
    }

    public CollectingHandler(@Nullable Buffer buffer, IntFunction<String> appNameLookup) {
        this.buffer = buffer;
        this.appNameLookup = appNameLookup;
    }

    @Override
    public ParserControl logRecord(int pid, LogRecord.Priority priority, String tag, String message) {
        return logRecord(
                LogRecord.createWithoutTimestamp(
                        pid, LogRecord.NO_ID, lookupAppByPid(pid), priority, tag, message, buffer));
    }

    @Override
    public ParserControl logRecord(Timestamp timestamp, int pid, int tid, LogRecord.Priority priority, String tag,
            String message) {
        return logRecord(
                LogRecord.createWithTimestamp(
                        timestamp, pid, tid, lookupAppByPid(pid), priority, tag, message, buffer));
    }

    @Override
    public ParserControl logRecord(Timestamp timestamp, int pid, int tid, LogRecord.Priority priority, String tag,
            String message, @Nullable String appName) {
        if (Objects.equals(NO_APP_NAME, appName)) {
            appName = lookupAppByPid(pid);
        }
        return logRecord(
                LogRecord.createWithTimestamp(timestamp, pid, tid, appName, priority, tag, message, buffer));
    }

    @Override
    public ParserControl logRecord(String message) {
        return logRecord(
                LogRecord.createWithoutTimestamp(
                        LogRecord.NO_ID, LogRecord.NO_ID, NO_APP_NAME, LogRecord.Priority.INFO, "", message, buffer));
    }

    @Override
    public ParserControl logRecord(LogRecord.Priority priority, String tag, String message) {
        return logRecord(
                LogRecord.createWithoutTimestamp(
                        LogRecord.NO_ID, LogRecord.NO_ID, NO_APP_NAME, priority, tag, message, buffer));
    }

    @Override
    public ParserControl logRecord(int pid, int tid, LogRecord.Priority priority, String message) {
        return logRecord(
                LogRecord.createWithoutTimestamp(pid, tid, lookupAppByPid(pid), priority, "", message, buffer));
    }

    @Override
    public ParserControl logRecord(Timestamp timestamp, int pid, LogRecord.Priority priority, String tag,
            String message) {
        return logRecord(
                LogRecord.createWithTimestamp(
                        timestamp, pid, LogRecord.NO_ID, lookupAppByPid(pid), priority, tag, message, buffer));
    }

    protected abstract ParserControl logRecord(LogRecord record);

    private @Nullable String lookupAppByPid(int pid) {
        return appNameLookup.apply(pid);
    }
}
