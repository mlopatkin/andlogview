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

/**
 * Base interface for all push parsers.
 */
public interface BasePushParser extends AutoCloseable {
    /**
     * Passes the line to the parser to process. The parser can signal that it no longer intends to process the input by
     * returning {@code false} from this method.
     *
     * @param line the next line of input
     * @return {@code true} if the parser can process more lines or {@code false} if the parser cannot process the input
     *         anymore
     */
    boolean nextLine(CharSequence line);

    /**
     * Signals the end of the input to the parser.
     */
    @Override
    void close();
}
