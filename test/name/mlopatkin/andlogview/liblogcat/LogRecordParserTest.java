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

import static name.mlopatkin.andlogview.liblogcat.AssertLogRecord.assertThatRecord;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Collections;

public class LogRecordParserTest {
    static final String BRIEF_RECORD = "D/MediaScanner(417): postscan return";
    static final String BRIEF_RECORD_PAD = "D/MediaScanner   (  417): postscan return";
    static final String PROCESS_RECORD = "D(  417) postscan return  (MediaScanner)";
    static final String TAG_RECORD = "D/MediaScanner: postscan return";
    static final String THREADTIME_RECORD = "08-18 13:40:59.546   417  1172 D MediaScanner: postscan return";
    static final String THREADTIME_RECORD_WITH_MCS =
            "08-18 13:40:59.546789   417  1172 D MediaScanner: postscan return";
    static final String TIME_RECORD = "08-18 13:40:59.546 D/MediaScanner(417): postscan return";
    static final String TIME_RECORD_WITH_MCS = "08-18 13:40:59.546789 D/MediaScanner(417): postscan return";
    static final String TIME_RECORD_PAD = "08-18 13:40:59.546 D/MediaScanner   (  417): postscan return";

    static final String TAG = "MediaScanner";
    static final String MESSAGE = "postscan return";
    static final int PID = 417;
    static final int TID = 1172;
    static final LogRecord.Priority PRIORITY = LogRecord.Priority.DEBUG;
    static final LogRecord.Buffer BUFFER = LogRecord.Buffer.MAIN;

    static final String LOG_BEGINNING_LINE = "--------- beginning of /dev/log/system";
    static final String BLANK_LINE = "";

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
    public void testLogRecordParserBrief() {
        LogRecord record = LogRecordParser.parseBrief(BUFFER, BRIEF_RECORD, Collections.emptyMap());

        assertThatRecord(record)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasPriority(PRIORITY)
                .hasBuffer(BUFFER)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void testLogRecordParserBriefPad() {
        LogRecord record =
                LogRecordParser.parseBrief(BUFFER, BRIEF_RECORD_PAD, Collections.emptyMap());

        assertThatRecord(record)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasPriority(PRIORITY)
                .hasBuffer(BUFFER)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void testLogRecordParserProcess() {
        LogRecord record =
                LogRecordParser.parseProcess(BUFFER, PROCESS_RECORD, Collections.emptyMap());

        assertThatRecord(record)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasPriority(PRIORITY)
                .hasBuffer(BUFFER)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void testLogRecordParserTag() {
        LogRecord record = LogRecordParser.parseTag(BUFFER, TAG_RECORD);

        assertThatRecord(record)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPriority(PRIORITY)
                .hasBuffer(BUFFER)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void testLogRecordParserTime() {
        LogRecord record = LogRecordParser.parseTime(BUFFER, TIME_RECORD, Collections.emptyMap());

        assertThatRecord(record)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasDate(8, 18).hasTime(13, 40, 59, 546)
                .hasPriority(PRIORITY)
                .hasBuffer(BUFFER)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void testLogRecordParserTimePad() {
        LogRecord record = LogRecordParser.parseTime(BUFFER, TIME_RECORD_PAD, Collections.emptyMap());

        assertThatRecord(record)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasDate(8, 18).hasTime(13, 40, 59, 546)
                .hasPriority(PRIORITY)
                .hasBuffer(BUFFER)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void testIsLogBeginningLine() {
        // positive test
        assertTrue(LogRecordParser.isLogBeginningLine(LOG_BEGINNING_LINE));
        // negative tests
        assertFalse(LogRecordParser.isLogBeginningLine(BLANK_LINE));
        assertFalse(LogRecordParser.isLogBeginningLine(BRIEF_RECORD));
        assertFalse(LogRecordParser.isLogBeginningLine(THREADTIME_RECORD));
    }

    @Test
    public void testTimeLogWithMicrosecondsTimestamp() {
        LogRecord record = LogRecordParser.parseTime(BUFFER, TIME_RECORD_WITH_MCS, Collections.emptyMap());

        assertThatRecord(record)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
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
    public void testAndroidStudioLogWithAppNameAndYear() {
        String logString = "2020-04-09 14:39:33.663 3630-21427/com.google.android.googlequicksearchbox:search "
                + "E/ActivityThread: Failed to find provider info for "
                + "com.google.android.apps.gsa.testing.ui.audio.recorded";
        LogRecord record = LogRecordParser.parseAndroidStudio(logString);

        assertThatRecord(record)
                .hasDate(4, 9).hasTime(14, 39, 33, 663)
                .hasPid(3630)
                .hasTid(21427)
                .hasAppName("com.google.android.googlequicksearchbox:search")
                .hasPriority(LogRecord.Priority.ERROR)
                .hasTag("ActivityThread")
                .hasMessage("Failed to find provider info for com.google.android.apps.gsa.testing.ui.audio.recorded")
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void testAndroidStudioLogWithoutYear() {
        String logString = "01-30 21:08:09.391 573-574/system_process W/ActivityManager: "
                + "Unable to start service Intent { action=com.android.ussd.INetworkService }: not found";
        LogRecord record = LogRecordParser.parseAndroidStudio(logString);

        assertThatRecord(record)
                .hasDate(1, 30).hasTime(21, 8, 9, 391)
                .hasPid(573)
                .hasTid(574)
                .hasAppName("system_process")
                .hasPriority(LogRecord.Priority.WARN)
                .hasTag("ActivityManager")
                .hasMessage("Unable to start service Intent { action=com.android.ussd.INetworkService }: not found")
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void testAndroidStudioLogWithoutAppName() {
        String logString = "2020-04-09 14:39:34.948 21494-21501/? "
                + "E/zygote: Failed sending reply to debugger: Broken pipe";
        LogRecord record = LogRecordParser.parseAndroidStudio(logString);

        assertThatRecord(record)
                .hasDate(4, 9).hasTime(14, 39, 34, 948)
                .hasPid(21494)
                .hasTid(21501)
                .hasPriority(LogRecord.Priority.ERROR)
                .hasTag("zygote")
                .hasMessage("Failed sending reply to debugger: Broken pipe")
                .andAllOtherFieldAreDefaults();
    }
}
