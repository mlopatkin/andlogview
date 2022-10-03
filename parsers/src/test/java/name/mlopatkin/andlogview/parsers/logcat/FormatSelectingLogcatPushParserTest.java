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
import static name.mlopatkin.andlogview.parsers.logcat.SingleEntryParser.assertOnlyRecord;

import static org.assertj.core.api.Assertions.assertThat;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.parsers.PushParser;

import org.junit.jupiter.api.Test;

class FormatSelectingLogcatPushParserTest {
    @Test
    void selectsBriefFormat() {
        assertOnlyParsedRecord(
                        h -> new FormatSelectingLogcatPushParser<>(h, Format.BRIEF, Format.THREADTIME),
                        LogcatParsersTest.BRIEF_RECORD)
                .hasTag("MediaScanner").hasTid(LogRecord.NO_ID);
    }

    @Test
    void selectsThreadTimeFormat() {
        assertOnlyParsedRecord(
                        h -> new FormatSelectingLogcatPushParser<>(h, Format.BRIEF, Format.THREADTIME),
                        LogcatParsersTest.THREADTIME_RECORD)
                .hasTag("MediaScanner")
                .hasTid(1172);
    }

    @Test
    void selectsProperFormatWhenPrecededWithGarbage() {
        ListCollectingHandler h = new ListCollectingHandler();
        try (PushParser<ListCollectingHandler> parser =
                new FormatSelectingLogcatPushParser<>(h, Format.BRIEF, Format.THREADTIME)) {
            parser.nextLine("Some unparceable line");
            parser.nextLine(LogcatParsersTest.BRIEF_RECORD);
        }

        assertOnlyRecord(h).hasTag("MediaScanner");
    }

    @Test
    void selectsProperFormatAndContinuesToParse() {
        ListCollectingHandler h = new ListCollectingHandler();
        try (PushParser<ListCollectingHandler> parser =
                new FormatSelectingLogcatPushParser<>(h, Format.BRIEF, Format.THREADTIME)) {
            parser.nextLine(LogcatParsersTest.BRIEF_RECORD);
            parser.nextLine(LogcatParsersTest.BRIEF_RECORD_PAD);
        }
        assertThat(h.getCollectedRecords()).hasSize(2);
    }

    @Test
    void selectsProperFormatAndIgnoresFollowingLogsInOtherFormats() {
        ListCollectingHandler h = new ListCollectingHandler();
        try (PushParser<ListCollectingHandler> parser =
                new FormatSelectingLogcatPushParser<>(h, Format.BRIEF, Format.THREADTIME)) {
            parser.nextLine(LogcatParsersTest.BRIEF_RECORD);
            parser.nextLine(LogcatParsersTest.THREADTIME_RECORD);
        }
        assertThat(h.getCollectedRecords()).hasSize(1);
    }
}
