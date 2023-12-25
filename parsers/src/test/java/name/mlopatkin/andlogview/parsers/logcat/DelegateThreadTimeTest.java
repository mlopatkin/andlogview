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

package name.mlopatkin.andlogview.parsers.logcat;

import static name.mlopatkin.andlogview.parsers.logcat.SingleEntryParser.assertOnlyParsedRecord;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DelegateThreadTimeTest {
    @ParameterizedTest
    @CsvSource({
            "09-11 12:52:56.962   188   189 I lowmemorykiller: Using psi monitors for memory pressure detection"
                    + ",9,11,12,52,56,962,188,189,I,lowmemorykiller,Using psi monitors for memory pressure detection",
            "12-25 15:06:43.909598 18086 18172 I FA      : App measurement disabled by setAnalyticsCollectionEnabled"
                    + ",12,25,15,6,43,909,18086,18172,I,FA,App measurement disabled by setAnalyticsCollectionEnabled",
            "12-25 15:26:52.426  root   302   303 I Zygote  : Process 18086 exited due to signal 9 (Killed)"
                    + ",12,25,15,26,52,426,302,303,I,Zygote,Process 18086 exited due to signal 9 (Killed)",
            "12-25 15:26:52.426789  root   302   303 I Zygote  : Process 18086 exited due to signal 9 (Killed)"
                    + ",12,25,15,26,52,426,302,303,I,Zygote,Process 18086 exited due to signal 9 (Killed)",
            "12-25 15:28:23.547 10116 18452 18453 D GH.HatsManager: Stopping HatsManager."
                    + ",12,25,15,28,23,547,18452,18453,D,GH.HatsManager,Stopping HatsManager.",
            "12-25 15:28:23.547456 10116 18452 18453 D GH.HatsManager: Stopping HatsManager."
                    + ",12,25,15,28,23,547,18452,18453,D,GH.HatsManager,Stopping HatsManager."
    })
    void parsesThreadTime(String line, int month, int day, int hour, int min, int sec, int msec, int pid, int tid,
            String priority, String tag, String message) {
        assertOnlyParsedRecord(LogcatParsers::threadTime, line)
                .hasDate(month, day)
                .hasTime(hour, min, sec, msec)
                .hasPid(pid)
                .hasTid(tid)
                .hasPriority(LogRecord.Priority.fromChar(priority))
                .hasTag(tag)
                .hasMessage(message)
                .hasNoBuffer()
                .hasNoAppName();
    }
}
