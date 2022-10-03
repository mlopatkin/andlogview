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

package name.mlopatkin.andlogview.parsers.logcat;

import name.mlopatkin.andlogview.parsers.BasePushParser;
import name.mlopatkin.andlogview.parsers.FormatSniffer;
import name.mlopatkin.andlogview.parsers.MultiplexParser;
import name.mlopatkin.andlogview.parsers.ParserControl;

import com.google.common.base.Preconditions;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A special parser that detects format of the logs and can create a {@link LogcatPushParser} to parse this format.
 */
public class LogcatFormatSniffer implements BasePushParser, FormatSniffer<LogcatParseEventsHandler> {
    private final MultiplexParser<LogcatPushParser<SniffingHandler>> parser;
    private @Nullable Format detectedFormat;

    LogcatFormatSniffer(List<Format> candidates) {
        this(SniffingHandler.NO_LIMIT, candidates);
    }

    LogcatFormatSniffer(int lookaheadLimit, List<Format> candidates) {
        parser = new MultiplexParser<>(candidates.stream()
                .map(format -> new LogcatPushParser<SniffingHandler>(format, new SniffingHandler(lookaheadLimit) {
                    @Override
                    public ParserControl logRecord() {
                        if (detectedFormat == null
                                || detectedFormat.getAvailableFields().size() < format.getAvailableFields().size()) {
                            detectedFormat = format;
                        }
                        return super.logRecord();
                    }
                }))
                .collect(Collectors.toList()));
    }

    @Override
    public boolean isFormatDetected() {
        return detectedFormat != null;
    }

    @Override
    public <H extends LogcatParseEventsHandler> LogcatPushParser<H> createParser(H eventsHandler) {
        Preconditions.checkState(detectedFormat != null, "The format is not yet detected");
        // NullAway cannot infer detectedFormat != null from the checkState
        // TODO(mlopatkin) remove this assert after updating NullAway to 0.9.8+, see
        //  https://github.com/uber/NullAway/issues/363
        // noinspection ConstantConditions
        assert detectedFormat != null;
        return new LogcatPushParser<>(detectedFormat, eventsHandler);
    }

    @Override
    public boolean nextLine(CharSequence line) {
        if (detectedFormat != null) {
            return false;
        }
        boolean result = parser.nextLine(line);
        return detectedFormat == null && result;
    }

    @Override
    public void close() {
        parser.close();
    }
}
