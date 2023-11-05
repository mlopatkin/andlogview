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

import static name.mlopatkin.andlogview.parsers.dumpstate.DumpstateParserControlAssert.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.ps.PsParseEventsHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

class DumpstatePushParserHandlerTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "PROCRANK",
            "PROCESSES AND THREADS (ps -t -p -P)",
            "KERNEL LOG (dmesg)"
    })
    void skipsUnrecognizedSection() {
        var handler = createHandler(createEventsHandler());

        assertThat(handler.sectionStarted("PROCRANK")).shouldSkip();
    }

    @Test
    void skipsPsSectionIfEventsHandlerIsEmpty() {
        var handler = createHandler(createEventsHandler());

        assertThat(handler.sectionStarted("PROCESSES (ps -P)")).shouldSkip();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SYSTEM LOG",
            "EVENT LOG (logcat -b events -v threadtime -d *:v)",
            "RADIO LOG",
    })
    void createsSubParserForLogcatSection(String sectionName) {
        var handler = createHandler(createEventsHandler());

        assertThat(handler.sectionStarted(sectionName)).hasSectionHandler();
    }

    @Test
    void createsSubParserForPsSectionIfEventsHandlerIsNotEmpty() {
        var eventsHandler = createEventsHandler();
        when(eventsHandler.psSectionBegin()).thenReturn(Optional.of(createPsEventsHandler()));

        var handler = createHandler(eventsHandler);

        assertThat(handler.sectionStarted("PROCESSES (ps -P)")).hasSectionHandler();
    }

    private DumpstatePushParserHandler createHandler(DumpstateParseEventsHandler eventsHandler) {
        return new DumpstatePushParserHandler(eventsHandler);
    }

    private DumpstateParseEventsHandler createEventsHandler() {
        var handler = mock(DumpstateParseEventsHandler.class);
        when(handler.logcatSectionBegin(any())).thenReturn(Optional.empty());
        when(handler.psSectionBegin()).thenReturn(Optional.empty());
        when(handler.unparseableLogcatSection(any())).thenReturn(ParserControl.proceed());
        return handler;
    }

    private PsParseEventsHandler createPsEventsHandler() {
        return mock(PsParseEventsHandler.class, invocation -> {
            if (invocation.getMethod().getReturnType() != void.class) {
                return ParserControl.proceed();
            }
            return null;
        });
    }
}
