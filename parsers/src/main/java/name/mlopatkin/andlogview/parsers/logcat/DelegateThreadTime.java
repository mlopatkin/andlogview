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

class DelegateThreadTime extends SingleLineRegexLogcatParserDelegate {
    private static final String TAG = TAG_REGEX + "\\s*:(?: |$)";
    // https://cs.android.com/android/platform/superproject/main/+/main:system/logging/liblog/logprint.cpp;l=1547;drc=dd7fe3fedd9446067b06d31fdf6c191760405e6d
    private static final String UID_REGEX = "(?:\\S+ +)?";
    private static final Pattern PATTERN = Patterns.compileFromParts(
            TIMESTAMP_REGEX,
            SEP,
            UID_REGEX,
            PID_REGEX,
            SEP,
            TID_REGEX,
            SEP,
            PRIORITY_REGEX,
            SEP,
            TAG, MESSAGE_REGEX);

    public DelegateThreadTime(LogcatParseEventsHandler eventsHandler) {
        super(eventsHandler, PATTERN);
    }

    @Override
    protected ParserControl fromGroups(Matcher m) throws ParseException {
        Timestamp dateTime = TimeFormatUtils.getTimeFromString(m.group(1));
        int pid = Integer.parseInt(m.group(2));
        int tid = Integer.parseInt(m.group(3));
        LogRecord.Priority priority = LogRecord.Priority.fromChar(m.group(4));
        String tag = m.group(5);
        String message = m.group(6);
        return eventsHandler.logRecord(dateTime, pid, tid, priority, tag, message);
    }
}
