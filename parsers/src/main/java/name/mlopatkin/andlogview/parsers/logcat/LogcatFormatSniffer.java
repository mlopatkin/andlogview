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

import name.mlopatkin.andlogview.parsers.AbstractBasePushParser;
import name.mlopatkin.andlogview.parsers.FormatSniffer;
import name.mlopatkin.andlogview.parsers.MultiplexParser;
import name.mlopatkin.andlogview.parsers.ParserControl;

import com.google.common.base.Preconditions;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A special parser that detects format of the logs and can create a {@link LogcatPushParser} to parse this format.
 */
public class LogcatFormatSniffer extends AbstractBasePushParser
        implements FormatSniffer<LogcatParseEventsHandler> {
    private final MultiplexParser<LogcatPushParser<SniffingHandler>> parser;
    private @Nullable Format detectedFormat;

    LogcatFormatSniffer(List<Format> candidates) {
        parser = new MultiplexParser<>(candidates.stream()
                .map(format -> new LogcatPushParser<SniffingHandler>(format, new SniffingHandler() {
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
    public String getDetectedFormatDescription() {
        return detectedFormat != null ? detectedFormat.toString() : "not detected yet";
    }

    @Override
    public <H extends LogcatParseEventsHandler> LogcatPushParser<H> createParser(H eventsHandler) {
        Preconditions.checkState(detectedFormat != null, "The format is not yet detected");
        return new LogcatPushParser<>(detectedFormat, eventsHandler);
    }

    @Override
    protected void onNextLine(CharSequence line) {
        assert detectedFormat == null;

        boolean result = parser.nextLine(line);
        stopUnless(detectedFormat == null && result);
    }

    @Override
    public void close() {
        parser.close();
    }
}
