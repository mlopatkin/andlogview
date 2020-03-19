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

import com.google.common.io.CharSource;
import com.google.common.io.Files;

import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.LogFormatSniffer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;

public class FileDataSourceFactory {
    private static final int READ_AHEAD_LIMIT = 65536;

    private FileDataSourceFactory() {}

    public static DataSource createDataSource(File file) throws UnrecognizedFormatException, IOException {
        return createDataSource(file.getName(), Files.asCharSource(file, StandardCharsets.UTF_8));
    }

    public static DataSource createDataSource(String fileName, CharSource file)
            throws UnrecognizedFormatException, IOException {
        LogFormatSniffer dumpstateSniffer = new DumpstateSniffer();
        LogFormatSniffer logfileSniffer = new LogFileSniffer();

        // check first non-empty line of the file
        try (BufferedReader in = file.openBufferedStream()) {
            in.mark(READ_AHEAD_LIMIT);
            String checkLine = in.readLine();
            while (checkLine != null) {
                if (dumpstateSniffer.push(checkLine)) {
                    return createDumpstateFileSource(fileName, in);
                } else if (logfileSniffer.push(checkLine)) {
                    return createLogFileSource(fileName, checkLine, in);
                }
                in.mark(READ_AHEAD_LIMIT);
                checkLine = in.readLine();
            }
            throw new UnrecognizedFormatException("There are no recognizable lines in the file");
        }
    }

    private static DataSource createLogFileSource(String fileName, String checkLine, BufferedReader in)
            throws IOException, UnrecognizedFormatException {
        LogfileDataSource source = LogfileDataSource.createLogfileDataSourceWithStrategy(fileName, checkLine);
        in.reset();
        source.parse(in);
        return source;
    }

    private static DataSource createDumpstateFileSource(String fileName, BufferedReader in)
            throws IOException, UnrecognizedFormatException {
        try {
            return new DumpstateFileDataSource(fileName, in);
        } catch (ParseException e) {
            throw new UnrecognizedFormatException("Cannot parse dumpstate file", e);
        }
    }

    private static class DumpstateSniffer extends LogFormatSniffer.SkipNullOrEmpty {
        private static final String DUMPSTATE_FIRST_LINE = "========================================================";

        @Override
        protected boolean pushNonEmpty(String nextLine) {
            return DUMPSTATE_FIRST_LINE.equals(nextLine);
        }
    }

    private static class LogFileSniffer extends LogFormatSniffer.SkipNullOrEmpty {
        @Override
        protected boolean pushNonEmpty(String nextLine) {
            for (ParsingStrategies.Strategy strategy : ParsingStrategies.supportedStrategies) {
                if (strategy.parse(LogRecord.Buffer.UNKNOWN, nextLine, Collections.emptyMap()) != null) {
                    return true;
                }
            }
            return false;
        }
    }
}
