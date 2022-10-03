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

package name.mlopatkin.andlogview.logmodel;

import java.text.ParseException;

public class LogRecordBuilder {
    private LogRecord record;

    LogRecordBuilder(String message) {
        this.record = LogRecordUtils.forMessage(message);
    }

    public LogRecordBuilder withTime(String timestamp) {
        try {
            record = LogRecordUtils.withTime(record, TimeFormatUtils.getTimeFromString(timestamp));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
        return this;
    }

    public LogRecord build() {
        return record;
    }
}