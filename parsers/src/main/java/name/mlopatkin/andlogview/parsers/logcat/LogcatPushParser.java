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

import name.mlopatkin.andlogview.logmodel.Field;
import name.mlopatkin.andlogview.parsers.AbstractPushParser;

import java.util.Set;

/**
 * The push parser for the logcat log. Use {@link LogcatParsers} to obtain the instance for the desired format.
 */
public class LogcatPushParser<H extends LogcatParseEventsHandler> extends AbstractPushParser<H> {
    private final Format format;
    private final RegexLogcatParserDelegate parserDelegate;

    LogcatPushParser(Format format, H eventsHandler) {
        super(eventsHandler);
        this.format = format;
        this.parserDelegate = format.createParser(eventsHandler);
    }

    @Override
    protected void onNextLine(CharSequence line) {
        stopUnless(parserDelegate.parseLine(line).shouldProceed());
    }

    @Override
    public void close() {
        parserDelegate.close();
        super.close();
    }

    public Set<Field<?>> getAvailableFields() {
        return format.getAvailableFields();
    }
}
