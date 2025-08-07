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

package name.mlopatkin.andlogview.logmodel;

import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;
import name.mlopatkin.andlogview.logmodel.LogRecord.Priority;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates the instances of the LogRecord, bound to the given buffer (if known). Assigns proper sequence numbers to the
 * produced records.
 */
public class LogRecordFactory {
    private final @Nullable Buffer buffer;
    private final AtomicInteger seqNo = new AtomicInteger();

    public LogRecordFactory(Buffer buffer) {
        this.buffer = Objects.requireNonNull(buffer);
    }

    public LogRecordFactory() {
        this.buffer = null;
    }

    public LogRecord create(
            Timestamp timestamp, int pid, int tid, @Nullable String appName, Priority priority, String tag,
            String message) {
        return new LogRecord(new SequenceNumber(seqNo.incrementAndGet(), buffer), timestamp, pid, tid, appName,
                priority, tag, message, buffer);
    }

    public LogRecord create(
            int pid, int tid, @Nullable String appName, Priority priority, String tag, String message) {
        return new LogRecord(new SequenceNumber(seqNo.incrementAndGet(), buffer), /* timestamp */null, pid, tid,
                appName, priority, tag, message, buffer);
    }
}
