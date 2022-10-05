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

import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.PushParser;

/**
 * A handler for low-level dumpstate parse events. This handler is only aware about the very basic structure of the
 * dumpstate file and can serve as a building block for more sophisticated parsers. All default implementation proceed
 * with parsing unless noted otherwise.
 */
interface BaseDumpstateParseEventsHandler extends PushParser.ParseEventsHandler {

    /**
     * Called when a dumpstate header is processed.
     *
     * @return {@linkplain ParserControl} instance to determine next parser action
     */
    default ParserControl header() {
        return ParserControl.proceed();
    }

    /**
     * Called when a new section starts. The returned {@link DumpstateParserControl} can provide a
     * {@link SectionHandler} to parse the section.
     *
     * @param sectionName the name of the section
     * @return {@linkplain DumpstateParserControl} instance to determine next parser action
     */
    default DumpstateParserControl sectionStarted(String sectionName) {
        return DumpstateParserControl.skipSection();
    }

    /**
     * Called when the section ends. This event happens even if the section was skipped.
     *
     * @param sectionName the name of the finished section
     * @return {@linkplain ParserControl} instance to determine next parser action
     */
    default ParserControl sectionEnded(String sectionName) {
        return ParserControl.proceed();
    }

    /**
     * Called when the line is encountered that cannot be attributed to header or section. This event is never emitted
     * for lines inside sections, even if the sections are being skipped.
     *
     * @param line the line
     * @return {@linkplain ParserControl} instance to determine next parser action
     */
    default ParserControl unparseableLine(CharSequence line) {
        return ParserControl.proceed();
    }
}
