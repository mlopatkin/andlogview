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

import static org.assertj.core.api.Assertions.assertThat;

import name.mlopatkin.andlogview.logmodel.AssertLogRecord;
import name.mlopatkin.andlogview.parsers.ParserUtils;
import name.mlopatkin.andlogview.parsers.PushParser;

import java.util.function.Function;
import java.util.stream.Stream;

public class SingleEntryParser {
    public static AssertLogRecord assertOnlyRecord(ListCollectingHandler collector) {
        assertThat(collector.getCollectedRecords()).hasSize(1);
        return assertThatRecord(collector.getCollectedRecords().get(0));
    }

    public static AssertLogRecord assertOnlyParsedRecord(
            Function<LogcatParseEventsHandler, PushParser<LogcatParseEventsHandler>> parserFactory,
            String line) {
        return assertOnlyParsedRecord(parserFactory, Stream.of(line));
    }

    public static AssertLogRecord assertOnlyParsedRecord(
            Function<LogcatParseEventsHandler, PushParser<LogcatParseEventsHandler>> parserFactory,
            Stream<String> lines) {
        return assertOnlyRecord(parseLines(parserFactory, lines));
    }

    private static ListCollectingHandler parseLines(
            Function<LogcatParseEventsHandler, PushParser<LogcatParseEventsHandler>> parserFactory,
            Stream<String> lines) {
        ListCollectingHandler collector = new ListCollectingHandler();
        try (PushParser<?> parser = parserFactory.apply(collector)) {
            ParserUtils.readInto(parser, lines);
        }
        return collector;
    }

}
