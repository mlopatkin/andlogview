/*
 * Copyright 2013 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.liblogcat.filters;

import name.mlopatkin.andlogview.liblogcat.LogRecord;
import name.mlopatkin.andlogview.liblogcat.LogRecord.Buffer;

import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * Performs filtering based on the buffer of the record.
 */
public class LogBufferFilter implements Predicate<LogRecord> {
    private EnumSet<Buffer> buffers = EnumSet.noneOf(Buffer.class);

    @Override
    public boolean test(LogRecord record) {
        Buffer buffer = record.getBuffer();
        // Always allow records with unknown buffer to show.
        return buffer == null || buffers.contains(buffer);
    }

    public void setBufferEnabled(Buffer buffer, boolean enabled) {
        if (enabled) {
            buffers.add(buffer);
        } else {
            buffers.remove(buffer);
        }
    }
}
