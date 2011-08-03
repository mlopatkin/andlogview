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

import java.text.ParsePosition;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Pattern;

public class LogRecordParser {
    private LogRecordParser() {
    }

    private static final Pattern tagSeparator = Pattern.compile(": ");

    public static LogRecord parseThreadtimeRecord(String s) {
        ParsePosition pos = new ParsePosition(0);
        Date dateTime = TimeFormatUtils.getTimeFromString(s, pos);
        Scanner scanner = new Scanner(s.substring(pos.getIndex()));
        int pid = scanner.nextInt();
        int tid = scanner.nextInt();
        LogRecord.Priority priority = getPriorityFromChar(scanner.next());
        String tag = readTag(scanner);
        scanner.skip(tagSeparator);
        String message = scanner.nextLine();
        return new LogRecord(dateTime, pid, tid, priority, tag, message);
    }

    private static String readTag(Scanner scanner) {
        return scanner.useDelimiter(tagSeparator).next().trim();
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
