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

package name.mlopatkin.andlogview.parsers.dumpstate;

/**
 * Parsers for dumpstate files.
 */
public final class DumpstateParsers {
    private DumpstateParsers() {}

    /**
     * Creates a special parser that checks if the file is actually a dumpstate file. This parser can then create a new
     * parser to actually handle the dumpstate file.
     *
     * @return the push parser that detects if the input is in the dumpstate format and can create parser to handle this
     *         format
     */
    public static DumpstateFormatSniffer detectFormat() {
        return new DumpstateFormatSniffer();
    }
}
