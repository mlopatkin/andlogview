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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;
import org.bitbucket.mlopatkin.android.liblogcat.ProcessListParser;
import org.bitbucket.mlopatkin.android.liblogcat.RecordListener;
import org.bitbucket.mlopatkin.android.liblogcat.file.ParsingStrategies.Strategy;
import org.bitbucket.mlopatkin.android.logviewer.Configuration;

public class DumpstateFileDataSource implements DataSource {
    private static final Logger logger = Logger.getLogger(DumpstateFileDataSource.class);
    private static final int READ_AHEAD_LIMIT = 65536;

    private List<SectionHandler> handlers = new ArrayList<SectionHandler>();
    private List<LogRecord> records = new ArrayList<LogRecord>();
    private EnumSet<Buffer> buffers = EnumSet.noneOf(Buffer.class);
    private RecordListener<LogRecord> listener;

    private File file;

    public DumpstateFileDataSource(File file, BufferedReader in) throws IOException, ParseException {
        this.file = file;
        initSectionHandlers();
        parseFile(in);
    }

    private void parseFile(BufferedReader in) throws IOException, ParseException {
        String line = in.readLine();
        while (line != null) {
            String sectionName = getSectionName(line);
            if (sectionName != null) {
                parseSection(in, sectionName);
            }
            line = in.readLine();
        }
    }

    private void parseSection(BufferedReader in, String sectionName) throws IOException,
            ParseException {
        SectionHandler handler = getSectionHandler(sectionName);
        if (handler == null) {
            return;
        }
        handler.startSection(sectionName);
        in.mark(READ_AHEAD_LIMIT);
        String line = in.readLine();
        while (line != null) {
            if (getSectionName(line) != null) {
                // found start of a new section
                in.reset();
                break;
            }
            boolean shouldBreak = !handler.handleLine(line);
            if (shouldBreak) {
                // handler reported that his section is over
                break;
            }
            in.mark(READ_AHEAD_LIMIT);
            line = in.readLine();
        }
        handler.endSection();
    }

    private SectionHandler getSectionHandler(String sectionName) {
        for (SectionHandler handler : handlers) {
            if (handler.isSupportedSection(sectionName)) {
                return handler;
            }
        }
        logger.debug("Unsupported section: " + sectionName);
        return null;
    }

    private void initSectionHandlers() {
        handlers.add(new LogcatSectionHandler());
        handlers.add(new ProcessesSectionHandler());
    }

    @Override
    public void close() {

    }

    @Override
    public EnumSet<Buffer> getAvailableBuffers() {
        return buffers;
    }

    @Override
    public Map<Integer, String> getPidToProcessConverter() {
        return converter;
    }

    @Override
    public boolean reset() {
        setLogRecordListener(listener);
        return true;
    }

    @Override
    public void setLogRecordListener(RecordListener<LogRecord> listener) {
        this.listener = listener;
        Collections.sort(records);
        listener.setRecords(records);
    }

    /**
     * Handles one section of the dumpstate file
     */
    private interface SectionHandler {
        /**
         * Checks if the implementation supports some section.
         * 
         * @param sectionName
         *            section name as appears in the file without wrapping
         *            dashes
         * @return {@code true} if the implementation can handle this section
         */
        boolean isSupportedSection(String sectionName);

        /**
         * Handles one line from the file.
         * 
         * @param line
         *            one line from the file (not null but can be empty)
         * @return {@code true} if the line wasn't last line in section and the
         *         handler is expecting more
         */
        boolean handleLine(String line) throws ParseException;

        /**
         * Called when the section ends due to end of the file or because other
         * section starts.
         */
        void endSection();

        /**
         * Starts section processing.
         */
        void startSection(String sectionName);
    }

    private static final Pattern SECTION_NAME_PATTERN = Pattern.compile("^------ (.*) ------$");

    private static String getSectionName(String line) {
        Matcher m = SECTION_NAME_PATTERN.matcher(line);
        if (m.matches()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    private static final Pattern SECTION_END_PATTERN = Pattern.compile("^\\[.*: .* elapsed\\]$");

    private void addLogRecord(LogRecord record) {
        records.add(record);
    }

    private void addBuffer(Buffer buffer) {
        buffers.add(buffer);
    }

    private class LogcatSectionHandler implements SectionHandler {

        private Buffer buffer;
        private Strategy parsingStrategy;

        @Override
        public void endSection() {
            assert buffer != null;
            addBuffer(buffer);
        }

        @Override
        public boolean handleLine(String line) throws ParseException {
            if (isEnd(line)) {
                return false;
            }
            if (StringUtils.isBlank(line) || LogRecordParser.isLogBeginningLine(line)) {
                return true;
            }
            if (parsingStrategy == null) {
                parsingStrategy = chooseParsingStrategy(line);
            }
            assert buffer != null;
            LogRecord record = parsingStrategy.parse(buffer, line);
            if (record == null) {
                logger.debug("Null record: " + line);
            } else {
                addLogRecord(record);
            }
            return true;
        }

        private Strategy chooseParsingStrategy(String line) throws ParseException {
            for (Strategy strategy : ParsingStrategies.supportedStrategies) {
                if (strategy.parse(null, line) != null) {
                    return strategy;
                }
            }
            throw new ParseException("Cannot figure out log format from " + line, 0);
        }

        @Override
        public boolean isSupportedSection(String sectionName) {
            for (Buffer buffer : Buffer.values()) {
                String header = Configuration.dump.bufferHeader(buffer);
                if (header != null && sectionName.startsWith(header + " LOG")) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void startSection(String sectionName) {
            parsingStrategy = null;
            buffer = getBufferFromName(sectionName);
        }

        private Buffer getBufferFromName(String sectionName) {
            for (Buffer buffer : Buffer.values()) {
                String header = Configuration.dump.bufferHeader(buffer);
                if (header != null && sectionName.startsWith(header)) {
                    return buffer;
                }
            }
            return Buffer.UNKNOWN;
        }

        private boolean isEnd(String line) {
            return SECTION_END_PATTERN.matcher(line).matches();
        }
    }

    private static final String PROCESSES_SECTION = "PROCESSES (ps -P)";
    private Map<Integer, String> converter = new HashMap<Integer, String>();

    private class ProcessesSectionHandler implements SectionHandler {

        @Override
        public void endSection() {
        }

        @Override
        public boolean handleLine(String line) throws ParseException {
            if (isEnd(line)) {
                return false;
            }

            if (StringUtils.isBlank(line) || ProcessListParser.isProcessListHeader(line)) {
                return true;
            }
            Matcher m = ProcessListParser.parseProcessListLine(line);
            converter.put(ProcessListParser.getPid(m), ProcessListParser.getProcessName(m));
            return true;
        }

        @Override
        public boolean isSupportedSection(String sectionName) {
            return PROCESSES_SECTION.equalsIgnoreCase(sectionName);
        }

        @Override
        public void startSection(String sectionName) {
        }

        private boolean isEnd(String line) {
            return SECTION_END_PATTERN.matcher(line).matches();
        }

    }

    @Override
    public String toString() {
        return file.getName();
    }
}
