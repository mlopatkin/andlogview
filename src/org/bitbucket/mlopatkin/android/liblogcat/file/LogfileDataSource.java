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
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordDataSourceListener;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;
import org.bitbucket.mlopatkin.android.liblogcat.PidToProcessConverter;

/**
 * This class implements simple log parser with the ability to determine actual
 * logcat output format used.
 * 
 */
public class LogfileDataSource implements DataSource {

    private static final Logger logger = Logger.getLogger(LogfileDataSource.class);

    private static final Buffer DEFAULT_BUFFER = Buffer.UNKNOWN;

    private LogRecordDataSourceListener listener;
    private ParsingStrategy strategy;
    private List<LogRecord> records = new ArrayList<LogRecord>();

    private LogfileDataSource(ParsingStrategy strategy) {
        this.strategy = strategy;
        logger.debug("Strategy implemented: " + strategy);
    }

    void parse(BufferedReader in) throws IOException {
        String line = in.readLine();
        while (line != null) {
            if (!LogRecordParser.isLogBeginningLine(line) && !StringUtils.isBlank(line)) {
                LogRecord record = strategy.parse(DEFAULT_BUFFER, line);
                records.add(record);
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
    public PidToProcessConverter getPidToProcessConverter() {
        return null;
    }

    @Override
    public void setLogRecordListener(LogRecordDataSourceListener listener) {
        this.listener = listener;
        for (LogRecord record : records) {
            this.listener.onNewRecord(record, false);
        }
    }

    private interface ParsingStrategy {
        LogRecord parse(Buffer buffer, String line);
    }

    private static final ParsingStrategy threadTimeStrategy = new ParsingStrategy() {
        @Override
        public LogRecord parse(Buffer buffer, String line) {
            return LogRecordParser.parseThreadTime(buffer, line);
        }

        public String toString() {
            return "ThreadTimeStrategy";
        };
    };

    private static final ParsingStrategy briefStrategy = new ParsingStrategy() {
        @Override
        public LogRecord parse(Buffer buffer, String line) {
            return LogRecordParser.parseBrief(buffer, line);
        }

        public String toString() {
            return "BriefStrategy";
        };
    };

    private static final ParsingStrategy[] supportedStrategies = { threadTimeStrategy,
            briefStrategy };

    static LogfileDataSource createLogfileDataSourceWithStrategy(String checkLine)
            throws UnrecognizedFormatException {
        for (ParsingStrategy current : supportedStrategies) {
            if (current.parse(null, checkLine) != null) {
                return new LogfileDataSource(current);
            }
        }
        throw new UnrecognizedFormatException();
    }

    @Override
    public void reset() {
        setLogRecordListener(listener);
    }
}
