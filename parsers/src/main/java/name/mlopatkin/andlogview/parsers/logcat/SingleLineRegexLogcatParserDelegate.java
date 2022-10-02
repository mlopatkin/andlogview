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

import name.mlopatkin.andlogview.parsers.ParserControl;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses formats that are one-line-per-entry, so everything except {@link Format#LONG}.
 */
abstract class SingleLineRegexLogcatParserDelegate extends RegexLogcatParserDelegate {
    private final Pattern pattern;

    /**
     * Initializes the parser.
     *
     * @param eventsHandler the handler of parse events
     * @param pattern the compiled pattern used to parse log entries
     */
    protected SingleLineRegexLogcatParserDelegate(LogcatParseEventsHandler eventsHandler, Pattern pattern) {
        super(eventsHandler);
        this.pattern = pattern;
    }

    @Override
    public final ParserControl parseLine(CharSequence line) {
        Matcher matcher = pattern.matcher(line);
        if (!matcher.matches()) {
            return eventsHandler.unparseableLine(line);
        }
        try {
            return fromGroups(matcher);
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Invoked when the pattern matches the given line. The implementation then should invoke an appropriate method of
     * the {@link #eventsHandler}, forwarding all extracted information to it. The matcher is guaranteed to be matched.
     *
     * @param m the matcher
     * @return the result of dispatching the extracted information
     * @throws ParseException if the pattern is broken somehow and matched parts cannot be parsed
     */
    protected abstract ParserControl fromGroups(Matcher m) throws ParseException;

    /**
     * Helper that concatenates all patternParts, wrap them in "^..$" and compiles the result
     *
     * @param patternParts the parts of the regular expression
     * @return the compiled Pattern
     */
    protected static Pattern compileFromParts(String... patternParts) {
        return Pattern.compile("^" + String.join("", patternParts) + "$");
    }
}
