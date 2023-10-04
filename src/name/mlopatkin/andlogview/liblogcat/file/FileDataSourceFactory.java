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
import name.mlopatkin.andlogview.parsers.FormatSniffer;
import name.mlopatkin.andlogview.parsers.MultiplexParser;
import name.mlopatkin.andlogview.parsers.ReplayParser;
import name.mlopatkin.andlogview.parsers.dumpstate.DumpstateFormatSniffer;
import name.mlopatkin.andlogview.parsers.dumpstate.DumpstateParsers;
import name.mlopatkin.andlogview.parsers.logcat.LogcatFormatSniffer;
import name.mlopatkin.andlogview.parsers.logcat.LogcatParsers;

import com.google.common.io.CharSource;
import com.google.common.io.Files;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileDataSourceFactory {
    private static final Logger logger = Logger.getLogger(FileDataSourceFactory.class);
    private static final int MAX_LOOKAHEAD_LINES = 100;

    private FileDataSourceFactory() {}

    public static ImportResult createDataSource(File file) throws UnrecognizedFormatException, IOException {
        return createDataSource(file.getName(), Files.asCharSource(file, StandardCharsets.UTF_8));
    }

    public static ImportResult createDataSource(String fileName, CharSource file)
            throws UnrecognizedFormatException, IOException {
        try (LineReader in = new LineReader(file)) {
            DumpstateFormatSniffer dumpstateSniffer = DumpstateParsers.detectFormat();
            LogcatFormatSniffer logcatSniffer = LogcatParsers.detectFormat();

            try (var parser = new ReplayParser<>(
                    MAX_LOOKAHEAD_LINES,
                    new MultiplexParser<>(dumpstateSniffer, logcatSniffer))) {
                CharSequence line;
                while ((line = in.readLine()) != null) {
                    boolean parserStopped = !parser.nextLine(line);
                    if (dumpstateSniffer.isFormatDetected()) {
                        logger.debug("Recognized " + fileName + " as a dumpstate file");
                        return createDumpstateFileSource(fileName, dumpstateSniffer, parser, in);
                    } else if (logcatSniffer.isFormatDetected()) {
                        logger.debug("Recognized " + fileName + " as a logcat file");
                        return createLogFileSource(fileName, logcatSniffer, parser, in);
                    }
                    if (parserStopped) {
                        break;
                    }
                }
            }
            throw new UnrecognizedFormatException("There are no recognizable lines in the file");
        }
    }

    private static ImportResult createLogFileSource(String fileName, LogcatFormatSniffer formatSniffer,
            ReplayParser<?> replayParser, LineReader in)
            throws IOException {
        return new LogfileDataSource.Builder(fileName).setParserFactory(
                        handler -> FormatSniffer.createAndReplay(replayParser, formatSniffer::createParser, handler))
                .readFrom(in);
    }

    private static ImportResult createDumpstateFileSource(String fileName, DumpstateFormatSniffer formatSniffer,
            ReplayParser<?> replayParser, LineReader in) throws IOException, UnrecognizedFormatException {
        return new DumpstateFileDataSource.Builder(fileName).setParserFactory(
                h -> FormatSniffer.createAndReplay(replayParser, formatSniffer::createParser, h)).readFrom(in);
    }
}
