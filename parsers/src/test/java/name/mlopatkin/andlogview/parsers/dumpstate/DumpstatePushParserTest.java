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

import static name.mlopatkin.andlogview.parsers.dumpstate.BaseDumpstatePushParserTest.linesWithHeader;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.ParserUtils;

import org.junit.jupiter.api.Test;

import java.util.Optional;

/*
 * Integration tests for the dumpstate push parser.
 */
class DumpstatePushParserTest {
    @Test
    void unparseableLogcatSectionIsReported() {
        var eventsHandler = createHandler();
        try (var parser = new DumpstatePushParser<>(eventsHandler)) {
            ParserUtils.readInto(parser, linesWithHeader("""
                    ------ SYSTEM LOG (logcat -v threadtime -v printable -v uid -d *:v) ------
                    Unrecognizable line 1
                    Unrecognizable line 2"""));
        }

        verify(eventsHandler).unparseableLogcatSection();
    }


    private DumpstateParseEventsHandler createHandler() {
        var handler = mock(DumpstateParseEventsHandler.class);
        when(handler.psSectionBegin()).thenReturn(Optional.empty());
        when(handler.logcatSectionBegin(any())).thenReturn(Optional.empty());
        when(handler.unparseableLogcatSection()).thenReturn(ParserControl.proceed());
        return handler;
    }
}
