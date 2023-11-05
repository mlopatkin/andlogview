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
package name.mlopatkin.andlogview.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import java.util.Locale;
import java.util.stream.Stream;

public class MyStringUtils {
    private MyStringUtils() {}

    private static final Splitter EOL_SPLITTER = Splitter.onPattern("((?<=\n))");

    public static final int NOT_FOUND = -1;

    public static int indexOfIgnoreCase(String src, String pattern) {
        return indexOfIgnoreCase(src, pattern, 0);
    }

    public static int indexOfIgnoreCase(String src, String pattern, int offset) {
        src = src.toLowerCase(Locale.getDefault());
        pattern = pattern.toLowerCase(Locale.getDefault());
        return src.indexOf(pattern, offset);
    }

    /**
     * Shortens the string to {@code maxLength} symbols by replacing middle (starting from {@code prefixLength}) symbols
     * with {@code replacement} symbol. If the string is not longer than {@code maxLength} then the unchanged string is
     * returned. If the replacement takes place then non-empty suffix of the string follows the replacement symbol in
     * the result.
     * <p>
     * Examples:
     * <pre>{@code
     * abbreviateMiddle("hello", '*', 3, 1) -> "h*o";
     * abbreviateMiddle("hello", '*', 4, 1) -> "h*lo", suffix is big enough to match maxLength;
     * abbreviateMiddle("hello", '*', 4, 2) -> "he*o";
     * abbreviateMiddle("hello", '*', 3, 2) -> IllegalArgumentException, he* is already maxLength;
     * abbreviateMiddle("hello", '*', 5, 1) -> "hello", unchanged because fits into maxLength=5.
     * }</pre>
     *
     * @param str the string to perform replacement in
     * @param replacement the replacement character
     * @param maxLength the maximum length of the result
     * @param prefixLength the length of the prefix of 'str' to keep before the replacement character in the
     *         result
     * @return the string of up to {@code maxLength} symbols with extra symbols in the middle replaced with {@code
     *         replacement}
     * @throws NullPointerException if {@code str} is {@code null}
     * @throws IllegalArgumentException if {@code maxLength <= 0} or {@code prefixLength <= 0} or
     *         {@code maxLength < prefixLength + 2} (length of prefix, length of
     *         replacement, and length of shortest possible suffix).
     */
    public static String abbreviateMiddle(String str, char replacement, int maxLength, int prefixLength) {
        Preconditions.checkNotNull(str, "str is null");
        Preconditions.checkArgument(maxLength > 0, "maxLength=%s <= 0", maxLength);
        Preconditions.checkArgument(prefixLength > 0, "prefixLength=%s <= 0", prefixLength);
        Preconditions.checkArgument(maxLength >= prefixLength + 2,
                "maxLength=%s is too small for prefixLength=%s + replacement + 1-char suffix", maxLength, prefixLength);

        if (str.length() <= maxLength) {
            return str;
        }

        int suffixLength = maxLength - prefixLength - 1; // one symbol for replacement
        assert suffixLength > 0;
        int suffixStart = str.length() - suffixLength;
        // StringBuilder has fewer allocations because no substring and IndyConcat is Java 9+ while target is Java 8.
        @SuppressWarnings("StringBufferReplaceableByString")
        StringBuilder result = new StringBuilder(maxLength);
        result.append(str, 0, prefixLength);
        result.append(replacement);
        result.append(str, suffixStart, str.length());
        return result.toString();
    }

    /**
     * Returns {@code true} if the given CharSequence is empty. Backport of Java 15's instance method.
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs.length() == 0;
    }

    /**
     * Returns the first character of a non-empty {@link CharSequence}.
     */
    public static char first(CharSequence cs) {
        Preconditions.checkArgument(!isEmpty(cs), "CharSequence is empty");
        return cs.charAt(0);
    }

    /**
     * Returns the last character of a non-empty {@link CharSequence}.
     */
    public static char last(CharSequence cs) {
        Preconditions.checkArgument(!isEmpty(cs), "CharSequence is empty");
        return cs.charAt(cs.length() - 1);
    }

    /**
     * Splits the char sequence at line terminators into a stream of strings. Similar to Java 11's
     * {@code String.lines()}, but only recognizes {@code \n} as a line terminator.
     *
     * @param sequence the sequence to split
     * @return the stream of strings
     */
    public static Stream<String> lines(CharSequence sequence) {
        if (isEmpty(sequence)) {
            return Stream.empty();
        }
        return EOL_SPLITTER.splitToStream(sequence).map(s -> s.endsWith("\n") ? s.substring(0, s.length() - 1) : s);
    }
}
