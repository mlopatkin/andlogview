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

import com.google.common.base.CharMatcher;
import com.google.common.io.CharSource;
import com.google.common.io.Files;

import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

public class FileDataSourceFactory {

    private static final String DUMPSTATE_FIRST_LINE = "========================================================";
    private static final int READ_AHEAD_LIMIT = 65536;

    private FileDataSourceFactory() {
    }

    private static String getFirstNonEmptyLine(BufferedReader in) throws IOException {
        String cur = in.readLine();
        while (cur != null && CharMatcher.whitespace().matchesAllOf(cur)) {
            cur = in.readLine();
        }
        return CharMatcher.whitespace().trimFrom(cur);
    }

    public static DataSource createDataSource(File file) throws UnrecognizedFormatException,
            IOException {
        return createDataSource(file.getName(), Files.asCharSource(file, StandardCharsets.UTF_8));
    }

    public static DataSource createDataSource(String fileName, CharSource file) throws UnrecognizedFormatException,
            IOException {
        // check first non-empty line of the file
        try (BufferedReader in = file.openBufferedStream()) {
            in.mark(READ_AHEAD_LIMIT);
            String checkLine = getFirstNonEmptyLine(in);
            while (checkLine != null && LogRecordParser.isLogBeginningLine(checkLine)) {
                in.mark(READ_AHEAD_LIMIT);
                checkLine = getFirstNonEmptyLine(in);
            }
            if (checkLine == null) {
                throw new UnrecognizedFormatException("There are no non-empty lines in the file");
            }
            if (DUMPSTATE_FIRST_LINE.equals(checkLine)) {
                return createDumpstateFileSource(fileName, in);
            } else {
                return createLogFileSource(fileName, checkLine, in);
            }
        }
    }

    private static DataSource createLogFileSource(String fileName, String checkLine, BufferedReader in)
            throws IOException, UnrecognizedFormatException {
        LogfileDataSource source = LogfileDataSource.createLogfileDataSourceWithStrategy(fileName,
                                                                                         checkLine);
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
}
