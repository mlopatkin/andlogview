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
import static name.mlopatkin.andlogview.parsers.logcat.SingleEntryParser.assertOnlyParsedRecord;
import static name.mlopatkin.andlogview.utils.MyStringUtils.lines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.parsers.ParserUtils;

import com.google.common.base.Strings;

import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class DelegateLongTest {
    @Test
    void parsesEmptyMessage() {
        assertOnlyParsedRecord(LogcatParsers::logcatLong, lines("""
                [ 01-02 03:04:05.678  1234: 4321 E/sometag ]


                """))
                .hasDate(1, 2)
                .hasTime(3, 4, 5, 678)
                .hasPid(1234)
                .hasTid(4321)
                .hasPriority(LogRecord.Priority.ERROR)
                .hasTag("sometag")
                .hasMessage("");
    }

    @Test
    void parsesBlankMessage() {
        assertOnlyParsedRecord(LogcatParsers::logcatLong, lines("""
                [ 01-02 03:04:05.678  1234: 4321 E/sometag ]
                \s\s\s\t

                """))
                .hasDate(1, 2)
                .hasTime(3, 4, 5, 678)
                .hasPid(1234)
                .hasTid(4321)
                .hasPriority(LogRecord.Priority.ERROR)
                .hasTag("sometag")
                .hasMessage("\s\s\s\t");
    }

    @Test
    void parsesSingleLineMessage() {
        assertOnlyParsedRecord(LogcatParsers::logcatLong, lines("""
                [ 01-02 03:04:05.678  1234: 4321 E/sometag ]
                Some message

                """))
                .hasDate(1, 2)
                .hasTime(3, 4, 5, 678)
                .hasPid(1234)
                .hasTid(4321)
                .hasPriority(LogRecord.Priority.ERROR)
                .hasTag("sometag")
                .hasMessage("Some message");
    }

    @Test
    void parsesSingleLineMessageWithBlankEnd() {
        assertOnlyParsedRecord(LogcatParsers::logcatLong, lines("""
                [ 01-02 03:04:05.678  1234: 4321 E/sometag ]
                Some message


                """))
                .hasDate(1, 2)
                .hasTime(3, 4, 5, 678)
                .hasPid(1234)
                .hasTid(4321)
                .hasPriority(LogRecord.Priority.ERROR)
                .hasTag("sometag")
                .hasMessage("Some message");
    }

    @ParameterizedTest
    @CsvSource({
            "'First line','Second line'",
            "'','Second line'",
    })
    void parsesTwoLineMessage(String first, String second) {
        assertParsed(String.format("""
                [ 01-02 03:04:05.678  1234: 4321 E/sometag ]
                %s
                %s

                """, first, second))
                .hasSize(2)
                .allSatisfy(
                        record -> assertThatRecord(record)
                                .hasDate(1, 2)
                                .hasTime(3, 4, 5, 678)
                                .hasPid(1234)
                                .hasTid(4321)
                                .hasPriority(LogRecord.Priority.ERROR)
                                .hasTag("sometag"))
                .satisfies(r -> assertThatRecord(r).hasMessage(first), atIndex(0))
                .satisfies(r -> assertThatRecord(r).hasMessage(second), atIndex(1));
    }

    @ParameterizedTest
    @CsvSource({
            "'First line','Second line','Third line'",
            "'','Second line','Third line'",
            "'First line','','Third line'",
    })
    void parsesThreeLineMessage(String first, String second, String third) {
        assertParsed(String.format("""
                [ 01-02 03:04:05.678  1234: 4321 E/sometag ]
                %s
                %s
                %s

                """, first, second, third))
                .hasSize(3)
                .allSatisfy(
                        record -> assertThatRecord(record)
                                .hasDate(1, 2)
                                .hasTime(3, 4, 5, 678)
                                .hasPid(1234)
                                .hasTid(4321)
                                .hasPriority(LogRecord.Priority.ERROR)
                                .hasTag("sometag"))
                .satisfies(r -> assertThatRecord(r).hasMessage(first), atIndex(0))
                .satisfies(r -> assertThatRecord(r).hasMessage(second), atIndex(1))
                .satisfies(r -> assertThatRecord(r).hasMessage(third), atIndex(2));
    }


    @ParameterizedTest(name = "parses PID {1} and TID {2} out of `{0}`")
    @CsvSource({
            "' 1234: 4321', 1234, 4321",
            "'12345:54321', 12345, 54321",
            "'    1:    3', 1, 3",
            "'654321:    3', 654321, 3",
            "'    1:654321', 1, 654321",
            "'654321:123456', 654321, 123456",
    })
    void parsesPidAndTid(String pidTid, int expectedPid, int expectedTid) {
        assertOnlyParsedRecord(LogcatParsers::logcatLong, lines(String.format("""
                [ 01-01 00:00:00.000 %s E/sometag ]
                Some message

                """, pidTid)))
                .hasPid(expectedPid)
                .hasTid(expectedTid)
                .hasPriority(LogRecord.Priority.ERROR)
                .hasTag("sometag")
                .hasMessage("Some message");
    }

    @ParameterizedTest(name = "parses tag `{1}` out of `{0}`")
    @CsvSource({
            "'someLongTag', 'someLongTag'",
            "'1234578', '1234578'",
            "'auditd  ', 'auditd'", // Trailing space is dropped.
            "'A       ', 'A'", // Trailing space is dropped.
            "'  auditd', '  auditd'", // Leading space must be kept.
            "' auditd ', ' auditd'", // Leading space must be kept, trailing - dropped.
            "'////    ', '////'", // Special characters.
            "'some tag ]', 'some tag ]'", // Special characters pretend to be the end of the pattern.
            "'I/tag1 ]', 'I/tag1 ]'", // Special characters pretend to be the end of the pattern.
            "'I/t ]   ', 'I/t ]'", // Special characters pretend to be the end of the pattern.
    })
    void parsesTag(String tagString, String expectedTag) {
        assertOnlyParsedRecord(LogcatParsers::logcatLong, lines(String.format("""
                [ 01-01 00:00:00.000     1:    3 E/%s ]
                Some message

                """, tagString)))
                .hasPriority(LogRecord.Priority.ERROR)
                .hasTag(expectedTag);
    }

    @Test
    void parsesTwoSingleLineMessages() {
        assertParsed("""
                [ 01-02 03:04:05.678  1234: 4321 E/sometag ]
                First message

                [ 02-03 04:05:06.789  5678: 8765 I/othertag ]
                Second message

                """)
                .hasSize(2)
                .satisfies(r -> assertThatRecord(r).hasDate(1, 2)
                        .hasTime(3, 4, 5, 678)
                        .hasPid(1234)
                        .hasTid(4321)
                        .hasPriority(LogRecord.Priority.ERROR)
                        .hasTag("sometag")
                        .hasMessage("First message"), atIndex(0))
                .satisfies(r -> assertThatRecord(r).hasDate(2, 3)
                        .hasTime(4, 5, 6, 789)
                        .hasPid(5678)
                        .hasTid(8765)
                        .hasPriority(LogRecord.Priority.INFO)
                        .hasTag("othertag")
                        .hasMessage("Second message"), atIndex(1));
    }

    @Test
    void parsesTwoLineMessages() {
        assertParsed("""
                [ 01-02 03:04:05.678  1234: 4321 E/sometag ]
                First message, line 1
                First message, line 2

                [ 02-03 04:05:06.789  5678: 8765 I/othertag ]
                Second message

                """)
                .hasSize(3)
                .satisfies(r -> assertThatRecord(r).hasDate(1, 2)
                        .hasTime(3, 4, 5, 678)
                        .hasPid(1234)
                        .hasTid(4321)
                        .hasPriority(LogRecord.Priority.ERROR)
                        .hasTag("sometag")
                        .hasMessage("First message, line 1"), atIndex(0))
                .satisfies(r -> assertThatRecord(r).hasDate(1, 2)
                        .hasTime(3, 4, 5, 678)
                        .hasPid(1234)
                        .hasTid(4321)
                        .hasPriority(LogRecord.Priority.ERROR)
                        .hasTag("sometag")
                        .hasMessage("First message, line 2"), atIndex(1))
                .satisfies(r -> assertThatRecord(r).hasDate(2, 3)
                        .hasTime(4, 5, 6, 789)
                        .hasPid(5678)
                        .hasTid(8765)
                        .hasPriority(LogRecord.Priority.INFO)
                        .hasTag("othertag")
                        .hasMessage("Second message"), atIndex(2));
    }

    @Test
    void parsesSingleLineMessageWithBlank() {
        assertParsed("""
                [ 01-02 03:04:05.678  1234: 4321 E/sometag ]
                First message


                [ 02-03 04:05:06.789  5678: 8765 I/othertag ]
                Second message

                """)
                .hasSize(2)
                .satisfies(r -> assertThatRecord(r).hasDate(1, 2)
                        .hasTime(3, 4, 5, 678)
                        .hasPid(1234)
                        .hasTid(4321)
                        .hasPriority(LogRecord.Priority.ERROR)
                        .hasTag("sometag")
                        .hasMessage("First message"), atIndex(0))
                .satisfies(r -> assertThatRecord(r).hasDate(2, 3)
                        .hasTime(4, 5, 6, 789)
                        .hasPid(5678)
                        .hasTid(8765)
                        .hasPriority(LogRecord.Priority.INFO)
                        .hasTag("othertag")
                        .hasMessage("Second message"), atIndex(1));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 1",
            "1, 1",
            "2, 1",
            "3, 1",
            "4, 2",
            "5, 3",
            "6, 4"
    })
    @SuppressWarnings("InlineMeInliner")
    void parsesMessageWithTrailingEolns(int trailingEolnsCount, int expectedRecordsCount) {
        assertParsed("""
                [ 01-02 03:04:05.678  1234: 4321 E/sometag ]
                Some message""" + Strings.repeat("\n", trailingEolnsCount))
                .allSatisfy(
                        r -> assertThatRecord(r).hasDate(1, 2)
                                .hasTime(3, 4, 5, 678)
                                .hasPid(1234)
                                .hasTid(4321)
                                .hasPriority(LogRecord.Priority.ERROR)
                                .hasTag("sometag"))
                .hasSize(expectedRecordsCount);
    }

    @ParameterizedTest
    @ValueSource(ints = {
            1, 2, 3
    })
    @SuppressWarnings("InlineMeInliner")
    void parsesMessageWithMiddleEolns(int middleEolnsCount) {
        assertParsed(String.format("""
                [ 01-02 03:04:05.678  1234: 4321 E/sometag ]
                First line
                %s
                Last line

                """, Strings.repeat("\n", middleEolnsCount - 1)))
                .allSatisfy(
                        r -> assertThatRecord(r).hasDate(1, 2)
                                .hasTime(3, 4, 5, 678)
                                .hasPid(1234)
                                .hasTid(4321)
                                .hasPriority(LogRecord.Priority.ERROR)
                                .hasTag("sometag"))
                .hasSize(middleEolnsCount + 2);
    }

    private static ListAssert<LogRecord> assertParsed(String records) {
        var handler = new ListCollectingHandler();
        try (var parser = LogcatParsers.logcatLong(handler)) {
            ParserUtils.readInto(parser, lines(records));
        }

        return assertThat(handler.getCollectedRecords());
    }
}
