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
import name.mlopatkin.andlogview.logmodel.order.OfflineSorter;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.ParserUtils;
import name.mlopatkin.andlogview.parsers.PushParser;
import name.mlopatkin.andlogview.parsers.dumpstate.DumpstateParseEventsHandler;
import name.mlopatkin.andlogview.parsers.dumpstate.ProcessEventsHandler;
import name.mlopatkin.andlogview.parsers.logcat.CollectingHandler;
import name.mlopatkin.andlogview.parsers.logcat.LogcatParseEventsHandler;
import name.mlopatkin.andlogview.parsers.ps.PsParseEventsHandler;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class DumpstateFileDataSource implements DataSource {
    private static final Logger logger = Logger.getLogger(DumpstateFileDataSource.class);

    private final String fileName;
    private final SourceMetadata sourceMetadata;

    private final List<LogRecord> records;
    private final Set<Field<?>> availableFields;
    private final EnumSet<Buffer> buffers;
    private final Map<Integer, String> converter;

    private @Nullable RecordListener<LogRecord> logcatListener;

    private DumpstateFileDataSource(File file, List<LogRecord> records, Set<Field<?>> availableFields,
            EnumSet<Buffer> buffers, Map<Integer, String> converter) {
        this.fileName = file.getName();
        this.sourceMetadata = new FileSourceMetadata(file);
        this.records = records;
        this.availableFields = availableFields;
        this.buffers = buffers;
        this.converter = converter;
    }

    @Override
    public void close() {}

    @Override
    public EnumSet<Buffer> getAvailableBuffers() {
        return buffers;
    }

    @Override
    public Set<Field<?>> getAvailableFields() {
        return availableFields;
    }

    @Override
    public Map<Integer, String> getPidToProcessConverter() {
        return converter;
    }

    @Override
    public boolean reset() {
        if (logcatListener != null) {
            setLogRecordListener(logcatListener);
        }
        return true;
    }

    @Override
    public void setLogRecordListener(RecordListener<LogRecord> listener) {
        this.logcatListener = listener;
        listener.setRecords(records);
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
        private final EnumSet<Buffer> availableBuffers = EnumSet.noneOf(Buffer.class);
        private final List<LogRecord> records = new ArrayList<>();
        private final Map<Integer, String> pidToProcessConverter = new HashMap<>();
        private final List<ImportProblem> problems = new ArrayList<>();

        private @Nullable PushParser<?> pushParser;

        private boolean hasPsSection;
        private boolean psSectionHadUnparseableLines;

        // ProcessWait section is available since about Android 4.0.1 and sometimes provides better insights.
        // At least, it has less variability between versions.
        private boolean hasProcessWaitSection;
        private boolean processWaitSectionHadUnparseableLines;

        public Builder(File file) {
            this.file = file;
        }

        public Builder setParserFactory(
                Function<DumpstateParseEventsHandler, PushParser<DumpstateParseEventsHandler>> factory) {
            pushParser = factory.apply(new DumpstateParseEventsHandler() {
                @Override
                public Optional<LogcatParseEventsHandler> logcatSectionBegin(Buffer buffer) {
                    availableBuffers.add(buffer);
                    return Optional.of(new CollectingHandler(buffer, pidToProcessConverter::get) {
                        @Override
                        protected ParserControl logRecord(LogRecord record) {
                            records.add(record);
                            return ParserControl.proceed();
                        }

                        @Override
                        public ParserControl unparseableLine(CharSequence line) {
                            if (line.length() > 0) {
                                logger.debug("Failed to parse dumpstate logcat line: " + line);
                            }
                            return ParserControl.proceed();
                        }
                    });
                }

                @Override
                public Optional<PsParseEventsHandler> psSectionBegin() {
                    hasPsSection = true;
                    return Optional.of(new PsParseEventsHandler() {
                        @Override
                        public ParserControl processLine(int pid, String processName) {
                            pidToProcessConverter.put(pid, processName);
                            return ParserControl.proceed();
                        }

                        @Override
                        public ParserControl unparseableLine(CharSequence line) {
                            psSectionHadUnparseableLines = true;
                            logger.debug("Failed to parse ps output line: " + line);
                            return ParserControl.proceed();
                        }
                    });
                }

                @Override
                public Optional<ProcessEventsHandler> processTimesSectionBegin() {
                    hasProcessWaitSection = true;
                    return Optional.of(new ProcessEventsHandler() {
                        @Override
                        public ParserControl process(int pid, String processName) {
                            pidToProcessConverter.putIfAbsent(pid, processName);
                            return ParserControl.proceed();
                        }

                        @Override
                        public ParserControl unknownKernelThread(int tid) {
                            // PS typically has a better output for kernel threads
                            pidToProcessConverter.putIfAbsent(tid, "???");
                            return ParserControl.proceed();
                        }

                        @Override
                        public ParserControl unparseableLine(CharSequence line) {
                            processWaitSectionHadUnparseableLines = true;
                            return ParserControl.proceed();
                        }
                    });
                }

                @Override
                public ParserControl unparseableLogcatSection(Buffer buffer) {
                    problems.add(new ImportProblem("Failed to import logcat entries for " + buffer.getCaption()));
                    return ParserControl.stop();
                }
            });
            return this;
        }

        public ImportResult readFrom(LineReader in) throws IOException, UnrecognizedFormatException {
            ParserUtils.readInto(Objects.requireNonNull(pushParser), in::readLine);

            if (availableBuffers.isEmpty()) {
                throw new UnrecognizedFormatException("Cannot load dumpstate file, no valid logcat section found");
            }
            if (!hasPsSection && !hasProcessWaitSection) {
                problems.add(
                        new ImportProblem("Failed to find Processes section. Application names are not available."));
            } else if ((psSectionHadUnparseableLines || processWaitSectionHadUnparseableLines)
                    && pidToProcessConverter.isEmpty()) {
                problems.add(
                        new ImportProblem("Failed to parse Processes section. Application names are not available."));
            } else {
                updateProcesses();
            }

            OfflineSorter sorter = new OfflineSorter();
            records.forEach(sorter::add);

            if (sorter.hasTimeTravels()) {
                problems.add(new ImportProblem(
                        "Dumpstate file has time travels. Record ordering across buffers may not be consistent."));
            }
            return new ImportResult(
                    new DumpstateFileDataSource(
                            file, sorter.buildTimestampOrdered(), Field.values(), availableBuffers,
                            pidToProcessConverter),
                    problems);
        }

        private void updateProcesses() {
            var iterator = records.listIterator();
            while (iterator.hasNext()) {
                var record = iterator.next();
                final String appName;
                if (!record.hasAppName() && (appName = pidToProcessConverter.get(record.getPid())) != null) {
                    iterator.set(record.withAppName(appName));
                }
            }
        }
    }
}
