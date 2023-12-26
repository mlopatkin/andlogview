/*
 * Copyright 2023 the Andlogview authors
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

import name.mlopatkin.andlogview.parsers.AbstractPushParser;
import name.mlopatkin.andlogview.parsers.Patterns;

import com.google.common.base.CharMatcher;

import java.util.regex.Pattern;

/**
 * Parser for the "BLOCKED PROCESS WAIT-CHANNELS" section.
 *
 * @see <a
 *         href=
 *         "https://android.googlesource.com/platform/frameworks/base/+/57fff78a70c82ca09beb91c4f92d97b6f0b897e4">AOSP
 *         commit</a>
 */
public class ProcessWaitChannelsParser<H extends ProcessEventsHandler> extends AbstractPushParser<H> {
    //    snprintf(name_buffer, sizeof(name_buffer), "%*s%s",
    //    pid == tid ? 0 : 3, "", name);
    //
    //    printf("%-7d %-32s %s\n", tid, name_buffer, buffer);

    private static final String SEP = "\\s+";
    private static final String HEX_NUMBER_REGEX = "\\p{XDigit}+";
    private static final String IDENTIFIER_REGEX = "[A-z0-9_]+";

    // %-7d - a number right-padded with spaces to 7 characters.
    private static final String PID_REGEX = "(\\d {6}|\\d\\d {5}|\\d{3} {4}|\\d{4} {3}|\\d{5}  |\\d{6} |\\d{7,})";
    private static final String WCHAN_OPT_REGEX = "(?:" + HEX_NUMBER_REGEX + '|' + IDENTIFIER_REGEX + ")?";

    // Non-greedy parsing to avoid consuming spaces
    private static final String COMMAND_LINE_REGEX = "( {3})?(.*?)";

    private static final Pattern BLOCKED_PROCESS_PATTERN = Patterns.compileFromParts(
            // Non-greedy version of parsing %-7d
            PID_REGEX,
            " ", // The command can have leading spaces too. PID_REGEX and this separator only consume the bare minimum
            // of spaces, to preserve the leading.
            COMMAND_LINE_REGEX,
            " +",
            WCHAN_OPT_REGEX
    );

    public ProcessWaitChannelsParser(H eventsHandler) {
        super(eventsHandler);
    }

    @Override
    protected void onNextLine(CharSequence line) {
        var matcher = BLOCKED_PROCESS_PATTERN.matcher(line);
        if (matcher.matches()) {
            int id = Integer.parseInt(CharMatcher.whitespace().trimFrom(matcher.group(1)));
            var isThread = matcher.group(2) != null;
            var name = trimSpaces(matcher.group(3));
            if (isThread) {
                if (name.isEmpty()) {
                    getHandler().unknownKernelThread(id);
                } else {
                    getHandler().thread(id, name);
                }
            } else {
                getHandler().process(id, name);
            }
        } else {
            getHandler().unparseableLine(line);
        }
    }

    private static String trimSpaces(CharSequence str) {
        return CharMatcher.whitespace().trimFrom(str);
    }
}
