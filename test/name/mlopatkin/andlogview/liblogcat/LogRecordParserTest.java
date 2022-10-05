/*
 * Copyright 2011 Mikhail Lopatkin
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
package name.mlopatkin.andlogview.liblogcat;

import static name.mlopatkin.andlogview.logmodel.AssertLogRecord.assertThatRecord;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import org.junit.Test;

import java.util.Collections;

public class LogRecordParserTest {
    static final String THREADTIME_RECORD = "08-18 13:40:59.546   417  1172 D MediaScanner: postscan return";
    static final String THREADTIME_RECORD_WITH_MCS =
            "08-18 13:40:59.546789   417  1172 D MediaScanner: postscan return";

    static final String TAG = "MediaScanner";
    static final String MESSAGE = "postscan return";
    static final int PID = 417;
    static final int TID = 1172;
    static final LogRecord.Priority PRIORITY = LogRecord.Priority.DEBUG;
    static final LogRecord.Buffer BUFFER = LogRecord.Buffer.MAIN;

    @Test
    public void testLogRecordParserThreadTime() {
        LogRecord record =
                LogRecordParser.parseThreadTime(BUFFER, THREADTIME_RECORD, Collections.emptyMap());

        assertThatRecord(record)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasTid(TID)
                .hasDate(8, 18).hasTime(13, 40, 59, 546)
                .hasPriority(PRIORITY)
                .hasBuffer(BUFFER)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void testThreadTimeLogWithMicrosecondsTimestamp() {
        LogRecord record = LogRecordParser.parseThreadTime(BUFFER, THREADTIME_RECORD_WITH_MCS, Collections.emptyMap());

        assertThatRecord(record)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasTid(TID)
                .hasDate(8, 18).hasTime(13, 40, 59, 546)
                .hasPriority(PRIORITY)
                .hasBuffer(BUFFER)
                .andAllOtherFieldAreDefaults();

    }

    @Test
    public void testThreadTimeWithEmptyMessage() {
        String logString = "01-09 23:11:34.523 32181 32278 E native  :";
        LogRecord record = LogRecordParser.parseThreadTime(BUFFER, logString, Collections.emptyMap());

        assertThatRecord(record)
                .hasDate(1, 9).hasTime(23, 11, 34, 523)
                .hasPid(32181)
                .hasTid(32278)
                .hasBuffer(BUFFER)
                .hasPriority(LogRecord.Priority.ERROR)
                .hasTag("native")
                .hasMessage("")
                .andAllOtherFieldAreDefaults();
    }
}
