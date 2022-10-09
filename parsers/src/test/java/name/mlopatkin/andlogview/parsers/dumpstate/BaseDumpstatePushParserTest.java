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

import static name.mlopatkin.andlogview.utils.MyStringUtils.lines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.ParserUtils;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

class BaseDumpstatePushParserTest {
    @Test
    void emptyDumpstateFileCanBeHandled() {
        var handler = createHandler();
        createParser(handler).close();
        verify(handler, only()).documentEnded();
    }

    @Test
    void dumpstateWithGarbageLinesCanBeMeaningfullyHandled() {
        var handler = createHandler();
        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, lines("""
                    Garbage in
                    Garbage out"""));
        }
        var order = inOrder(handler);
        order.verify(handler).unparseableLine("Garbage in");
        order.verify(handler).unparseableLine("Garbage out");
        order.verify(handler).documentEnded();
        order.verifyNoMoreInteractions();
    }

    @Test
    void dumpstateHeaderIsParsed() {
        var handler = createHandler();
        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, lines("""
                    ========================================================
                    == dumpstate: 2022-10-03 21:10:47
                    ========================================================
                    """));
        }
        var order = inOrder(handler);
        order.verify(handler).header();
        order.verify(handler).documentEnded();
        order.verifyNoMoreInteractions();
    }

    @Test
    void dumpstateHeaderWithGarbageInTheBeginningIsParsed() {
        var handler = createHandler();
        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, lines("""
                    SoMeJunk========================================================
                    == dumpstate: 2022-10-03 21:10:47
                    ========================================================
                    """));
        }
        var order = inOrder(handler);
        order.verify(handler).header();
        order.verify(handler).documentEnded();
        order.verifyNoMoreInteractions();
    }

    @Test
    void dumpstateHeaderWithoutTimestampIsParsed() {
        var handler = createHandler();
        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, lines("""
                    ========================================================
                    == dumpstate
                    ========================================================
                    """));
        }
        var order = inOrder(handler);
        order.verify(handler).header();
        order.verify(handler).documentEnded();
        order.verifyNoMoreInteractions();
    }

    @Test
    void headerWithUnparseableLinesInBetweenCanBeRecognized() {
        var handler = createHandler();
        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, lines("""
                    Prefix Junk
                    ========================================================
                    Junk
                    == dumpstate
                    Other junk
                    ========================================================
                    """));
        }
        var order = inOrder(handler);
        order.verify(handler).unparseableLine("Prefix Junk");
        order.verify(handler).unparseableLine("Junk");
        order.verify(handler).unparseableLine("Other junk");
        order.verify(handler).header();
        order.verify(handler).documentEnded();
        order.verifyNoMoreInteractions();
    }

    @Test
    void linesAfterHeaderAreReportedAsUnparseable() {
        var handler = createHandler();
        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, lines("""
                    ========================================================
                    == dumpstate
                    ========================================================
                    Junk
                    """));
        }
        var order = inOrder(handler);
        order.verify(handler).header();
        order.verify(handler).unparseableLine("Junk");
        order.verify(handler).documentEnded();
        order.verifyNoMoreInteractions();
    }

    @Test
    void handlerCanStopParsingAfterHeader() {
        var handler = createHandler();
        when(handler.header()).thenReturn(ParserControl.stop());

        boolean parseResult;
        try (var parser = createParser(handler)) {
            parseResult = ParserUtils.readInto(parser, lines("""
                    ========================================================
                    == dumpstate
                    ========================================================
                    Junk line
                    """));
        }

        var order = inOrder(handler);
        order.verify(handler).header();
        order.verifyNoMoreInteractions();

        assertThat(parseResult).isFalse();
    }

    @Test
    void sectionBeginEndAreReported() {
        var handler = createHandler();
        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, linesWithHeader("""
                    ------ MEMORY INFO (/proc/meminfo) ------
                    [top: 1.1s elapsed]
                    """));
        }
        var order = inOrder(handler);
        order.verify(handler).sectionStarted("MEMORY INFO (/proc/meminfo)");
        order.verify(handler).sectionEnded("MEMORY INFO (/proc/meminfo)");
        order.verify(handler).documentEnded();
        order.verifyNoMoreInteractions();
    }

    @Test
    void sectionEndIsReportedWhenDocumentEnds() {
        var handler = createHandler();
        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, linesWithHeader("""
                    ------ MEMORY INFO (/proc/meminfo) ------
                    """));
        }
        var order = inOrder(handler);
        order.verify(handler).sectionStarted("MEMORY INFO (/proc/meminfo)");
        order.verify(handler).sectionEnded("MEMORY INFO (/proc/meminfo)");
        order.verify(handler).documentEnded();
        order.verifyNoMoreInteractions();
    }

    @Test
    void sectionEndIsReportedWhenNewSectionStarts() {
        var handler = createHandler();
        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, linesWithHeader("""
                    ------ MEMORY INFO (/proc/meminfo) ------
                    ------ PROCRANK (procrank) ------
                    """));
        }
        var order = inOrder(handler);
        order.verify(handler).sectionStarted("MEMORY INFO (/proc/meminfo)");
        order.verify(handler).sectionEnded("MEMORY INFO (/proc/meminfo)");
        order.verify(handler).sectionStarted("PROCRANK (procrank)");
        order.verify(handler).sectionEnded("PROCRANK (procrank)");
        order.verify(handler).documentEnded();
        order.verifyNoMoreInteractions();
    }

    @Test
    void sectionContentIsNotUnparseableWhenSectionIsSkipped() {
        var handler = createHandler();
        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, linesWithHeader("""
                    ------ MEMORY INFO (/proc/meminfo) ------
                    Some content
                    Some other content
                    """));
        }
        var order = inOrder(handler);
        order.verify(handler).sectionStarted("MEMORY INFO (/proc/meminfo)");
        order.verify(handler, never()).unparseableLine(any());
        order.verify(handler).sectionEnded("MEMORY INFO (/proc/meminfo)");
        order.verify(handler).documentEnded();
        order.verifyNoMoreInteractions();
    }

    @Test
    void contentOutsideSectionIsUnparseable() {
        var handler = createHandler();
        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, linesWithHeader("""
                    ------ MEMORY INFO (/proc/meminfo) ------
                    Some content
                    [top: 1.1s elapsed]
                    Content outside
                    ------ PROCRANK (procrank) ------
                    """));
        }
        var order = inOrder(handler);
        order.verify(handler).sectionStarted("MEMORY INFO (/proc/meminfo)");
        order.verify(handler).sectionEnded("MEMORY INFO (/proc/meminfo)");
        order.verify(handler).unparseableLine("Content outside");
        order.verify(handler).sectionStarted("PROCRANK (procrank)");
        order.verify(handler).documentEnded();
        order.verifyNoMoreInteractions();
    }

    @Test
    void sectionHandlerReceivesEndEventWhenSectionEnds() {
        var sectionHandler = createSectionHandler();
        var handler = createHandler();
        when(handler.sectionStarted("PROCRANK")).thenReturn(DumpstateParserControl.handleWith(sectionHandler));

        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, linesWithHeader("""
                    ------ PROCRANK ------
                    procrank line
                    [top: 1.1s elapsed]

                    unparseable line
                    """));
        }

        var order = inOrder(handler, sectionHandler);
        order.verify(handler).sectionStarted("PROCRANK");
        order.verify(sectionHandler).nextLine("procrank line");
        order.verify(sectionHandler).end();
        order.verify(handler).sectionEnded("PROCRANK");
    }

    @Test
    void sectionHandlerReceivesEndEventWhenNewSectionBegins() {
        var sectionHandler = createSectionHandler();
        var handler = createHandler();
        when(handler.sectionStarted("PROCRANK")).thenReturn(DumpstateParserControl.handleWith(sectionHandler));

        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, linesWithHeader("""
                    ------ PROCRANK ------
                    procrank line
                    ------ OTHER SECTION ------
                    """));
        }

        var order = inOrder(handler, sectionHandler);
        order.verify(handler).sectionStarted("PROCRANK");
        order.verify(sectionHandler).nextLine("procrank line");
        order.verify(sectionHandler).end();
        order.verify(handler).sectionEnded("PROCRANK");
        order.verify(handler).sectionStarted("OTHER SECTION");
    }

    @Test
    void sectionHandlerReceivesEndEventWhenDocumentEnds() {
        var sectionHandler = createSectionHandler();
        var handler = createHandler();
        when(handler.sectionStarted("PROCRANK")).thenReturn(DumpstateParserControl.handleWith(sectionHandler));

        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, linesWithHeader("""
                    ------ PROCRANK ------
                    procrank line
                    """));
        }

        var order = inOrder(handler, sectionHandler);
        order.verify(handler).sectionStarted("PROCRANK");
        order.verify(sectionHandler).nextLine("procrank line");
        order.verify(sectionHandler).end();
        order.verify(handler).sectionEnded("PROCRANK");
        order.verify(handler).documentEnded();
    }

    @Test
    void sectionHandlerCanSkipSection() {
        var sectionHandler = createSectionHandler();
        var handler = createHandler();
        when(handler.sectionStarted("PROCRANK")).thenReturn(DumpstateParserControl.handleWith(sectionHandler));
        when(sectionHandler.nextLine(any())).thenReturn(SectionParserControl.skipSection());

        try (var parser = createParser(handler)) {
            ParserUtils.readInto(parser, linesWithHeader("""
                    ------ PROCRANK ------
                    procrank line
                    other procrank line
                    """));
        }

        var order = inOrder(handler, sectionHandler);
        order.verify(sectionHandler).nextLine("procrank line");
        order.verify(sectionHandler, never()).nextLine(any());
        order.verify(handler, never()).unparseableLine(any());
        order.verify(handler).sectionEnded("PROCRANK");
    }


    @Test
    void sectionHandlerCanAbortParsing() {
        var sectionHandler = createSectionHandler();
        var handler = createHandler();
        when(handler.sectionStarted("PROCRANK")).thenReturn(DumpstateParserControl.handleWith(sectionHandler));
        when(sectionHandler.nextLine(any())).thenReturn(SectionParserControl.stop());

        boolean result;
        try (var parser = createParser(handler)) {
            result = ParserUtils.readInto(parser, linesWithHeader("""
                    ------ PROCRANK ------
                    procrank line
                    other procrank line
                    """));
        }

        assertThat(result).isFalse();

        var order = inOrder(handler, sectionHandler);
        order.verify(sectionHandler).nextLine("procrank line");
        order.verifyNoMoreInteractions();
    }

    private SectionHandler createSectionHandler() {
        var handler = mock(SectionHandler.class);
        when(handler.nextLine(any())).thenReturn(SectionParserControl.proceed());
        when(handler.end()).thenReturn(ParserControl.proceed());
        return handler;
    }

    private BaseDumpstateParseEventsHandler createHandler() {
        var handler = mock(BaseDumpstateParseEventsHandler.class);
        when(handler.header()).thenReturn(ParserControl.proceed());
        when(handler.sectionStarted(any())).thenReturn(DumpstateParserControl.skipSection());
        when(handler.sectionEnded(any())).thenReturn(ParserControl.proceed());
        when(handler.unparseableLine(any())).thenReturn(ParserControl.proceed());
        return handler;
    }

    private BaseDumpstatePushParser<BaseDumpstateParseEventsHandler> createParser(
            BaseDumpstateParseEventsHandler handler) {
        return new BaseDumpstatePushParser<>(handler);
    }

    private static Stream<String> linesWithHeader(String s) {
        return Stream.concat(header(), lines(s));
    }

    private static Stream<String> header() {
        return lines("""
                ========================================================
                == dumpstate
                ========================================================
                """);
    }
}
