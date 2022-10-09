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

package name.mlopatkin.andlogview.test;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecordPredicates;
import name.mlopatkin.andlogview.logmodel.LogRecordUtils;

import java.util.function.Predicate;

public final class TestData {
    private TestData() {}

    public static final LogRecord RECORD1 = LogRecordUtils.forTimestamp("08-03 16:21:35.538")
            .withPid(98)
            .withTid(231)
            .withPriority(LogRecord.Priority.VERBOSE)
            .withTag("AudioFlinger")
            .withMessage("start(4117), calling thread 172");

    public static final LogRecord RECORD2 = LogRecordUtils.forTimestamp("08-03 16:21:35.538")
            .withPid(98)
            .withTid(231)
            .withPriority(LogRecord.Priority.VERBOSE)
            .withTag("NotAudioFlinger")
            .withMessage("start(4117), calling thread 172");

    public static final LogRecord RECORD1_IN_MAIN = RECORD1.withBuffer(LogRecord.Buffer.MAIN);

    public static final Predicate<LogRecord> MATCH_FIRST =
            LogRecordPredicates.matchTag(Predicate.isEqual("AudioFlinger"));
    public static final Predicate<LogRecord> MATCH_SECOND =
            LogRecordPredicates.matchTag(Predicate.isEqual("NotAudioFlinger"));

    public static final Predicate<LogRecord> MATCH_ALL = logRecord -> true;
    public static final Predicate<LogRecord> MATCH_NONE = logRecord -> false;
}
