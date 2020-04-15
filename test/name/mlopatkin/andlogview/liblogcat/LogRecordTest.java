/*
 * Copyright 2020 Mikhail Lopatkin
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

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;
import org.junit.Test;

import java.util.Date;
import java.util.stream.Stream;

import static org.bitbucket.mlopatkin.android.liblogcat.LogRecordUtils.withBuffer;
import static org.bitbucket.mlopatkin.android.liblogcat.LogRecordUtils.withTime;
import static org.junit.Assert.assertEquals;

public class LogRecordTest {
    public static final long BASE_DATE = 1584719894000L;
    private static final LogRecord BASE = new LogRecord(
            new Date(BASE_DATE), 123, 123, "com.example.app", Priority.INFO, "tag", "message", Buffer.EVENTS);


    @Test
    @SuppressWarnings({"SelfComparison", "EqualsWithItself"})
    public void logRecordIsEqualToItself() {
        assertEquals("BASE == BASE", 0, BASE.compareTo(BASE));
    }

    @Test
    public void logRecordsAreOrderedByTime() {
        LogRecord later = withTime(BASE, new Date(BASE_DATE + 1));

        assertEquals("BASE < later", -1, BASE.compareTo(later));
        assertEquals("later < BASE", 1, later.compareTo(BASE));
    }

    @Test
    public void logRecordsAreOrderedByBuffer() {
        // CRASH > EVENTS
        LogRecord later = withBuffer(BASE, Buffer.CRASH);

        assertEquals("BASE < later", -1, BASE.compareTo(later));
        assertEquals("later < BASE", 1, later.compareTo(BASE));
    }

    @Test
    public void logRecordsAreOrderedByTimeFirst() {
        // EVENTS < MAIN
        LogRecord later = withTime(withBuffer(BASE, Buffer.MAIN), new Date(BASE_DATE + 1));

        assertEquals("BASE < later", -1, BASE.compareTo(later));
        assertEquals("later < BASE", 1, later.compareTo(BASE));
    }

    @Test
    public void logRecordWithNullBufferIsSmallest() {
        LogRecord nullBuffer = withBuffer(BASE, null);

        Stream.of(Buffer.values()).map(b -> withBuffer(BASE, b)).forEach(r -> {
            assertEquals("nullBuffer < r{" + r.getBuffer() + "}", -1, nullBuffer.compareTo(r));
            assertEquals("r{" + r.getBuffer() + "} < nullBuffer", 1, r.compareTo(nullBuffer));
        });
    }

    @Test
    @SuppressWarnings({"SelfComparison", "EqualsWithItself"})
    public void logRecordWithNullBufferEqualsItself() {
        LogRecord nullBuffer = withBuffer(BASE, null);

        assertEquals("nullBuffer == nullBuffer", 0, nullBuffer.compareTo(nullBuffer));
    }
}

