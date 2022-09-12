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
package name.mlopatkin.andlogview.liblogcat.file;

import name.mlopatkin.andlogview.liblogcat.LogRecordParser;
import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.logmodel.Field;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;
import name.mlopatkin.andlogview.logmodel.RecordListener;
import name.mlopatkin.andlogview.logmodel.SourceMetadata;

import com.google.common.base.CharMatcher;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class implements simple log parser with the ability to determine actual
 * logcat output format used.
 */
public class LogfileDataSource implements DataSource {
    private static final Logger logger = Logger.getLogger(LogfileDataSource.class);

    private @Nullable RecordListener<LogRecord> listener;
    private final ParsingStrategies.Strategy strategy;
    private final List<LogRecord> records = new ArrayList<>();
    private final String fileName;
    private final SourceMetadata sourceMetadata;


    private LogfileDataSource(String fileName, ParsingStrategies.Strategy strategy) {
        this.strategy = strategy;
        this.fileName = fileName;
        this.sourceMetadata = new FileSourceMetadata(new File(fileName));
        logger.debug("Strategy implemented: " + strategy);
    }

    void parse(BufferedReader in) throws IOException {
        String line = in.readLine();
        while (line != null) {
            if (!LogRecordParser.isLogBeginningLine(line) && !CharMatcher.whitespace().matchesAllOf(line)) {
                LogRecord record = strategy.parse(null, line, Collections.emptyMap());
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
    public void close() {}

    @Override
    public EnumSet<Buffer> getAvailableBuffers() {
        return EnumSet.noneOf(Buffer.class);
    }

    @Override
    public Set<Field> getAvailableFields() {
        return strategy.getAvailableFields();
    }

    @Override
    public @Nullable Map<Integer, String> getPidToProcessConverter() {
        return null;
    }

    @Override
    public void setLogRecordListener(RecordListener<LogRecord> listener) {
        this.listener = listener;
        this.listener.setRecords(records);
    }

    static LogfileDataSource createLogfileDataSourceWithStrategy(String fileName, String checkLine)
            throws UnrecognizedFormatException {
        for (ParsingStrategies.Strategy current : ParsingStrategies.supportedStrategies) {
            if (current.parse(null, checkLine, Collections.emptyMap()) != null) {
                return new LogfileDataSource(fileName, current);
            }
        }
        throw new UnrecognizedFormatException();
    }

    @Override
    public boolean reset() {
        assert listener != null;
        setLogRecordListener(listener);
        return true;
    }

    @Override
    public String toString() {
        return fileName;
    }

    @Override
    public SourceMetadata getMetadata() {
        return sourceMetadata;
    }
}
