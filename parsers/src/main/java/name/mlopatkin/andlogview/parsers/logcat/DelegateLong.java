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

import name.mlopatkin.andlogview.logmodel.LogRecord.Priority;
import name.mlopatkin.andlogview.logmodel.TimeFormatUtils;
import name.mlopatkin.andlogview.logmodel.Timestamp;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.Patterns;
import name.mlopatkin.andlogview.utils.LineParser;
import name.mlopatkin.andlogview.utils.LineParser.State;

import com.google.common.base.CharMatcher;

import org.jspecify.annotations.Nullable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Long output format supports multi-line messages natively. Each message looks like:
 * <pre>
 * [ 09-11 12:52:56.962   188:  188 I/lowmemorykiller ]
 * Using psi monitors for memory pressure detection
 *
 * </pre>
 * It consists of a header (first line), and the rest of the message. The message is finalized with double EOLN
 * {@code \n\n}.
 * <p>
 * Even though the paragraph above talks about EOLNs, this delegate only expects lines without end-of-line characters.
 */
class DelegateLong extends RegexLogcatParserDelegate {
    private static final Pattern HEADER_PATTERN = Patterns.compileFromParts(
            "\\[ ",
            TIMESTAMP_REGEX,
            " ",
            SEP_OPT, // TODO(mlopatkin): actually, an uid can be there.
            // pid:tid, e.g. " 1172: 1172" or "12345:12345"
            // tid can be hexadecimal
            // see https://android.googlesource.com/platform/system/logging/+/1c3851559e27b532e5c6768cbb79033de24b0d31
            PID_REGEX, ":", SEP_OPT, TID_REGEX,
            " ",
            // priority and left-aligned tag. Tag has to be trimmed before feeding it upstream.
            PRIORITY_REGEX, "/", TAG_REGEX,
            " ",
            "\\]"
    );
    private static final Pattern CONTROL_PATTERN = Pattern.compile(CONTROL_LINE_REGEX);

    private @Nullable CurrentMessage currentMessage;

    private final LineParser lineParser;

    DelegateLong(LogcatParseEventsHandler eventsHandler) {
        super(eventsHandler);

        lineParser = new LineParser(this::stateSeekFirstHeader);
    }

    private State stateSeekFirstHeader(CharSequence line) {
        assert currentMessage == null;
        var nextMessage = maybeHandleHeader(line);
        if (nextMessage.isPresent()) {
            currentMessage = nextMessage.get();
            return this::stateProcessFirstMessageLine;
        }
        return LineParser.currentState();
    }

    public State stateProcessFirstMessageLine(CharSequence line) {
        var msg = Objects.requireNonNull(currentMessage);
        msg.addMessageLine(line);
        return this::stateProcessNextMessageLine;
    }

    private State stateProcessNextMessageLine(CharSequence line) {
        var msg = Objects.requireNonNull(currentMessage);
        if (isBlank(line)) {
            // Our previous message line ended with '\n'. This "line" is a next '\n'. There are two possibilities:
            // 1. Message ended, wasn't EOLN-terminated and both EOLNs are the entry terminator. In this case next
            //    line is a header of a new log entry.
            // 2. Message ended, was EOLN-terminated. This "line" is a first symbol of the terminator. Next line is
            //    EOLN.
            // 3. Message doesn't end here, this "line" is part of it.
            return nextLine -> stateMaybeMessageEnded(2, nextLine);
        }
        msg.addMessageLine(line);
        // Note: this method is also called from other states, so it isn't safe to return currentState() here.
        return this::stateProcessNextMessageLine;
    }

    public State stateMaybeMessageEnded(int consecutiveEolns, CharSequence line) {
        // Before this call we saw at least two consecutive EOLNs. At this point the current line can be:
        // 1. Another EOLN. A third in a row could be part of the terminator, if the last non-blank line had a trailing
        // '\n', like Log.d("foo\n"). We don't want to emit an empty log record in this case.
        // Fourth is worth printing a single empty line, though.
        // TODO(mlopatkin): Verify how logcat handles d("FOO\n") vs d("FOO")
        var msg = Objects.requireNonNull(currentMessage);
        if (isBlank(line)) {
            if (consecutiveEolns >= 3) {
                msg.addMessageLine("");
                return LineParser.currentState();
            } else {
                return nextLine -> stateMaybeMessageEnded(consecutiveEolns + 1, nextLine);
            }
        }
        // 2. A control line, like "--------- beginning of system". It is followed by a header if it is a valid
        // control line. However, it may be a part of the message, so we need to keep our potential empty lines with us.
        if (isControlLine(line)) {
            return nextLine -> afterControlLikeLine(consecutiveEolns, line, nextLine);
        }

        // 4. A header. This means, a new message begins, and the old one is done.
        var maybeNextMessage = maybeHandleHeader(line);
        if (maybeNextMessage.isPresent()) {
            msg.commit();
            currentMessage = maybeNextMessage.get();
            return this::stateProcessFirstMessageLine;
        }

        // 5. A text line. All EOLNS before are part of the message. Except one, which was the end of a previous
        // non-empty line.
        for (int i = 0; i < consecutiveEolns - 1; ++i) {
            msg.addMessageLine("");
        }
        msg.addMessageLine(line);
        return this::stateProcessNextMessageLine;
    }

    private Optional<CurrentMessage> maybeHandleHeader(CharSequence line) {
        var matcher = HEADER_PATTERN.matcher(line);
        if (matcher.matches()) {
            return Optional.of(new CurrentMessage(matcher));
        }
        return Optional.empty();
    }

    private static boolean isControlLine(CharSequence sequence) {
        return CONTROL_PATTERN.matcher(sequence).matches();
    }

    private State afterControlLikeLine(int consecutiveEolns, CharSequence maybeControlLine, CharSequence line) {
        var msg = Objects.requireNonNull(currentMessage);
        var maybeNextMessage = maybeHandleHeader(line);
        if (maybeNextMessage.isPresent()) {
            msg.commit();
            currentMessage = maybeNextMessage.get();
            return this::stateProcessFirstMessageLine;
        }
        // control line was actually a message line, preceded by eolns:
        for (int i = 0; i < consecutiveEolns - 1; ++i) {
            msg.addMessageLine("");
        }
        msg.addMessageLine(maybeControlLine);
        // We process the current line as a next line too.
        return stateProcessNextMessageLine(line);
    }

    private static boolean isBlank(CharSequence sequence) {
        return sequence.isEmpty();
    }

    @Override
    public ParserControl parseLine(CharSequence line) {
        lineParser.nextLine(line);
        return ParserControl.proceed();
    }

    @Override
    public void close() {
        CurrentMessage msg = currentMessage;
        if (msg != null) {
            msg.commit();
        }
    }

    private class CurrentMessage {
        private final Timestamp timestamp;
        private final int pid;
        private final int tid;
        private final Priority priority;
        private final String tag;
        private final List<CharSequence> messageLines = new ArrayList<>();

        public CurrentMessage(Matcher matcher) {
            timestamp = parseTimestamp(matcher.group(1));
            pid = Integer.parseInt(matcher.group(2));
            tid = Integer.decode(matcher.group(3));
            priority = Priority.fromChar(matcher.group(4));
            // TODO(mlopatkin): we probably need to trim tags of other formats.
            tag = CharMatcher.whitespace().trimTrailingFrom(matcher.group(5));
        }

        public void addMessageLine(CharSequence line) {
            messageLines.add(line);
        }

        public void commit() {
            for (var messageLine : messageLines) {
                eventsHandler.logRecord(timestamp, pid, tid, priority, tag, messageLine.toString());
            }
        }
    }

    private static Timestamp parseTimestamp(String timestamp) {
        try {
            return TimeFormatUtils.getTimeFromString(timestamp);
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
    }
}
