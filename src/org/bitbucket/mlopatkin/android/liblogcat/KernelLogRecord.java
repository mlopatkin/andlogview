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

import org.apache.commons.lang3.StringUtils;

/**
 * This class represents the line from the kernel log.
 */
public class KernelLogRecord {

    public static enum Severity {
        KERN_EMERG(0), // Emergency messages (precede a crash)
        KERN_ALERT(1), // Error requiring immediate attention
        KERN_CRIT(2), // Critical error (hardware or software)
        KERN_ERR(3), // Error conditions (common in drivers)
        KERN_WARNING(4), // Warning conditions (could lead to errors)
        KERN_NOTICE(5), // Not an error but a significant condition
        KERN_INFO(6), // Informational message
        KERN_DEBUG(7); // Used only for debug messages

        private final int value;

        private Severity(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "<" + value + ">";
        }

        static Severity lookupByCode(int code) {
            // not very safe - hack
            return values()[code];
        }
    }

    private final Severity severity;
    private final String timestamp;
    private final String message;

    public KernelLogRecord(Severity severity, String timestamp, String message) {
        this.severity = severity;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        if (StringUtils.isBlank(timestamp)) {
            return severity + message;
        } else {
            return severity + "[" + timestamp + "] " + message;
        }
    }
}
