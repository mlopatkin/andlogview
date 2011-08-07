/*
 * Copyright 2011 Mikhail Lopatkin
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
package org.bitbucket.mlopatkin.android.liblogcat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

class ProcessListParser {
    private static final String HEADER_REGEX = "^USER\\s+PID\\s+PPID\\s+VSIZE\\s+RSS\\s+PCY\\s+WCHAN\\s+PC\\s+NAME$";
    private static final String FOOTER_REGEX = "^\\[ps: .* elapsed\\]$";

    private static final String NUMBER_REGEX = "[\\-]?\\d+";
    private static final String HEX_NUMBER_REGEX = "\\p{XDigit}+";
    private static final String SEP = "\\s+";

    private static final String USERNAME_REGEX = "\\w+";
    private static final String PID_REGEX = "(\\d+)";

    private static final String PPID_REGEX = NUMBER_REGEX;
    private static final String VSIZE_REGEX = NUMBER_REGEX;
    private static final String RSS_REGEX = NUMBER_REGEX;
    private static final String PCY_REGEX = "\\w\\w";
    private static final String WCHAN_REGEX = HEX_NUMBER_REGEX;
    private static final String PC_REGEX = HEX_NUMBER_REGEX;
    /*
     * D Uninterruptible sleep (usually IO)
     * R Running or runnable (on run queue)
     * S Interruptible sleep (waiting for an event to complete)
     * T Stopped, either by a job control signal or because it is being traced.
     * W paging (not valid since the 2.6.xx kernel)
     * X dead (should never be seen)
     * Z Defunct ("zombie") process, terminated but not reaped by its parent.
     */
    private static final String PROCESS_STATUS_REGEX = "[DRSTWXZ]";
    private static final String PROCESS_NAME = "(\\S+)";

    private static final String PS_LINE_FIELDS[] = { USERNAME_REGEX, PID_REGEX, PPID_REGEX,
            VSIZE_REGEX, RSS_REGEX, PCY_REGEX, WCHAN_REGEX, PC_REGEX, PROCESS_STATUS_REGEX,
            PROCESS_NAME };

    private static final String PS_LINE_REGEX = "^" + StringUtils.join(PS_LINE_FIELDS, SEP) + "$";

    private static final Pattern PS_LINE_PATTERN = Pattern.compile(PS_LINE_REGEX);

    public static Matcher parseProcessListLine(String line) {
        return PS_LINE_PATTERN.matcher(line);
    }

    public static int getPid(Matcher m) {
        if (m.matches()) {
            return Integer.parseInt(m.group(1));
        } else {
            throw new IllegalArgumentException("Matcher doesn't match");
        }
    }

    public static String getProcessName(Matcher m) {
        if (m.matches()) {
            return m.group(2);
        } else {
            throw new IllegalArgumentException("Matcher doesn't match");
        }
    }
}
