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
import name.mlopatkin.andlogview.logmodel.TimeFormatUtils;
import name.mlopatkin.andlogview.logmodel.Timestamp;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.Patterns;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DelegateTime extends SingleLineRegexLogcatParserDelegate {
    private static final Pattern PATTERN = Patterns.compileFromParts(
            TIMESTAMP_REGEX,
            SEP,
            PRIORITY_REGEX, "/", TAG_REGEX,
            SEP_OPT,
            PID_BRACKETS, ": ",
            MESSAGE_REGEX);

    protected DelegateTime(LogcatParseEventsHandler eventsHandler) {
        super(eventsHandler, PATTERN);
    }

    @Override
    protected ParserControl fromGroups(Matcher m) throws ParseException {
        Timestamp dateTime = TimeFormatUtils.getTimeFromString(m.group(1));
        LogRecord.Priority priority = getPriorityFromChar(m.group(2));
        String tag = m.group(3);
        int pid = Integer.parseInt(m.group(4));
        String message = m.group(5);
        return eventsHandler.logRecord(dateTime, pid, priority, tag, message);
    }
}
