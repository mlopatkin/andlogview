/*
 * Copyright 2023 the Andlogview authors
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

/**
 * End-of-line character sequence
 */
public enum Eoln {
    NONE(""),
    CR("\r"),
    LF("\n"),
    CRLF("\r\n");

    private final String chars;

    Eoln(String chars) {
        this.chars = chars;
    }

    /**
     * Returns the string representation of this EOLN sequence.
     *
     * @return the EOLN as a String
     */
    public String getChars() {
        return chars;
    }
}
