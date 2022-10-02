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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DelegateProcess extends SingleLineRegexLogcatParserDelegate {
    private static final String TAG_BRACKETS = "\\(" + TAG_REGEX + "\\)";

    private static final Pattern PATTERN =
            compileFromParts(PRIORITY_REGEX, PID_BRACKETS, " ", MESSAGE_REGEX, "  ", TAG_BRACKETS);

    public DelegateProcess(LogcatParseEventsHandler eventsHandler) {
        super(eventsHandler, PATTERN);
    }

    @Override
    protected ParserControl fromGroups(Matcher m) {
        LogRecord.Priority priority = getPriorityFromChar(m.group(1));
        int pid = Integer.parseInt(m.group(2));
        String message = m.group(3);
        String tag = m.group(4);
        eventsHandler.logRecord(pid, priority, tag, message);
        return ParserControl.proceed();
    }
}
