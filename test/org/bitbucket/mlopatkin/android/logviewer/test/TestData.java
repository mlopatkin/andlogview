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

package org.bitbucket.mlopatkin.android.logviewer.test;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordPredicates;

import java.util.Collections;

public final class TestData {
    private TestData() {
    }

    public static final LogRecord RECORD1 = LogRecordParser.parseThreadTime(
            LogRecord.Buffer.UNKNOWN,
            "08-03 16:21:35.538    98   231 V AudioFlinger: start(4117), calling thread 172",
            Collections.<Integer, String>emptyMap());

    public static final LogRecord RECORD2 = LogRecordParser.parseThreadTime(
            LogRecord.Buffer.UNKNOWN,
            "08-03 16:21:35.538    98   231 V NotAudioFlinger: start(4117), calling thread 172",
            Collections.<Integer, String>emptyMap());

    public static final LogRecord RECORD1_IN_MAIN =
            new LogRecord(RECORD1.getTime(), RECORD1.getPid(), RECORD1.getTid(), RECORD1.getAppName(),
                    RECORD1.getPriority(), RECORD1.getTag(), RECORD1.getMessage(),
                    LogRecord.Buffer.MAIN);

    public static final Predicate<LogRecord> MATCH_FIRST =
            LogRecordPredicates.matchTag(Predicates.equalTo("AudioFlinger"));
    public static final Predicate<LogRecord> MATCH_SECOND =
            LogRecordPredicates.matchTag(Predicates.equalTo("NotAudioFlinger"));

    public static final Predicate<LogRecord> MATCH_ALL = Predicates.alwaysTrue();
    public static final Predicate<LogRecord> MATCH_NONE = Predicates.alwaysFalse();
}
