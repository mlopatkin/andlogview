/*
 * Copyright 2020 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.filterdialog;

import com.google.common.base.CharMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is the collection of the utilities to parse and display patterns and pattern lists in filter dialogs. Each
 * dialog has input fields for tags, pids and applications, and message. The user enters patterns to match items of
 * interest. Fields for tags and pids/apps understand multiple patterns if such patterns are separated with comma.
 * Patterns may be plain text or regular expressions (such patterns are enclosed in {@code /.../}). This class provides
 * routines to split such comma-separated pattern lists and join them back to display to the user.
 *
 * <h3>Detailed format description</h3>
 *
 * The pattern list is a comma-separated list of pattern values (plain text or regular expressions). Pattern value
 * cannot include commas verbatim, it has to be escaped. Two consecutive comma symbols must be written to include
 * comma symbol in the pattern (this is similar to the escaping of quotes in Windows BAT files and CSV format). This
 * applies to both plain text and regex patterns.
 * <p>
 * Leading and trailing whitespaces in patterns are ignored unless whitespace appears within regex, e. g. in {@code "/
 * foo  /"}  whitespaces around {@code foo} are part of the pattern. Another exception is the quoted text (see below).
 * In other words: consecutive whitespaces around separating (not escaped) comma symbol and leading and trailing
 * whitespaces are ignored when splitting.
 * <p>
 * The plain text value can be enclosed in quoting characters. The only supported quoting character is the backtick
 * symbol {@code `}. All whitespaces including trailing are preserved within backticks. The comma symbol is a normal
 * symbol within backticks, it doesn't serve as a pattern separator (again, similar to CSV format). If the quoting
 * character appears in the value itself it is doubled to escape it, for example {@code hello`there} is written as
 * {@code `hello``there`}. Values with leading or trailing whitespaces or the ones that start with the backtick symbol
 * must be written in the quoting characters, there is no other way. The value that contains commas may be written with
 * escaped commas but quoting is still preferred. It is an error to have unclosed quote though.
 * <p>
 * Currently it is an error to quote regular expressions. It is reserved syntax to provide plaintext patterns that are
 * enclosed in {@code /.../}. This isn't supported yet.
 * <p>
 * Whitespace is defined as what {@link CharMatcher#whitespace()} accepts.
 */
public final class PatternsList {
    private PatternsList() {}

    /**
     * Thrown if splitting the pattern failed. Brief description of the error is in {@link #getMessage()}. Broken
     * pattern that failed to parse can be accesed with {@link #getPattern()}. Location of the offending fragment is
     * {@code [getStartPos(), getEndPos())}.
     */
    public static class FormatException extends Exception {
        private final String pattern;
        private final int startPos;
        private final int endPos;

        public FormatException(String message, String pattern, int startPos, int endPos) {
            super(message);

            this.pattern = pattern;
            this.startPos = startPos;
            this.endPos = endPos;
        }

        /** @return the pattern that failed to be parsed */
        public String getPattern() {
            return pattern;
        }

        /** @return the start position of error pattern or -1 if it cannot be determined */
        public int getStartPos() {
            return startPos;
        }

        /** @return the end position of error pattern or -1 if it cannot be determined */
        public int getEndPos() {
            return endPos;
        }
    }

    /**
     * Splits the pattern list into patterns. Empty patterns are omitted from result unless enclosed in quotes,
     * similar to {@code Splitter.on(",").trimWhitespace().omitEmptyStrings()}.
     *
     * @param text the pattern list to split
     * @return the list of patterns
     * @throws FormatException if the text is not a valid pattern list
     */
    public static List<String> split(String text) throws FormatException {
        return new PatternSplitter(text).split();
    }

    /**
     * Joins patterns so the resulting string can be presented in filter dialog and later can be
     * split with {@link #split(String)}.
     * <p>
     * It is guaranteed that join applies proper quoting/escaping so the resulting string ca be split without loss of
     * data.
     */
    public static String join(List<String> toJoin) {
        return PatternJoiner.join(toJoin);
    }

    public static String join(Stream<String> toJoin) {
        return PatternJoiner.join(toJoin);
    }

    // TODO(mlopatkin) Unify this code and SearchRequestParser
    private static final char QUOTE = '`';
    private static final char REGEX_BOUND = '/';
    private static final char SEPARATOR_CHAR = ',';
    public static final CharMatcher WHITESPACE = CharMatcher.whitespace();
    private static final CharMatcher NON_WHITESPACE = WHITESPACE.negate();

    private static class PatternSplitter {
        private final String toSplit;

        private int currentPos;

        private PatternSplitter(String toSplit) {
            this.toSplit = toSplit;
        }

        private List<String> split() throws FormatException {
            List<String> result = new ArrayList<>();
            while (!isAtEnd()) {
                skipWs();
                if (isAtQuote()) {
                    result.add(readQuoted());
                } else {
                    String value = readValue();
                    if (!value.isEmpty()) {
                        result.add(value);
                    }
                }
                skipWs();
                skipSeparator();
            }
            return result;
        }

        private boolean isAtEnd() {
            return toSplit.length() == currentPos;
        }

        private void skipWs() {
            int firstNonWs = NON_WHITESPACE.indexIn(toSplit, currentPos);
            if (firstNonWs != -1) {
                currentPos = firstNonWs;
            } else {
                currentPos = toSplit.length();
            }
        }

        private char curCh() {
            return toSplit.charAt(currentPos);
        }

        private String readValue() {
            if (isAtEnd()) {
                return "";
            }

            assert NON_WHITESPACE.matches(curCh());

            int startIndex = currentPos;

            int separatorIndex = toSplit.indexOf(SEPARATOR_CHAR, currentPos);
            while (separatorIndex != -1 && isEscaped(separatorIndex, SEPARATOR_CHAR)) {
                separatorIndex = toSplit.indexOf(SEPARATOR_CHAR, separatorIndex + 2);
            }
            if (separatorIndex != -1) {
                currentPos = separatorIndex;
            } else {
                currentPos = toSplit.length();
            }
            return CharMatcher.whitespace()
                    .trimTrailingFrom(toSplit.substring(startIndex, currentPos))
                    .replace(String.valueOf(SEPARATOR_CHAR) + SEPARATOR_CHAR, String.valueOf(SEPARATOR_CHAR));
        }

        private void skipSeparator() throws FormatException {
            if (isAtEnd()) {
                return;
            }
            if (curCh() != SEPARATOR_CHAR) {
                throw new FormatException("Expected separator here. Did you forget to escape quote character?", toSplit,
                        currentPos, currentPos + 1);
            }
            ++currentPos;
        }

        private boolean isAtQuote() {
            if (isAtEnd()) {
                return false;
            }
            char ch = curCh();
            return ch == QUOTE;
        }

        private String readQuoted() throws FormatException {
            assert !isAtEnd();
            char quote = curCh();
            int startQuotePos = currentPos;
            int endQuotePos = toSplit.indexOf(quote, startQuotePos + 1);
            while (endQuotePos != -1 && isEscaped(endQuotePos, quote)) {
                endQuotePos = toSplit.indexOf(quote, endQuotePos + 2);
            }
            if (endQuotePos == -1) {
                throw new FormatException("Quote must be closed somewhere", toSplit, startQuotePos, toSplit.length());
            }
            currentPos = endQuotePos + 1;
            String result = toSplit.substring(startQuotePos + 1, endQuotePos)
                    .replace(String.valueOf(quote) + quote, String.valueOf(quote));
            if (isRegex(result)) {
                throw new FormatException("Regex in quotes is not allowed yet", toSplit, startQuotePos,
                        endQuotePos + 1);
            }
            return result;
        }

        private boolean isEscaped(int pos, char quote) {
            return toSplit.charAt(pos) == quote && pos + 1 < toSplit.length() && toSplit.charAt(pos + 1) == quote;
        }
    }

    private static class PatternJoiner {
        private static final String QUOTE_STR = String.valueOf(QUOTE);
        private static final String SEP_QUOTE_STR = String.valueOf(SEPARATOR_CHAR);

        public static String join(List<String> toJoin) {
            return join(toJoin.stream());
        }

        public static String join(Stream<String> toJoin) {
            return toJoin.map(PatternJoiner::escapeIfNeeded).collect(Collectors.joining(", "));
        }

        private static String escapeIfNeeded(String s) {
            if (isRegex(s)) {
                return escapeRegex(s);
            }
            if (needsQuoting(s)) {
                return QUOTE_STR + quote(s, QUOTE_STR) + QUOTE_STR;
            }
            return s;
        }

        private static String escapeRegex(String s) {
            if (s.indexOf(SEPARATOR_CHAR, 1) == -1) {
                // Fast path: escaping not needed.
                return s;
            }
            return quote(s, SEP_QUOTE_STR);
        }

        private static boolean needsQuoting(String s) {
            assert !isRegex(s);

            if (s.isEmpty()) {
                return true;
            }
            char first = s.charAt(0);
            char last = s.charAt(s.length() - 1);
            if (isWhitespace(first) || isWhitespace(last)) {
                return true;
            }
            if (first == QUOTE) {
                return true;
            }
            return s.indexOf(SEPARATOR_CHAR) != -1;
        }

        private static String quote(String s, String quoteStr) {
            return s.replace(quoteStr, quoteStr + quoteStr);
        }
    }

    private static boolean isWhitespace(char c) {
        return WHITESPACE.matches(c);
    }

    /**
     * Checks if the string looks like a regular expression pattern. Only wrappers are checked, not the content.
     *
     * @param s the string to check
     * @return {@code true} if the string is interpreted as a regex pattern, {@code false} otherwise
     */
    public static boolean isRegex(String s) {
        return s.length() >= 2 && s.charAt(0) == REGEX_BOUND
                && s.charAt(s.length() - 1) == REGEX_BOUND;
    }

    /**
     * Wraps the string in regular expression slashes, i. e. converts {@code "foo"} into {@code "/foo/"}. No escaping of
     * the given string is performed.
     *
     * @param regexPattern the regular expression string to wrap
     * @return the regular expression pattern
     */
    public static String wrapRegex(String regexPattern) {
        return REGEX_BOUND + regexPattern + REGEX_BOUND;
    }
}
