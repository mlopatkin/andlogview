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

/**
 * Utility methods for printing log files in native formats.
 */
public class LogRecordFormatter {
    private LogRecordFormatter() {
    }

    public static String formatAppropriate(LogRecord record) {
        int recordMask = getRecordMask(record);
        if (checkMask(recordMask, MASK_THREADTIME_FORMAT)) {
            return formatThreadTime(record);
        } else if (checkMask(recordMask, MASK_TIME_FORMAT)) {
            return formatTime(record);
        } else {
            return formatBrief(record);
        }
    }

    public static String formatThreadTime(LogRecord record) {
        if (!checkMask(getRecordMask(record), MASK_THREADTIME_FORMAT)) {
            throw new IllegalArgumentException("Not sufficient data: " + record);
        }
        String formatString = "%s %5d %5d %s %-8s: %s";
        return String.format(formatString, TimeFormatUtils.convertTimeToString(record.getTime()),
                record.getPid(), record.getTid(), record.getPriority().getLetter(),
                record.getTag(), record.getMessage());
    }

    public static String formatTime(LogRecord record) {
        if (!checkMask(getRecordMask(record), MASK_TIME_FORMAT)) {
            throw new IllegalArgumentException("Not sufficient data: " + record);
        }
        String formatString = "%s %s/%-8s(%5d): %s";
        return String.format(formatString, TimeFormatUtils.convertTimeToString(record.getTime()),
                record.getPriority().getLetter(), record.getTag(), record.getPid(),
                record.getMessage());
    }

    public static String formatBrief(LogRecord record) {
        if (!checkMask(getRecordMask(record), MASK_BRIEF_FORMAT)) {
            throw new IllegalArgumentException("Not sufficient data: " + record);
        }
        String formatString = "%s/%-8s(%5d): %s";
        return String.format(formatString, record.getPriority().getLetter(), record.getTag(),
                record.getPid(), record.getMessage());
    }

    private static final int MASK_TIME = 1;
    private static final int MASK_PID = 2;
    private static final int MASK_TID = 4;
    private static final int MASK_TAG = 8;
    private static final int MASK_PRIORITY = 16;
    private static final int MASK_MSG = 32;

    private static final int MASK_THREADTIME_FORMAT = MASK_TIME | MASK_PID | MASK_TID | MASK_TAG
            | MASK_PRIORITY | MASK_MSG;
    private static final int MASK_TIME_FORMAT = MASK_TIME | MASK_PID | MASK_TAG | MASK_PRIORITY
            | MASK_MSG;
    private static final int MASK_BRIEF_FORMAT = MASK_PID | MASK_TAG | MASK_PRIORITY | MASK_MSG;

    private static int getRecordMask(LogRecord record) {
        int result = 0;
        if (record.getTime() != null) {
            result |= MASK_TIME;
        }
        if (record.getPid() != LogRecord.NO_ID) {
            result |= MASK_PID;
        }
        if (record.getTid() != LogRecord.NO_ID) {
            result |= MASK_TID;
        }
        if (record.getPriority() != null) {
            result |= MASK_PRIORITY;
        }
        if (record.getTag() != null) {
            result |= MASK_TAG;
        }
        if (record.getMessage() != null) {
            result |= MASK_MSG;
        }
        return result;
    }

    private static boolean checkMask(int recordMask, int targetMask) {
        return (targetMask & recordMask) == targetMask;
    }

}
