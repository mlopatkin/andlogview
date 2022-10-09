/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.parsers.dumpstate;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.ReplayParser;
import name.mlopatkin.andlogview.parsers.logcat.LogcatFormatSniffer;
import name.mlopatkin.andlogview.parsers.logcat.LogcatParsers;
import name.mlopatkin.andlogview.parsers.logcat.LogcatPushParser;

import org.checkerframework.checker.nullness.qual.Nullable;

class LogcatSectionHandler implements SectionHandler {
    private static final int MAX_LOOKAHEAD_LINES = 128;

    private final ReplayParser<LogcatFormatSniffer> replayParser =
            new ReplayParser<>(MAX_LOOKAHEAD_LINES, LogcatParsers.detectFormat());
    private final LogRecord.Buffer buffer;
    private final DumpstateParseEventsHandler eventsHandler;
    private @Nullable LogcatPushParser<?> delegate;
    private boolean isSectionEmpty = true;

    public LogcatSectionHandler(LogRecord.Buffer buffer, DumpstateParseEventsHandler eventsHandler) {
        this.buffer = buffer;
        this.eventsHandler = eventsHandler;
    }

    @Override
    public SectionParserControl nextLine(CharSequence line) {
        isSectionEmpty = false;
        if (delegate != null) {
            return delegate.nextLine(line)
                    ? SectionParserControl.proceed()
                    : SectionParserControl.skipSection();
        }
        if (!replayParser.nextLine(line)) {
            LogcatFormatSniffer formatSniffer = replayParser.getDelegate();
            if (formatSniffer.isFormatDetected()) {
                boolean shouldProceed = eventsHandler.logcatSectionBegin(buffer).map(h -> {
                    delegate = formatSniffer.createParser(h);
                    boolean replayResult = replayParser.replayInto(delegate);
                    replayParser.close();
                    return replayResult;
                }).orElse(false);
                return shouldProceed ? SectionParserControl.proceed() : SectionParserControl.skipSection();
            } else {
                return SectionParserControl.skipSection();
            }
        }
        return SectionParserControl.proceed();
    }

    @Override
    public ParserControl end() {
        replayParser.close();
        if (delegate != null) {
            delegate.close();
        } else if (!isSectionEmpty) {
            return eventsHandler.unparseableLogcatSection();
        }
        return ParserControl.proceed();
    }
}
