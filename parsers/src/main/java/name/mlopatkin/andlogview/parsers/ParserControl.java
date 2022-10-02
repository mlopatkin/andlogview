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
 * Sealed class that the handler uses to signal the parser about next action after processing the event.
 */
public abstract class ParserControl {
    private ParserControl() {}

    private static final ParserControl PROCEED = new ParserControl() {
        @Override
        public boolean shouldProceed() {
            return true;
        }
    };

    private static final ParserControl STOP = new ParserControl() {
        @Override
        public boolean shouldProceed() {
            return false;
        }
    };

    /**
     * Whether the parsing should proceed further.
     * @return {@code true} if the parsing can proceed or {@code false} if it should stop
     */
    public abstract boolean shouldProceed();

    /**
     * Returns an instance of ParserControl that signals that the parsing can proceed.
     * @return an instance of ParserControl
     */
    public static ParserControl proceed() {
        return PROCEED;
    }
    /**
     * Returns an instance of ParserControl that signals that the parsing should stop.
     * @return an instance of ParserControl
     */
    public static ParserControl stop() {
        return STOP;
    }
}
