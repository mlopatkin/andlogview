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

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.TimeFormatUtils;
import name.mlopatkin.andlogview.logmodel.Timestamp;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.utils.Try;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

class SniffingHandlerTest {

    @ParameterizedTest
    @MethodSource
    void allKidsOfLogRecordEventsAreDetectedByHandler(Function<LogcatParseEventsHandler, ParserControl> event) {
        SniffingHandler handler = new SniffingHandler();
        handler.lineConsumed();
        ParserControl result = event.apply(handler);

        assertThat(result.shouldProceed()).isFalse();
        assertThat(handler.hasParsed()).isTrue();
    }

    static List<Function<LogcatParseEventsHandler, ParserControl>> allKidsOfLogRecordEventsAreDetectedByHandler() {
        Timestamp time = Try.ofCallable(() -> TimeFormatUtils.getTimeFromString("10-01 10:00:00.000")).get();

        return Arrays.asList(
                h -> h.logRecord(1, LogRecord.Priority.DEBUG, "TAG", "message"),
                h -> h.logRecord(time, 1, 2, LogRecord.Priority.DEBUG, "TAG", "message"),
                h -> h.logRecord(time, 1, 2, LogRecord.Priority.DEBUG, "TAG", "message", null),
                h -> h.logRecord("message"),
                h -> h.logRecord(LogRecord.Priority.DEBUG, "TAG", "message"),
                h -> h.logRecord(1, 2, LogRecord.Priority.DEBUG, "message"),
                h -> h.logRecord(time, 1, LogRecord.Priority.DEBUG, "TAG", "message")
        );
    }

    @ParameterizedTest(name = "exhausting lookahead limit of {0} causes parser to stop")
    @ValueSource(ints = {1, 2, 3})
    void exhaustingLookaheadLimitCausesTheParserToStop(int lookaheadLimit) {
        SniffingHandler handler = new SniffingHandler(lookaheadLimit);

        for (int i = 1; i < lookaheadLimit; ++i) {
            assertThat(handler.lineConsumed().shouldProceed())
                    .as("should proceed at step %s after consuming the line", i)
                    .isTrue();
            assertThat(handler.unparseableLine("123").shouldProceed())
                    .as("should proceed at step %s after failing to parse the line", i)
                    .isTrue();
        }

        assertThat(handler.lineConsumed().shouldProceed())
                .as("should proceed at step %s after consuming the line", lookaheadLimit)
                .isTrue();
        assertThat(handler.unparseableLine("123").shouldProceed())
                .as("should stop at step %s after failing to parse the line", lookaheadLimit)
                .isFalse();

        assertThat(handler.lineConsumed().shouldProceed())
                .as("should stop after exhausting the limit before parsing the line")
                .isFalse();
    }
}
