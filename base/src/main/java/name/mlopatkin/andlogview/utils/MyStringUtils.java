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

import java.util.Locale;

public class MyStringUtils {
    private MyStringUtils() {}

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
    public static String abbreviateMiddle(CharSequence str, char replacement, int maxLength, int prefixLength) {
        Preconditions.checkNotNull(str, "str is null");
        Preconditions.checkArgument(maxLength > 0, "maxLength=%s <= 0", maxLength);
        Preconditions.checkArgument(prefixLength > 0, "prefixLength=%s <= 0", prefixLength);
        Preconditions.checkArgument(maxLength >= prefixLength + 2,
                "maxLength=%s is too small for prefixLength=%s + replacement + 1-char suffix", maxLength, prefixLength);

        if (str.length() <= maxLength) {
            return str.toString();
        }

        int suffixLength = maxLength - prefixLength - 1; // one symbol for replacement
        assert suffixLength > 0;
        int suffixStart = str.length() - suffixLength;
        // StringBuilder has fewer allocations because no substring is needed.
        StringBuilder result = new StringBuilder(maxLength);
        result.append(str, 0, prefixLength);
        result.append(replacement);
        result.append(str, suffixStart, str.length());
        return result.toString();
    }

    /**
     * Returns the first character of a non-empty {@link CharSequence}.
     */
    public static char first(CharSequence cs) {
        Preconditions.checkArgument(!cs.isEmpty(), "CharSequence is empty");
        return cs.charAt(0);
    }

    /**
     * Returns the last character of a non-empty {@link CharSequence}.
     */
    public static char last(CharSequence cs) {
        Preconditions.checkArgument(!cs.isEmpty(), "CharSequence is empty");
        return cs.charAt(cs.length() - 1);
    }
}
