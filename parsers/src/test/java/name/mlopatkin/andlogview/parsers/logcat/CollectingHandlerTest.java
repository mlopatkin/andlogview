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

import static name.mlopatkin.andlogview.logmodel.AssertLogRecord.assertThatRecord;
import static name.mlopatkin.andlogview.parsers.logcat.SingleEntryParser.assertOnlyRecord;

import static org.assertj.core.api.Assertions.assertThat;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecord.Priority;
import name.mlopatkin.andlogview.logmodel.TimeFormatUtils;
import name.mlopatkin.andlogview.logmodel.Timestamp;
import name.mlopatkin.andlogview.utils.Try;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class CollectingHandlerTest {
    private static final Timestamp TIME =
            Try.ofCallable(() -> TimeFormatUtils.getTimeFromString("10-01 10:00:00.000")).get();

    @Test
    void createdRecordsHaveNoBufferAndAppNameByDefault() {
        ListCollectingHandler collector = new ListCollectingHandler();

        createAllKindsOfRecords(collector);

        assertThat(collector.getCollectedRecords())
                .allSatisfy(r ->
                        assertThatRecord(r)
                                .hasNoAppName()
                                .hasNoBuffer());
    }

    @Test
    void createdRecordsHaveProvidedBufferAndNoAppName() {
        ListCollectingHandler collector = new ListCollectingHandler(LogRecord.Buffer.SYSTEM);

        createAllKindsOfRecords(collector);

        assertThat(collector.getCollectedRecords())
                .allSatisfy(r ->
                        assertThatRecord(r)
                                .hasNoAppName()
                                .hasBuffer(LogRecord.Buffer.SYSTEM));
    }

    @Test
    void briefOrProcessRecordHasProperFieldsSet() {
        ListCollectingHandler collector = new ListCollectingHandler();

        collector.logRecord(1, Priority.INFO, "TAG", "message");

        assertOnlyRecord(collector)
                .hasPid(1)
                .hasPriority(Priority.INFO)
                .hasTag("TAG")
                .hasMessage("message")
                .andAllOtherFieldAreDefaults();
    }

    @Test
    void longOrThreadTimeRecordHasProperFieldsSet() {
        ListCollectingHandler collector = new ListCollectingHandler();

        collector.logRecord(TIME, 1, 2, Priority.INFO, "TAG", "message");

        assertOnlyRecord(collector)
                .hasDate(10, 1).hasTime(10, 0, 0, 0)
                .hasPid(1)
                .hasTid(2)
                .hasPriority(Priority.INFO)
                .hasTag("TAG")
                .hasMessage("message")
                .andAllOtherFieldAreDefaults();
    }

    @Test
    void studioRecordHasProperFieldsSet() {
        ListCollectingHandler collector = new ListCollectingHandler();

        collector.logRecord(TIME, 1, 2, Priority.INFO, "TAG", "message", "app");

        assertOnlyRecord(collector)
                .hasDate(10, 1).hasTime(10, 0, 0, 0)
                .hasPid(1)
                .hasTid(2)
                .hasPriority(Priority.INFO)
                .hasTag("TAG")
                .hasMessage("message")
                .hasAppName("app")
                .andAllOtherFieldAreDefaults();
    }

    @Test
    void rawRecordHasProperFieldsSet() {
        ListCollectingHandler collector = new ListCollectingHandler();

        collector.logRecord("message");

        assertOnlyRecord(collector)
                .hasMessage("message")
                .hasPriority(Priority.INFO)
                .hasTag("")
                .andAllOtherFieldAreDefaults();
    }

    @Test
    void tagRecordHasProperFieldsSet() {
        ListCollectingHandler collector = new ListCollectingHandler();

        collector.logRecord(Priority.INFO, "TAG", "message");

        assertOnlyRecord(collector)
                .hasPriority(Priority.INFO)
                .hasTag("TAG")
                .hasMessage("message")
                .andAllOtherFieldAreDefaults();
    }

    @Test
    void threadRecordHasProperFieldsSet() {
        ListCollectingHandler collector = new ListCollectingHandler();

        collector.logRecord(1, 2, Priority.INFO, "message");

        assertOnlyRecord(collector)
                .hasPid(1)
                .hasTid(2)
                .hasPriority(Priority.INFO)
                .hasTag("")
                .hasMessage("message")
                .andAllOtherFieldAreDefaults();
    }

    @Test
    void timeRecordHasProperFieldsSet() {
        ListCollectingHandler collector = new ListCollectingHandler();

        collector.logRecord(TIME, 1, Priority.INFO, "TAG", "message");

        assertOnlyRecord(collector)
                .hasDate(10, 1).hasTime(10, 0, 0, 0)
                .hasPid(1)
                .hasPriority(Priority.INFO)
                .hasTag("TAG")
                .hasMessage("message")
                .andAllOtherFieldAreDefaults();
    }

    @Test
    void pidLookupIsPerformedForKnownPid() {
        ListCollectingHandler collector = new ListCollectingHandler(this::lookupPid);

        collector.logRecord(1, Priority.DEBUG, "TAG", "message");
        collector.logRecord(TIME, 1, 2, Priority.DEBUG, "TAG", "message");
        collector.logRecord(TIME, 1, 2, Priority.DEBUG, "TAG", "message", null);
        collector.logRecord(1, 2, Priority.DEBUG, "message");
        collector.logRecord(TIME, 1, Priority.DEBUG, "TAG", "message");

        assertThat(collector.getCollectedRecords()).allSatisfy(r -> assertThatRecord(r).hasAppName("app"));
    }

    @Test
    void pidLookupIsNotPerformedForUnknownPid() {
        ListCollectingHandler collector = new ListCollectingHandler(this::lookupPid);

        collector.logRecord(3, Priority.DEBUG, "TAG", "message");
        collector.logRecord(TIME, 3, 2, Priority.DEBUG, "TAG", "message");
        collector.logRecord(TIME, 3, 2, Priority.DEBUG, "TAG", "message", null);
        collector.logRecord(3, 2, Priority.DEBUG, "message");
        collector.logRecord(TIME, 3, Priority.DEBUG, "TAG", "message");

        assertThat(collector.getCollectedRecords()).allSatisfy(r -> assertThatRecord(r).hasNoAppName());
    }

    @Test
    void explicitAppNameInStudioLogsOverridesPidLookup() {
        ListCollectingHandler collector = new ListCollectingHandler(this::lookupPid);
        collector.logRecord(TIME, 1, 2, Priority.DEBUG, "TAG", "message", "otherApp");

        assertOnlyRecord(collector).hasAppName("otherApp");
    }

    private static void createAllKindsOfRecords(ListCollectingHandler collector) {
        collector.logRecord(1, Priority.DEBUG, "TAG", "message");
        collector.logRecord(TIME, 1, 2, Priority.DEBUG, "TAG", "message");
        collector.logRecord(TIME, 1, 2, Priority.DEBUG, "TAG", "message", null);
        collector.logRecord("message");
        collector.logRecord(Priority.DEBUG, "TAG", "message");
        collector.logRecord(1, 2, Priority.DEBUG, "message");
        collector.logRecord(TIME, 1, Priority.DEBUG, "TAG", "message");
    }

    private @Nullable String lookupPid(int pid) {
        return (pid == 1) ? "app" : null;
    }
}
