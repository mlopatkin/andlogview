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
import name.mlopatkin.andlogview.logmodel.Timestamp;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.PushParser;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A handler of push parses events. The parser invokes an appropriate callback depending on the number of
 * fields that can be extracted from the parsed line.
 * <p>
 * Default method implementations drop data and delegate to methods with fewer parameters. Everything eventually
 * boils down to the {@link #logRecord(String)}. This aims to help in cases where the all data from the log is not
 * really necessary, e.g. in tests or to check if the file is parseable.
 */
public interface LogcatParseEventsHandler extends PushParser.ParseEventsHandler {
    /**
     * Called for records in {@code brief} ({@linkplain Format#BRIEF}) or {@code process} ({@linkplain Format#PROCESS})
     * formats.
     * <p>
     * The default implementation delegates to
     * {@link #logRecord(LogRecord.Priority, String, String)}.
     *
     * @param pid the pid (process id) of the log record's emitter
     * @param priority the priority of the record
     * @param tag the tag of the record
     * @param message the message of the record
     * @return {@linkplain ParserControl} instance to determine next parser action
     */
    default ParserControl logRecord(int pid, LogRecord.Priority priority, String tag, String message) {
        return logRecord(priority, tag, message);
    }

    /**
     * Called for records in {@code long} ({@linkplain Format#LONG}) or {@code threadtime}
     * ({@linkplain Format#THREADTIME}) formats.
     * <p>
     * The default implementation delegates to
     * {@link #logRecord(Timestamp, int, LogRecord.Priority, String, String)}.
     *
     * @param timestamp the timestamp of the log entry
     * @param pid the pid (process id) of the log record's emitter
     * @param tid the tid (thread id) of the log record's emitter
     * @param priority the priority of the record
     * @param tag the tag of the record
     * @param message the message of the record
     * @return {@linkplain ParserControl} instance to determine next parser action
     */
    default ParserControl logRecord(Timestamp timestamp, int pid, int tid, LogRecord.Priority priority, String tag,
            String message) {
        return logRecord(timestamp, pid, priority, tag, message);
    }

    /**
     * Called for records in the format used in Android Studio's logcat ({@linkplain Format#STUDIO}). This format can
     * sometimes have information about the process name, but not necessary for all lines.
     * <p>
     * The default implementation delegates to
     * {@link #logRecord(Timestamp, int, int, LogRecord.Priority, String, String)}.
     *
     * @param timestamp the timestamp of the log entry
     * @param pid the pid (process id) of the log record's emitter
     * @param tid the tid (thread id) of the log record's emitter
     * @param priority the priority of the record
     * @param tag the tag of the record
     * @param message the message of the record
     * @param appName the name of the process of the log record's emitter or {@code null} if it wasn't provided
     * @return {@linkplain ParserControl} instance to determine next parser action
     */
    default ParserControl logRecord(Timestamp timestamp, int pid, int tid, LogRecord.Priority priority, String tag,
            String message,
            @Nullable String appName) {
        return logRecord(timestamp, pid, tid, priority, tag, message);
    }

    /**
     * Called for records in {@code raw} ({@linkplain Format#RAW}) format. This is the only method that you have to
     * implement, all others delegate to this one in the end.
     *
     * @param message the message of the record
     * @return {@linkplain ParserControl} instance to determine next parser action
     */
    default ParserControl logRecord(String message) {
        return ParserControl.proceed();
    }

    /**
     * Called for records in {@code tag} ({@linkplain Format#TAG}) format.
     * <p>
     * The default implementation delegates to
     * {@link #logRecord(String)}.
     *
     * @param priority the priority of the record
     * @param tag the tag of the record
     * @param message the message of the record
     * @return {@linkplain ParserControl} instance to determine next parser action
     */
    default ParserControl logRecord(LogRecord.Priority priority, String tag, String message) {
        return logRecord(message);
    }

    /**
     * Called for records in {@code thread} ({@linkplain Format#THREAD}) format.
     * <p>
     * The default implementation delegates to
     * {@link #logRecord(String)}.
     *
     * @param pid the pid (process id) of the log record's emitter
     * @param tid the tid (thread id) of the log record's emitter
     * @param priority the priority of the record
     * @param message the message of the record
     * @return {@linkplain ParserControl} instance to determine next parser action
     */
    default ParserControl logRecord(int pid, int tid, LogRecord.Priority priority, String message) {
        return logRecord(message);
    }

    /**
     * Called for records in {@code time} ({@linkplain Format#TIME}) format.
     * <p>
     * The default implementation delegates to
     * {@link #logRecord(int, LogRecord.Priority, String, String)}.
     *
     * @param timestamp the timestamp of the log entry
     * @param pid the pid (process id) of the log record's emitter
     * @param priority the priority of the record
     * @param tag the tag of the record
     * @param message the message of the record
     * @return {@linkplain ParserControl} instance to determine next parser action
     */
    default ParserControl logRecord(Timestamp timestamp, int pid, LogRecord.Priority priority, String tag,
            String message) {
        return logRecord(pid, priority, tag, message);
    }

    /**
     * Called for lines that cannot be attributed to any record
     *
     * @return {@linkplain ParserControl} instance to determine next parser action
     */
    default ParserControl unparseableLine() {
        return ParserControl.proceed();
    }
}
