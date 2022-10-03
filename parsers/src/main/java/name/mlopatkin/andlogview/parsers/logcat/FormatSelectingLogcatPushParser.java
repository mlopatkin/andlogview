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

import name.mlopatkin.andlogview.parsers.PushParser;
import name.mlopatkin.andlogview.parsers.ReplayParser;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.List;

class FormatSelectingLogcatPushParser<H extends LogcatParseEventsHandler> implements PushParser<H> {
    private final H eventsHandler;

    private @Nullable ReplayParser<LogcatFormatSniffer> replaySniffer;
    private @Nullable LogcatPushParser<H> delegate;

    FormatSelectingLogcatPushParser(H eventsHandler, Format... candidates) {
        this(eventsHandler, Arrays.asList(candidates));
    }

    FormatSelectingLogcatPushParser(H eventsHandler, List<Format> candidates) {
        this.eventsHandler = eventsHandler;
        this.replaySniffer = new ReplayParser<>(new LogcatFormatSniffer(candidates));
    }

    @Override
    public H getHandler() {
        return eventsHandler;
    }

    @Override
    public boolean nextLine(CharSequence line) {
        if (delegate != null) {
            return delegate.nextLine(line);
        }
        if (replaySniffer == null) {
            return false;
        }
        boolean result = replaySniffer.nextLine(line);
        if (!result) {
            LogcatFormatSniffer sniffer = replaySniffer.getDelegate();
            if (sniffer.isFormatDetected()) {
                delegate = sniffer.createParser(eventsHandler);
                result = replaySniffer.replayInto(delegate);
            }
            replaySniffer.close();
            replaySniffer = null;
        }
        return result;
    }

    @Override
    public void close() {
        if (delegate != null) {
            delegate.close();
            return;
        }
        if (replaySniffer != null) {
            replaySniffer.close();
        }
        eventsHandler.documentEnded();
    }
}
