/*
 * Copyright 2023 the Andlogview authors
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

import name.mlopatkin.andlogview.logmodel.Field;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecordBuilder;
import name.mlopatkin.andlogview.logmodel.LogRecordUtils;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Objects;

/**
 * A JSON-serializable representation of a logcat log entry.
 */
@SuppressWarnings({"UseCorrectAssertInTests", "unused"})
public class LogcatEntryJsonModel {
    private @Nullable String time;
    private @Nullable String pid;
    private @Nullable String tid;
    private @Nullable String prio;
    private @Nullable String tag;
    private @Nullable String message;

    public LogRecord buildRecord(Collection<Field<?>> fields) {
        assert fields.contains(Field.MESSAGE);
        LogRecordBuilder record = LogRecordUtils.logRecord(Objects.requireNonNull(message));
        if (fields.contains(Field.TIME)) {
            record.withTime(Objects.requireNonNull(time));
        }
        if (fields.contains(Field.PID)) {
            record.withPid(Integer.parseInt(Objects.requireNonNull(pid)));
        }
        if (fields.contains(Field.TID)) {
            record.withTid(Integer.parseInt(Objects.requireNonNull(tid)));
        }
        if (fields.contains(Field.PRIORITY)) {
            record.withPriority(Objects.requireNonNull(prio));
        }
        if (fields.contains(Field.TAG)) {
            record.withTag(Objects.requireNonNull(tag));
        }
        return record.build();
    }
}
