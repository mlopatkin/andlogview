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
package org.bitbucket.mlopatkin.android.logviewer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Kind;

public class TestDataLoader {
    private static final String filename = "test_data.txt";

    public static List<LogRecord> getRecords() {
        List<LogRecord> result = new ArrayList<LogRecord>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            try {
                String s = in.readLine();
                while (s != null) {
                    result.add(LogRecordParser.createThreadtimeRecord(Kind.UNKNOWN, LogRecordParser
                            .parseLogRecordLine(s)));
                    s = in.readLine();
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
