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

import name.mlopatkin.andlogview.parsers.ParserControl;

abstract class RegexLogcatParserDelegate {
    static final String TIMESTAMP_REGEX = "(?:\\d\\d\\d\\d-)?(\\d\\d-\\d\\d "
            + "\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d)(?:\\d\\d\\d)?";
    static final String ID_REGEX = "(\\d+)";
    static final String PID_REGEX = ID_REGEX;
    static final String PID_BRACKETS = "\\(\\s*" + PID_REGEX + "\\)";

    static final String TID_REGEX = ID_REGEX;
    static final String TAG_REGEX = "(.*?)";
    static final String PRIORITY_REGEX = "([AVDIWEF])";
    static final String MESSAGE_REGEX = "(.*)";
    static final String SEP = "\\s+";
    static final String SEP_OPT = "\\s*";

    protected final LogcatParseEventsHandler eventsHandler;

    RegexLogcatParserDelegate(LogcatParseEventsHandler eventsHandler) {
        this.eventsHandler = eventsHandler;
    }

    public abstract ParserControl parseLine(CharSequence line);
}
