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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bitbucket.mlopatkin.android.logviewer.LogRecordDataSourceListener;

public class DumpstateFileDataSource {

    private List<LogRecord> source = new ArrayList<LogRecord>();

    public DumpstateFileDataSource(LogRecordDataSourceListener listener, File file) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            readLog(in, "main");
            readLog(in, "event");
            readLog(in, "radio");
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(source, sortByTimeAscComparator);

        for (LogRecord record : source) {
            listener.onNewRecord(record);
        }

    }

    private void readLog(BufferedReader in, String bufferName) throws IOException {
        scanForLogBegin(in, bufferName);
        LogRecordStream stream = new DumpstateRecordStream(in);
        LogRecord record = stream.next();
        while (record != null) {
            source.add(record);
            record = stream.next();
        }
    }

    private void scanForLogBegin(BufferedReader in, String bufferName) throws IOException {
        bufferName = bufferName.toUpperCase();
        String logBegin = "------ " + bufferName + " LOG";
        String line = in.readLine();
        while (line != null && !line.startsWith(logBegin)) {
            line = in.readLine();
        }
    }

    private static class DumpstateRecordStream extends LogRecordStream {

        public DumpstateRecordStream(BufferedReader in) {
            super(in);
        }

        private static final String LOG_END = "[logcat:";

        @Override
        protected boolean isLogEnd(String line) {
            return super.isLogEnd(line) || line.startsWith(LOG_END);
        }
    }

    private static Comparator<LogRecord> sortByTimeAscComparator = new Comparator<LogRecord>() {

        public int compare(LogRecord o1, LogRecord o2) {
            return o1.getTime().compareTo(o2.getTime());
        }

    };
}
