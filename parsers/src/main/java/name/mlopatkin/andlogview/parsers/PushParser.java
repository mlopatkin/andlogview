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
 * The base interface of a push parser. The implementation is expected to hold a listener and invoke callbacks on this
 * listener for parsing events as a reaction to input.
 */
public interface PushParser<H extends PushParser.ParseEventsHandler> extends BasePushParser {
    interface ParseEventsHandler {
        /**
         * Called when the document parsing is complete. This is the last parsing event. As there is nothing to control,
         * there is no return value.
         */
        default void documentEnded() {}
    }

    H getHandler();
}
