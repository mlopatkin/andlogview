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

import name.mlopatkin.andlogview.base.io.LineReader;
import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.logmodel.Field;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;
import name.mlopatkin.andlogview.logmodel.RecordListener;
import name.mlopatkin.andlogview.logmodel.SourceMetadata;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.ParserUtils;
import name.mlopatkin.andlogview.parsers.logcat.CollectingHandler;
import name.mlopatkin.andlogview.parsers.logcat.LogcatParseEventsHandler;
import name.mlopatkin.andlogview.parsers.logcat.LogcatPushParser;

import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * This class implements simple log parser with the ability to determine actual
 * logcat output format used.
 */
public class LogfileDataSource implements DataSource {
    private final String fileName;
    private final Set<Field<?>> availableFields;
    private final List<LogRecord> records;
    private final SourceMetadata sourceMetadata;

    private @Nullable RecordListener<LogRecord> listener;

    private LogfileDataSource(File file, Set<Field<?>> availableFields, List<LogRecord> records) {
        this.fileName = file.getName();
        this.availableFields = availableFields;
        // records may be huge, do not copy it needlessly
        this.records = Collections.unmodifiableList(records);
        this.sourceMetadata = new FileSourceMetadata(file);
    }

    @Override
    public void close() {}

    @Override
    public EnumSet<Buffer> getAvailableBuffers() {
        return EnumSet.noneOf(Buffer.class);
    }

    @Override
    public Set<Field<?>> getAvailableFields() {
        return availableFields;
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

    public static class Builder {
        private final File file;
        private final ArrayList<LogRecord> records = new ArrayList<>();

        private @Nullable LogcatPushParser<?> pushParser;

        public Builder(File file) {
            this.file = file;
        }

        public Builder setParserFactory(Function<LogcatParseEventsHandler, LogcatPushParser<?>> factory) {
            pushParser = factory.apply(new CollectingHandler() {
                @Override
                protected ParserControl logRecord(LogRecord record) {
                    records.add(record);
                    return ParserControl.proceed();
                }
            });
            return this;
        }

        public ImportResult readFrom(LineReader in) throws IOException {
            assert pushParser != null;
            ParserUtils.readInto(pushParser, in::readLine);
            return new ImportResult(new LogfileDataSource(file, pushParser.getAvailableFields(), records));
        }
    }
}
