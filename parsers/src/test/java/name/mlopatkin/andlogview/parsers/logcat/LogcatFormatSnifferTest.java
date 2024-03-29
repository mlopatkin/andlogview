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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.parsers.ParserUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.Arrays;
import java.util.List;

class LogcatFormatSnifferTest {
    @Test
    void selectsBriefFormat() {
        try (LogcatFormatSniffer sniffer = createSniffer()) {
            assertThat(sniffer.nextLine(LogcatParsersTest.BRIEF_RECORD)).isFalse();
            assertThat(sniffer.isFormatDetected()).isTrue();
        }
    }

    @Test
    void selectsThreadTimeFormat() {
        try (LogcatFormatSniffer sniffer = createSniffer()) {
            assertThat(sniffer.nextLine(LogcatParsersTest.THREADTIME_RECORD)).isFalse();
            assertThat(sniffer.isFormatDetected()).isTrue();
        }
    }

    @Test
    void selectsProperFormatWhenPrecededWithGarbage() {
        try (LogcatFormatSniffer sniffer = createSniffer()) {
            assertThat(sniffer.nextLine("Some garbage line")).isTrue();
            assertThat(sniffer.isFormatDetected()).isFalse();

            assertThat(sniffer.nextLine(LogcatParsersTest.THREADTIME_RECORD)).isFalse();
            assertThat(sniffer.isFormatDetected()).isTrue();
        }
    }

    @Test
    void stopsAfterDetectingFormat() {
        try (LogcatFormatSniffer sniffer = createSniffer()) {
            assertThat(sniffer.nextLine(LogcatParsersTest.THREADTIME_RECORD)).isFalse();
            assertThat(sniffer.isFormatDetected()).isTrue();

            assertThat(sniffer.nextLine(LogcatParsersTest.BRIEF_RECORD)).isFalse();
            assertThat(sniffer.isFormatDetected()).isTrue();
        }
    }

    @Test
    void canCreateParserAfterDetectingFormat() {
        try (LogcatFormatSniffer sniffer = createSniffer()) {
            assertThat(sniffer.nextLine(LogcatParsersTest.THREADTIME_RECORD)).isFalse();
            assertThat(sniffer.isFormatDetected()).isTrue();

            SingleEntryParser.assertOnlyParsedRecord(sniffer::createParser, LogcatParsersTest.THREADTIME_RECORD)
                    .hasPid(417);
        }
    }

    @Test
    void detectFormatOnce() {
        try (LogcatFormatSniffer sniffer = createSniffer()) {
            sniffer.nextLine(LogcatParsersTest.THREADTIME_RECORD);
            sniffer.nextLine(LogcatParsersTest.BRIEF_RECORD);

            SingleEntryParser.assertOnlyParsedRecord(sniffer::createParser, LogcatParsersTest.THREADTIME_RECORD)
                    .hasPid(417);
        }
    }

    @Test
    void canDistinguishBriefAndTag() {
        try (LogcatFormatSniffer sniffer = new LogcatFormatSniffer(Arrays.asList(Format.BRIEF, Format.TAG))) {
            sniffer.nextLine(LogcatParsersTest.BRIEF_RECORD);

            SingleEntryParser
                    .assertOnlyParsedRecord(sniffer::createParser, LogcatParsersTest.BRIEF_RECORD)
                    .hasTag("MediaScanner")
                    .hasPid(417);
        }
    }

    @Test
    void canDistinguishTagAndBrief() {
        try (LogcatFormatSniffer sniffer = new LogcatFormatSniffer(Arrays.asList(Format.TAG, Format.BRIEF))) {
            sniffer.nextLine(LogcatParsersTest.BRIEF_RECORD);

            SingleEntryParser
                    .assertOnlyParsedRecord(sniffer::createParser, LogcatParsersTest.BRIEF_RECORD)
                    .hasTag("MediaScanner")
                    .hasPid(417);
        }
    }

    @Test
    void creatingFormatBeforeDetectingFails() {
        try (LogcatFormatSniffer sniffer = createSniffer()) {
            sniffer.nextLine("123");

            assertThatThrownBy(() -> sniffer.createParser(new ListCollectingHandler())).isInstanceOf(
                    IllegalStateException.class);
        }
    }

    @ParameterizedTest(name = "{0} with Eoln.{2}")
    @LogcatCompatSource(
            path = "parsers/logcat/golden.json",
            formats = {Format.BRIEF, Format.THREADTIME},
            eolns = {Eoln.NONE})
    void canDetectFormat(Format ignoredFormat, List<LogRecord> ignoredRecords, Eoln ignoredEoln, List<String> lines) {
        try (var sniffer = LogcatParsers.detectFormat()) {
            ParserUtils.readInto(sniffer, lines.stream());

            assertThat(sniffer.isFormatDetected()).isTrue();
        }
    }

    private LogcatFormatSniffer createSniffer() {
        return new LogcatFormatSniffer(Arrays.asList(Format.BRIEF, Format.THREADTIME));
    }
}
