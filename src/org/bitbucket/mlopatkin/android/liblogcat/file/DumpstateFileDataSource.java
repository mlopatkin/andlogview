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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordDataSourceListener;
import org.bitbucket.mlopatkin.android.liblogcat.PidToProcessConverter;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;

public class DumpstateFileDataSource implements DataSource {
    private static final Logger logger = Logger.getLogger(DumpstateFileDataSource.class);
    private static final int READ_AHEAD_LIMIT = 65536;

    private List<SectionHandler> handlers = new ArrayList<SectionHandler>();

    public DumpstateFileDataSource(BufferedReader in) throws IOException, ParseException {
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
    public void reset() {

    }

    @Override
    public void setLogRecordListener(LogRecordDataSourceListener listener) {
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
}
