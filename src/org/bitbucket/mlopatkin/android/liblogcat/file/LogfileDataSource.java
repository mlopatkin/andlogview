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
package org.bitbucket.mlopatkin.android.liblogcat.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.RecordListener;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;

/**
 * This class implements simple log parser with the ability to determine actual
 * logcat output format used.
 * 
 */
public class LogfileDataSource implements DataSource {

    private static final Logger logger = Logger.getLogger(LogfileDataSource.class);

    private static final Buffer DEFAULT_BUFFER = Buffer.UNKNOWN;

    private RecordListener<LogRecord> listener;
    private ParsingStrategies.Strategy strategy;
    private List<LogRecord> records = new ArrayList<LogRecord>();
    private File file;

    private LogfileDataSource(File file, ParsingStrategies.Strategy strategy) {
        this.strategy = strategy;
        this.file = file;
        logger.debug("Strategy implemented: " + strategy);
    }

    void parse(BufferedReader in) throws IOException {
        String line = in.readLine();
        while (line != null) {
            if (!LogRecordParser.isLogBeginningLine(line) && !StringUtils.isBlank(line)) {
                LogRecord record = strategy.parse(DEFAULT_BUFFER, line);
                // sometimes we cannot handle the line well: if we didn't guess
                // the log type correctly or if there is some weird formatting
                // (probably binary output)

                // in the first case to stop and throw may be better but there
                // is no reliable way to distinguish these two cases
                // in the second case it is obviously better to ignore these
                // weird lines
                if (record != null) {
                    records.add(record);
                } else {
                    logger.debug("Null record: " + line);
                }
            }
            line = in.readLine();
        }
    }

    @Override
    public void close() {

    }

    @Override
    public EnumSet<Buffer> getAvailableBuffers() {
        return EnumSet.noneOf(Buffer.class);
    }

    @Override
    public Map<Integer, String> getPidToProcessConverter() {
        return null;
    }

    @Override
    public void setLogRecordListener(RecordListener<LogRecord> listener) {
        this.listener = listener;
        this.listener.setRecords(records);
    }

    static LogfileDataSource createLogfileDataSourceWithStrategy(File file, String checkLine)
            throws UnrecognizedFormatException {
        for (ParsingStrategies.Strategy current : ParsingStrategies.supportedStrategies) {
            if (current.parse(null, checkLine) != null) {
                return new LogfileDataSource(file, current);
            }
        }
        throw new UnrecognizedFormatException();
    }

    @Override
    public boolean reset() {
        setLogRecordListener(listener);
        return true;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
