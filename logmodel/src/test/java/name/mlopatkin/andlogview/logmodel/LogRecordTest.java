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

package name.mlopatkin.andlogview.logmodel;

import static org.junit.Assert.assertEquals;

import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;

import org.junit.Test;

import java.util.Date;
import java.util.stream.Stream;

public class LogRecordTest {
    private static final long BASE_DATE_LONG = 1584719894000L;
    private static final Timestamp BASE_TIME = new Timestamp(new Date(BASE_DATE_LONG));
    private static final Timestamp AFTER_BASE_TIME = new Timestamp(new Date(BASE_DATE_LONG + 1));

    private static final LogRecord BASE = LogRecordUtils.forBuffer(Buffer.EVENTS).withTimestamp(BASE_TIME);

    @Test
    @SuppressWarnings({"SelfComparison", "EqualsWithItself"})
    public void logRecordIsEqualToItself() {
        assertEquals("BASE == BASE", 0, BASE.compareTo(BASE));
    }

    @Test
    public void logRecordsAreOrderedByTime() {
        LogRecord later = BASE.withTimestamp(AFTER_BASE_TIME);

        assertEquals("BASE < later", -1, BASE.compareTo(later));
        assertEquals("later < BASE", 1, later.compareTo(BASE));
    }

    @Test
    public void logRecordsAreOrderedByBuffer() {
        // CRASH > EVENTS
        LogRecord later = BASE.withBuffer(Buffer.CRASH);

        assertEquals("BASE < later", -1, BASE.compareTo(later));
        assertEquals("later < BASE", 1, later.compareTo(BASE));
    }

    @Test
    public void logRecordsAreOrderedByTimeFirst() {
        // EVENTS < MAIN
        LogRecord later = BASE.withBuffer(Buffer.MAIN).withTimestamp(AFTER_BASE_TIME);

        assertEquals("BASE < later", -1, BASE.compareTo(later));
        assertEquals("later < BASE", 1, later.compareTo(BASE));
    }

    @Test
    public void logRecordWithNullBufferIsSmallest() {
        LogRecord nullBuffer = BASE.withoutBuffer();

        Stream.of(Buffer.values()).map(BASE::withBuffer).forEach(r -> {
            assertEquals("nullBuffer < r{" + r.getBuffer() + "}", -1, nullBuffer.compareTo(r));
            assertEquals("r{" + r.getBuffer() + "} < nullBuffer", 1, r.compareTo(nullBuffer));
        });
    }

    @Test
    @SuppressWarnings({"SelfComparison", "EqualsWithItself"})
    public void logRecordWithNullBufferEqualsItself() {
        LogRecord nullBuffer = BASE.withoutBuffer();

        assertEquals("nullBuffer == nullBuffer", 0, nullBuffer.compareTo(nullBuffer));
    }
}

