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

import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.ps.PsPushParser;

/**
 * An implementation of {@link BaseDumpstateParseEventsHandler} that converts low-level section parsing events into more
 * high-level logcat and ps sections. Capable of auto-detecting the logcat format.
 */
class DumpstatePushParserHandler implements BaseDumpstateParseEventsHandler {
    private final DumpstateParseEventsHandler eventsHandler;

    DumpstatePushParserHandler(DumpstateParseEventsHandler eventsHandler) {
        this.eventsHandler = eventsHandler;
    }

    @Override
    public DumpstateParserControl sectionStarted(String sectionName) {
        if (DumpstateElements.isProcessSection(sectionName)) {
            return eventsHandler.psSectionBegin()
                    .map(PsPushParser::new)
                    .map(SectionHandler::withSubParser)
                    .map(DumpstateParserControl::handleWith)
                    .orElse(DumpstateParserControl.skipSection());
        }

        if (DumpstateElements.isLogcatSection(sectionName)) {
            return DumpstateElements.getBufferFromLogcatSectionName(sectionName)
                    .map(buffer -> new LogcatSectionHandler(buffer, eventsHandler))
                    .map(DumpstateParserControl::handleWith)
                    .orElse(DumpstateParserControl.skipSection());
        }
        return DumpstateParserControl.skipSection();
    }

    @Override
    public ParserControl sectionEnded(String sectionName) {
        return ParserControl.proceed();
    }

    @Override
    public ParserControl lineConsumed() {
        return eventsHandler.lineConsumed();
    }

    @Override
    public void documentEnded() {
        eventsHandler.documentEnded();
    }
}
