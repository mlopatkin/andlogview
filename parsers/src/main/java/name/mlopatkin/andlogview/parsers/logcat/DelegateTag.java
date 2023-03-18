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

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DelegateTag extends SingleLineRegexLogcatParserDelegate {
    private static final Pattern PATTERN =
            Patterns.compileFromParts(PRIORITY_REGEX, "/", TAG_REGEX, ": ", MESSAGE_REGEX);

    public DelegateTag(LogcatParseEventsHandler eventsHandler) {
        super(eventsHandler, PATTERN);
    }

    @Override
    protected ParserControl fromGroups(Matcher m) {
        LogRecord.Priority priority = LogRecord.Priority.fromChar(m.group(1));
        String tag = m.group(2);
        String message = m.group(3);
        eventsHandler.logRecord(priority, tag, message);
        return ParserControl.proceed();
    }
}
