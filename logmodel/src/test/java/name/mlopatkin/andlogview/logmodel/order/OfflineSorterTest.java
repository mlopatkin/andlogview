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

package name.mlopatkin.andlogview.logmodel.order;

import static name.mlopatkin.andlogview.logmodel.LogRecordMatchers.hasMessage;
import static name.mlopatkin.andlogview.logmodel.LogRecordUtils.forTimestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class OfflineSorterTest {

    @Test
    void sortsMultipleBuffersWithoutTimeTravelsProperly() {
        var bufferMain = records(Buffer.MAIN,
                forTimestamp("01-01 00:00:00.000").withMessage("main 1"),
                forTimestamp("01-01 00:00:01.000").withMessage("main 2"),
                forTimestamp("01-01 00:00:02.000").withMessage("main 3")
        );
        var bufferSystem = records(Buffer.SYSTEM,
                forTimestamp("01-01 00:00:00.000").withMessage("system 1"),
                forTimestamp("01-01 00:00:01.500").withMessage("system 2"),
                forTimestamp("01-01 00:00:01.700").withMessage("system 3")
        );
        var bufferEvents = records(Buffer.EVENTS,
                forTimestamp("01-01 00:00:00.000").withMessage("events 1"),
                forTimestamp("01-01 00:00:01.000").withMessage("events 2"),
                forTimestamp("01-01 00:00:03.000").withMessage("events 3"));

        var sorter = new OfflineSorter();

        addAll(sorter, bufferMain);
        addAll(sorter, bufferSystem);
        addAll(sorter, bufferEvents);

        assertThat(sorter.hasTimeTravels()).isFalse();
        assertThat(sorter.build(), contains(
                hasMessage("main 1"),
                hasMessage("system 1"),
                hasMessage("events 1"),
                hasMessage("main 2"),
                hasMessage("events 2"),
                hasMessage("system 2"),
                hasMessage("system 3"),
                hasMessage("main 3"),
                hasMessage("events 3")
        ));
    }

    @Test
    void noTimeTravelForRecordsWithIdenticalTimestamps() {
        var buffer = records(Buffer.MAIN,
                forTimestamp("01-01 00:00:00.000").withMessage("main 1"),
                forTimestamp("01-01 00:00:00.000").withMessage("main 2"),
                forTimestamp("01-01 00:00:00.000").withMessage("main 3"));

        var sorter = new OfflineSorter();

        addAll(sorter, buffer);

        assertThat(sorter.hasTimeTravels()).isFalse();
    }

    @Test
    void sortsSingleBufferWithTimeTravelProperly() {
        var buffer = records(Buffer.MAIN,
                forTimestamp("01-01 00:00:00.000").withMessage("main 1"),
                forTimestamp("01-01 00:00:01.000").withMessage("main 2"),
                forTimestamp("01-01 00:00:00.100").withMessage("main 3"));

        var sorter = new OfflineSorter();

        addAll(sorter, buffer);

        assertThat(sorter.hasTimeTravels()).isTrue();
        assertThat(sorter.build(), contains(
                hasMessage("main 1"),
                hasMessage("main 2"),
                hasMessage("main 3")
        ));
    }

    @Test
    void sortsSeveralBuffersWithTimeTravelProperly() {
        var bufferMain = records(Buffer.MAIN,
                forTimestamp("01-01 00:00:00.000").withMessage("main 1"),
                forTimestamp("01-01 00:00:01.000").withMessage("main 2"),
                forTimestamp("01-01 00:00:00.100").withMessage("main 3"));

        var bufferSystem = records(Buffer.SYSTEM,
                forTimestamp("01-01 00:00:02.000").withMessage("system 1"),
                forTimestamp("01-01 00:00:01.000").withMessage("system 2"));

        var sorter = new OfflineSorter();

        addAll(sorter, bufferMain);
        addAll(sorter, bufferSystem);

        assertThat(sorter.hasTimeTravels()).isTrue();
        assertThat(sorter.build(), contains(
                hasMessage("main 1"),
                hasMessage("main 2"),
                hasMessage("system 1"),
                hasMessage("main 3"),
                hasMessage("system 2")
        ));
    }

    private static List<LogRecord> records(Buffer buffer, LogRecord... records) {
        var output = new ArrayList<LogRecord>(records.length);

        for (int i = 0; i < records.length; ++i) {
            output.add(records[i].withBuffer(buffer).withSequenceNumber(i));
        }

        return output;
    }

    private static void addAll(OfflineSorter sorter, Iterable<LogRecord> records) {
        for (LogRecord record : records) {
            sorter.add(record);
        }
    }
}
