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

package org.bitbucket.mlopatkin.android.liblogcat.filters;

import com.google.common.base.Predicate;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;

import java.util.EnumSet;

/**
 * Performs filtering based on the buffer of the record.
 */
public class LogBufferFilter implements Predicate<LogRecord> {

    private EnumSet<Buffer> buffers = EnumSet.of(Buffer.UNKNOWN);

    @Override
    public boolean apply(LogRecord record) {
        return buffers.contains(record.getBuffer());
    }

    public void setBufferEnabled(Buffer buffer, boolean enabled) {
        if (enabled) {
            buffers.add(buffer);
        } else {
            buffers.remove(buffer);
        }
    }

}
