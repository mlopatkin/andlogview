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

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;

public class LogRecordParser {
    private static final Logger logger = Logger.getLogger(LogRecordParser.class);

    private LogRecordParser() {
    }

    private static final String TIMESTAMP_REGEX = "(\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d)";
    private static final String ID_REGEX = "(\\d+)";
    private static final String PID_REGEX = ID_REGEX;
    private static final String TID_REGEX = ID_REGEX;
    private static final String PRIORITY_REGEX = "([AVDIWEF])";
    private static final String TAG_REGEX = "(.*?)\\s*: ";
    private static final String MESSAGE_REGEX = "(.*)";
    private static final String SEP = "\\s+";
    private static final String[] LOG_RECORD_FIELDS = { TIMESTAMP_REGEX, SEP, PID_REGEX, SEP,
            TID_REGEX, SEP, PRIORITY_REGEX, SEP, TAG_REGEX, MESSAGE_REGEX };
    private static final Pattern threadTimeRecordPattern = Pattern.compile("^"
            + StringUtils.join(LOG_RECORD_FIELDS) + "$");

    public static Matcher parseLogRecordLine(String line) {
        return threadTimeRecordPattern.matcher(line);
    }

    public static LogRecord createThreadtimeRecord(LogRecord.Kind kind, Matcher m) {
        if (!m.matches()) {
            return null;
        }
        try {
            Date dateTime = TimeFormatUtils.getTimeFromString(m.group(1));
            int pid = Integer.parseInt(m.group(2));
            int tid = Integer.parseInt(m.group(3));
            LogRecord.Priority priority = getPriorityFromChar(m.group(4));
            String tag = m.group(5);
            String message = m.group(6);
            return new LogRecord(dateTime, pid, tid, priority, tag, message, kind);
        } catch (ParseException e) {
            return new LogRecord(new Date(), -1, -1, Priority.ERROR, "Parse Error", m.group());
        }
    }

    private static LogRecord.Priority getPriorityFromChar(String next) {
        next = next.trim();
        for (LogRecord.Priority val : LogRecord.Priority.values()) {
            if (val.getLetter().equalsIgnoreCase(next)) {
                return val;
            }
        }
        throw new IllegalArgumentException("Symbol '" + next
                + "' doesn't correspond to valid priority value");
    }

}
