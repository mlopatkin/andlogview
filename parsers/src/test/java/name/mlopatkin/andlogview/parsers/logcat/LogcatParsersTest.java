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

import static name.mlopatkin.andlogview.parsers.logcat.SingleEntryParser.assertOnlyParsedRecord;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.parsers.ParserUtils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.List;

public class LogcatParsersTest {
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

    @Test
    public void parsesBrief() {
        assertOnlyParsedRecord(LogcatParsers::brief, BRIEF_RECORD)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasPriority(PRIORITY)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void parsesBriefWithPadding() {
        assertOnlyParsedRecord(LogcatParsers::brief, BRIEF_RECORD_PAD)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasPriority(PRIORITY)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void parsesProcess() {
        assertOnlyParsedRecord(LogcatParsers::process, PROCESS_RECORD)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasPriority(PRIORITY)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void parsesStudioLogWithAppNameAndYear() {
        String logString = "2020-04-09 14:39:33.663 3630-21427/com.google.android.googlequicksearchbox:search "
                + "E/ActivityThread: Failed to find provider info for "
                + "com.google.android.apps.gsa.testing.ui.audio.recorded";

        assertOnlyParsedRecord(LogcatParsers::androidStudio, logString)
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
    public void parsesStudioLogWithoutYear() {
        String logString = "01-30 21:08:09.391 573-574/system_process W/ActivityManager: "
                + "Unable to start service Intent { action=com.android.ussd.INetworkService }: not found";

        assertOnlyParsedRecord(LogcatParsers::androidStudio, logString)
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
    public void parsesStudioLogWithoutAppName() {
        String logString = "2020-04-09 14:39:34.948 21494-21501/? "
                + "E/zygote: Failed sending reply to debugger: Broken pipe";

        assertOnlyParsedRecord(LogcatParsers::androidStudio, logString)
                .hasDate(4, 9).hasTime(14, 39, 34, 948)
                .hasPid(21494)
                .hasTid(21501)
                .hasPriority(LogRecord.Priority.ERROR)
                .hasTag("zygote")
                .hasMessage("Failed sending reply to debugger: Broken pipe")
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void parsesTag() {
        assertOnlyParsedRecord(LogcatParsers::tag, TAG_RECORD)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPriority(PRIORITY)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void parsesThreadTime() {
        assertOnlyParsedRecord(LogcatParsers::threadTime, THREADTIME_RECORD)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasTid(TID)
                .hasDate(8, 18).hasTime(13, 40, 59, 546)
                .hasPriority(PRIORITY)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void parsesThreadTimeWithMicrosecondsTimestamp() {
        assertOnlyParsedRecord(LogcatParsers::threadTime, THREADTIME_RECORD_WITH_MCS)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasTid(TID)
                .hasDate(8, 18).hasTime(13, 40, 59, 546)
                .hasPriority(PRIORITY)
                .andAllOtherFieldAreDefaults();

    }

    @Test
    public void parsesThreadTimeWithEmptyMessage() {
        String logString = "01-09 23:11:34.523 32181 32278 E native  :";

        assertOnlyParsedRecord(LogcatParsers::threadTime, logString)
                .hasDate(1, 9).hasTime(23, 11, 34, 523)
                .hasPid(32181)
                .hasTid(32278)
                .hasPriority(LogRecord.Priority.ERROR)
                .hasTag("native")
                .hasMessage("")
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void parsesTime() {
        assertOnlyParsedRecord(LogcatParsers::time, TIME_RECORD)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasDate(8, 18).hasTime(13, 40, 59, 546)
                .hasPriority(PRIORITY)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void parsesTimeWithPadding() {
        assertOnlyParsedRecord(LogcatParsers::time, TIME_RECORD_PAD)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasDate(8, 18).hasTime(13, 40, 59, 546)
                .hasPriority(PRIORITY)
                .andAllOtherFieldAreDefaults();
    }

    @Test
    public void parsesTimeWithMicrosecondsTimestamp() {
        assertOnlyParsedRecord(LogcatParsers::time, TIME_RECORD_WITH_MCS)
                .hasTag(TAG)
                .hasMessage(MESSAGE)
                .hasPid(PID)
                .hasDate(8, 18).hasTime(13, 40, 59, 546)
                .hasPriority(PRIORITY)
                .andAllOtherFieldAreDefaults();
    }

    @ParameterizedTest(name = "{0} with Eoln.{2}")
    @LogcatCompatSource(
            path = "parsers/logcat/golden.json",
            formats = {
                    Format.BRIEF,
                    Format.PROCESS,
                    Format.TAG,
                    Format.THREADTIME,
                    Format.TIME
            },
            eolns = {Eoln.NONE, Eoln.LF, Eoln.CRLF}
    )
    void compatibilityTest(Format format, List<LogRecord> expectedRecords, Eoln ignoredEoln, List<String> lines) {
        ListCollectingHandler handler = new ListCollectingHandler();
        try (var parser = new LogcatPushParser<>(format, handler)) {
            Assertions.assertThat(ParserUtils.readInto(parser, lines.stream())).as("should consume all lines").isTrue();
        }

        Assertions.assertThat(handler.getCollectedRecords())
                .usingElementComparator(format.createComparator())
                .isEqualTo(expectedRecords);
    }
}
