/*
 * Copyright 2015 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.logmodel;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Fields in the log record.
 */
public enum Field {
    /**
     * The timestamp. This is an optional field with {@code null} representing the missing value.
     */
    TIME {
        @Override
        public @Nullable Timestamp getValue(LogRecord record) {
            return record.getTime();
        }
    },
    /**
     * The PID. This is an optional field with {@link LogRecord#NO_ID} representing the missing value.
     */
    PID {
        @Override
        public Integer getValue(LogRecord record) {
            return record.getPid();
        }
    },
    /**
     * The TID. This is an optional field with {@link LogRecord#NO_ID} representing the missing value.
     */
    TID {
        @Override
        public Integer getValue(LogRecord record) {
            return record.getTid();
        }
    },
    /**
     * The priority.
     */
    PRIORITY {
        @Override
        public LogRecord.Priority getValue(LogRecord record) {
            return record.getPriority();
        }
    },
    /**
     * The tag. This is an optional field with empty string representing the missing value.
     */
    TAG {
        @Override
        public String getValue(LogRecord record) {
            return record.getTag();
        }
    },
    /**
     * The message.
     */
    MESSAGE {
        @Override
        public String getValue(LogRecord record) {
            return record.getMessage();
        }
    },
    /**
     * The buffer. This is an optional field with {@code null} representing the missing value.
     */
    BUFFER {
        @Override
        public LogRecord.@Nullable Buffer getValue(LogRecord record) {
            return record.getBuffer();
        }
    },
    /**
     * The app name. This is an optional field with empty string representing the missing value.
     */
    APP_NAME {
        @Override
        public String getValue(LogRecord record) {
            return record.getAppName();
        }
    };

    /**
     * Extracts the value of this field from the given record.
     *
     * @param record the record to get the value from
     * @return the value of the field of the record
     */
    public abstract @Nullable Object getValue(LogRecord record);
}
