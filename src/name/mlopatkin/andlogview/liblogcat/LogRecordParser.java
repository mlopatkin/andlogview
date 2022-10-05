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
import name.mlopatkin.andlogview.parsers.logcat.LogcatPushParser;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class to parse log record lines in different formats.
 */
public class LogRecordParser {
    private LogRecordParser() {}

    public static @Nullable LogRecord parseThreadTime(@Nullable Buffer buffer, String line,
            Map<Integer, String> pidToProcess) {
        AtomicReference<LogRecord> recordRef = new AtomicReference<>();
        try (LogcatPushParser<CollectingHandler> parser = LogcatParsers.threadTime(
                new CollectingHandler(buffer, pidToProcess::get) {
                    @Override
                    protected ParserControl logRecord(LogRecord record) {
                        recordRef.set(record);
                        return ParserControl.stop();
                    }
                })) {
            parser.nextLine(line);
        }

        return recordRef.get();
    }

}
