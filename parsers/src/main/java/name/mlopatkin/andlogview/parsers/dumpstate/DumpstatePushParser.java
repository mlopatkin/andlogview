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

import name.mlopatkin.andlogview.parsers.BasePushParser;
import name.mlopatkin.andlogview.parsers.DelegatingParser;
import name.mlopatkin.andlogview.parsers.PushParser;

/**
 * A Dumpstate parser that understands more specific aspects of the dumpstate file. It can parse logcat and ps sections.
 *
 * @param <H> the type of the handler
 */
class DumpstatePushParser<H extends DumpstateParseEventsHandler> extends DelegatingParser<BasePushParser>
        implements PushParser<H> {
    private final H eventsHandler;

    public DumpstatePushParser(H eventsHandler) {
        super(new BaseDumpstatePushParser<>(new DumpstatePushParserHandler(eventsHandler)));
        this.eventsHandler = eventsHandler;
    }

    @Override
    public H getHandler() {
        return eventsHandler;
    }
}
