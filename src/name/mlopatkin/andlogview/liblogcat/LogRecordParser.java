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
package name.mlopatkin.andlogview.liblogcat;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.logcat.CollectingHandler;
import name.mlopatkin.andlogview.parsers.logcat.LogcatParsers;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.function.IntFunction;

/**
 * Utility class to parse log record lines in different formats.
 */
public class LogRecordParser {
    private LogRecordParser() {}

    public static @Nullable LogRecord parseThreadTime(String line, Map<Integer, String> pidToProcess) {
        return new SingleLineParser(pidToProcess::get).parse(line);
    }

    public static @Nullable LogRecord parseThreadTime(Buffer buffer, String line, Map<Integer, String> pidToProcess) {
        return new SingleLineParser(buffer, pidToProcess::get).parse(line);
    }

    private static class SingleLineParser extends CollectingHandler {
        private @Nullable LogRecord record;

        public SingleLineParser(IntFunction<String> appNameLookup) {
            super(appNameLookup);
        }

        public SingleLineParser(Buffer buffer, IntFunction<String> appNameLookup) {
            super(buffer, appNameLookup);
        }

        @Override
        protected ParserControl logRecord(LogRecord record) {
            this.record = record;
            return ParserControl.stop();
        }

        public @Nullable LogRecord parse(CharSequence line) {
            try (var parser = LogcatParsers.threadTime(this)) {
                parser.nextLine(line);
                return record;
            } finally {
                record = null;
            }
        }
    }
}
