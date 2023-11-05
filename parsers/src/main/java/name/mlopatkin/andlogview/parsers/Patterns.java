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

package name.mlopatkin.andlogview.parsers;

import java.util.regex.Pattern;

public final class Patterns {
    private Patterns() {}

    /**
     * Helper that concatenates all patternParts, wrap them in {@code "^..$"} and compiles the result
     *
     * @param patternParts the parts of the regular expression
     * @return the compiled Pattern
     */
    public static Pattern compileFromParts(String... patternParts) {
        return joinAndCompileFromParts("", patternParts);
    }

    /**
     * Helper that concatenates all patternParts, wrap them in {@code "^..$"} and compiles the result
     *
     * @param separator the separator to use when joining patternParts, can be a regular expression too.
     * @param patternParts the parts of the regular expression
     * @return the compiled Pattern
     */
    public static Pattern joinAndCompileFromParts(String separator, String... patternParts) {
        return Pattern.compile("^" + String.join(separator, patternParts) + "$");
    }
}
