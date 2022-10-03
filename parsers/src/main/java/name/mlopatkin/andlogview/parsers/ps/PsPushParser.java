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

package name.mlopatkin.andlogview.parsers.ps;

import name.mlopatkin.andlogview.parsers.Patterns;
import name.mlopatkin.andlogview.parsers.PushParser;
import name.mlopatkin.andlogview.utils.LineParser;
import name.mlopatkin.andlogview.utils.LineParser.State;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PsPushParser<H extends PsParseEventsHandler> implements PushParser<H> {
    private static final String HEADER_REGEX =
            "^USER\\s+PID\\s+PPID\\s+(VSIZE|VSZ)\\s+RSS\\s+(PCY\\s+)?WCHAN\\s+(PC|ADDR)\\s+(S\\s+)?NAME\\s*$";

    private static final String NUMBER_REGEX = "[\\-]?\\d+";
    private static final String HEX_NUMBER_REGEX = "\\p{XDigit}+";
    private static final String IDENTIFIER_REGEX = "[A-z0-9_]+";
    private static final String SEP = "\\s+";

    private static final String USERNAME_REGEX = "\\w+";
    private static final String PID_REGEX = "(\\d+)";

    private static final String PPID_REGEX = NUMBER_REGEX;
    private static final String VSIZE_REGEX = NUMBER_REGEX;
    private static final String RSS_REGEX = NUMBER_REGEX;
    private static final String PCY_REGEX = "(?:\\w\\w\\s+)?";
    private static final String WCHAN_REGEX = "(?:" + HEX_NUMBER_REGEX + '|' + IDENTIFIER_REGEX + ")?";
    private static final String PC_REGEX = HEX_NUMBER_REGEX;
    /*
     * D Uninterruptible sleep (usually IO)
     * I Idle kernel thread
     * R Running or runnable (on run queue)
     * S Interruptible sleep (waiting for an event to complete)
     * T Stopped, either by a job control signal or because it is being traced.
     * t stopped by debugger during the tracing
     * W paging (not valid since the 2.6.xx kernel)
     * X dead (should never be seen)
     * Z Defunct ("zombie") process, terminated but not reaped by its parent.
     */
    private static final String PROCESS_STATUS_REGEX = "[DIRSTtWXZ]";
    private static final String PROCESS_NAME = "(.*)";

    private static final Pattern PS_LINE_PATTERN = Patterns.joinAndCompileFromParts(SEP,
            USERNAME_REGEX,
            PID_REGEX,
            PPID_REGEX,
            VSIZE_REGEX,
            RSS_REGEX,
            PCY_REGEX + WCHAN_REGEX,
            PC_REGEX,
            PROCESS_STATUS_REGEX,
            PROCESS_NAME);

    private final H eventsHandler;
    private final LineParser lineParser;

    private boolean shouldStop;

    public PsPushParser(H eventsHandler) {
        this.eventsHandler = eventsHandler;
        this.lineParser = new LineParser(this::seekHeader);
    }

    @Override
    public H getHandler() {
        return eventsHandler;
    }

    @Override
    public boolean nextLine(CharSequence line) {
        if (shouldStop || !eventsHandler.lineConsumed().shouldProceed()) {
            return false;
        }

        lineParser.nextLine(line);
        return !shouldStop;
    }

    @Override
    public void close() {
        eventsHandler.documentEnded();
    }

    private State seekHeader(CharSequence line) {
        if (isProcessListHeader(line)) {
            shouldStop = !eventsHandler.header().shouldProceed();
            return shouldStop ? LineParser.sinkState() : this::parseProcessListLine;
        } else {
            shouldStop = !eventsHandler.unparseableLine(line).shouldProceed();
        }
        return shouldStop ? LineParser.sinkState() : LineParser.currentState();
    }

    private State parseProcessListLine(CharSequence line) {
        Matcher m = PS_LINE_PATTERN.matcher(line);
        if (m.matches()) {
            shouldStop = !eventsHandler.processLine(getPid(m), getProcessName(m)).shouldProceed();
        } else {
            shouldStop = !eventsHandler.unparseableLine(line).shouldProceed();
        }
        return shouldStop ? LineParser.sinkState() : LineParser.currentState();
    }

    private static int getPid(MatchResult m) {
        return Integer.parseInt(m.group(1));
    }

    private static String getProcessName(MatchResult m) {
        return m.group(2);
    }

    private static boolean isProcessListHeader(CharSequence line) {
        return Pattern.matches(HEADER_REGEX, line);
    }
}
