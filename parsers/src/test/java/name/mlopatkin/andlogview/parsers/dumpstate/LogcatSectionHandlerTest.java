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

package name.mlopatkin.andlogview.parsers.dumpstate;

import static name.mlopatkin.andlogview.logmodel.AssertLogRecord.assertThatRecord;
import static name.mlopatkin.andlogview.parsers.ParserControlAssert.assertThat;
import static name.mlopatkin.andlogview.parsers.dumpstate.SectionParserControlAssert.assertThat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.logcat.ListCollectingHandler;
import name.mlopatkin.andlogview.parsers.logcat.LogcatParseEventsHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class LogcatSectionHandlerTest {

    @Test
    void emptyLogcatSectionFinishedWithProceed() {
        var handler = createHandler(createEventsHandler());

        assertThat(handler.end()).shouldProceed();
    }

    @Test
    void emptyLogcatSectionIsNotUnparseable() {
        var eventsHandler = createEventsHandler();
        var handler = createHandler(eventsHandler);

        handler.end();

        verify(eventsHandler, never()).unparseableLogcatSection();
    }

    @Test
    void logcatSectionWithOnlyNonLogcatLinesIsUnparseable() {
        var eventsHandler = createEventsHandler();
        var handler = createHandler(eventsHandler);

        handler.nextLine("non-logcat line");
        handler.end();

        verify(eventsHandler, only()).unparseableLogcatSection();
    }

    @ParameterizedTest
    @MethodSource(value = "getLogcatLinesInSupportedFormats")
    void logcatSectionIsSkippedIfNoHandlerIsSupplied(String logcatLine) {
        var handler = createHandler(createEventsHandler());

        assertThat(handler.nextLine(logcatLine)).shouldSkip();
    }

    @ParameterizedTest
    @MethodSource(value = "getLogcatLinesInSupportedFormats")
    void logcatSectionIsParsedIfHandlerIsSupplied(String logcatLine) {
        var logcatHandler = createLogcatEventsHandler();
        var handler = createHandler(createEventsHandler(logcatHandler));

        assertThat(handler.nextLine(logcatLine)).shouldProceed();
    }

    @ParameterizedTest
    @MethodSource(value = "getLogcatLinesInSupportedFormats")
    void logcatHandlerIsCreatedLazilyWithProperBuffer(String logcatLine) {
        var eventsHandler = createEventsHandler(createLogcatEventsHandler());
        var handler = createHandler(eventsHandler);

        handler.nextLine(logcatLine);

        verify(eventsHandler, only()).logcatSectionBegin(LogRecord.Buffer.MAIN);
    }

    @ParameterizedTest
    @MethodSource(value = "getLogcatLinesInSupportedFormats")
    void logcatSectionIsNotUnparseableIfParseableLinesPresent(String logcatLine) {
        var eventsHandler = createEventsHandler(createLogcatEventsHandler());
        var handler = createHandler(eventsHandler);

        handler.nextLine("Garbage");
        handler.nextLine(logcatLine);
        handler.nextLine("Garbage");

        verify(eventsHandler, never()).unparseableLogcatSection();
    }

    @ParameterizedTest
    @MethodSource(value = "getLogcatLinesInSupportedFormats")
    void logcatSectionIsParsedWithHandler(String logcatLine) {
        var logcatHandler = createLogcatEventsHandler();
        var handler = createHandler(createEventsHandler(logcatHandler));

        handler.nextLine(logcatLine);

        assertThat(logcatHandler.getCollectedRecords())
                .hasSize(1)
                .element(0)
                .satisfies(lr ->
                        assertThatRecord(lr)
                                .hasMessage("Using psi monitors for memory pressure detection")
                );
    }

    private LogcatSectionHandler createHandler(DumpstateParseEventsHandler eventsHandler) {
        return new LogcatSectionHandler(LogRecord.Buffer.MAIN, eventsHandler);
    }

    private DumpstateParseEventsHandler createEventsHandler() {
        var handler = mock(DumpstateParseEventsHandler.class);
        when(handler.logcatSectionBegin(any())).thenReturn(Optional.empty());
        when(handler.psSectionBegin()).thenReturn(Optional.empty());
        when(handler.unparseableLogcatSection()).thenReturn(ParserControl.proceed());
        return handler;
    }

    private DumpstateParseEventsHandler createEventsHandler(LogcatParseEventsHandler logcatHandler) {
        var handler = createEventsHandler();
        when(handler.logcatSectionBegin(any())).thenReturn(Optional.of(logcatHandler));
        return handler;
    }

    private ListCollectingHandler createLogcatEventsHandler() {
        return new ListCollectingHandler();
    }

    private static List<String> getLogcatLinesInSupportedFormats() {
        return Arrays.asList(
                "I/lowmemorykiller(  188): Using psi monitors for memory pressure detection",
                "I(  188) Using psi monitors for memory pressure detection  (lowmemorykiller)",
                "I/lowmemorykiller: Using psi monitors for memory pressure detection",
                "09-11 12:52:56.962   188   188 I lowmemorykiller: Using psi monitors for memory pressure detection",
                "09-11 12:52:56.962 I/lowmemorykiller(  188): Using psi monitors for memory pressure detection"
        );
    }
}
