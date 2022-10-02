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
import name.mlopatkin.andlogview.parsers.PushParser;

import java.util.Set;

/**
 * The push parser for the logcat log. Use {@link LogcatParsers} to obtain the instance for the desired format.
 */
public class LogcatPushParser implements PushParser {
    private final Format format;
    private final RegexLogcatParserDelegate parserDelegate;
    private final LogcatParseEventsHandler eventsReceiver;

    LogcatPushParser(Format format, LogcatParseEventsHandler eventsReceiver) {
        this.format = format;
        this.parserDelegate = format.createParser(eventsReceiver);
        this.eventsReceiver = eventsReceiver;
    }

    @Override
    public boolean nextLine(CharSequence line) {
        if (!eventsReceiver.lineConsumed().shouldProceed()) {
            return false;
        }
        return parserDelegate.parseLine(line).shouldProceed();
    }

    @Override
    public void close() {
        eventsReceiver.documentEnded();
    }

    public Set<Field> getAvailableFields() {
        return format.getAvailableFields();
    }
}
