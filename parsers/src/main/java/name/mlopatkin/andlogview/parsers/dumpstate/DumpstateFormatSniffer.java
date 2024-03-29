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

import name.mlopatkin.andlogview.parsers.AbstractBasePushParser;
import name.mlopatkin.andlogview.parsers.FormatSniffer;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.PushParser;

/**
 * A special parser to check if the input is actually a dumpstate. Current implementation searches for dumpstate header.
 */
public class DumpstateFormatSniffer extends AbstractBasePushParser
        implements FormatSniffer<DumpstateParseEventsHandler> {
    private final BaseDumpstatePushParser<Handler> parser = new BaseDumpstatePushParser<>(new Handler());

    @Override
    protected void onNextLine(CharSequence line) {
        stopUnless(parser.nextLine(line));
    }

    @Override
    public void close() {
        parser.close();
    }

    @Override
    public boolean isFormatDetected() {
        return parser.getHandler().headerFound;
    }

    @Override
    public String getDetectedFormatDescription() {
        return isFormatDetected() ? "dumpstate file" : "not detected yet";
    }

    @Override
    public <H extends DumpstateParseEventsHandler> PushParser<H> createParser(H eventsHandler) {
        return new DumpstatePushParser<>(eventsHandler);
    }

    private static class Handler implements BaseDumpstateParseEventsHandler {
        boolean headerFound;

        @Override
        public ParserControl header() {
            headerFound = true;
            return ParserControl.stop();
        }
    }
}
