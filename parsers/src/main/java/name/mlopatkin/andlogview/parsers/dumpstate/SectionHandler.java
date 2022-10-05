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

import name.mlopatkin.andlogview.parsers.BasePushParser;
import name.mlopatkin.andlogview.parsers.ParserControl;

/**
 * A sub-parser for the dumpstate file section.
 */
interface SectionHandler {
    /**
     * Called for a new line of the section.
     *
     * @param line the line to process
     * @return {@linkplain SectionParserControl} instance to determine next parser action
     */
    default SectionParserControl nextLine(CharSequence line) {
        return SectionParserControl.proceed();
    }

    /**
     * Called when the section ends. Doesn't get called when the section is skipped.
     *
     * @return {@linkplain ParserControl} instance to determine next parser action
     */
    default ParserControl end() {
        return ParserControl.proceed();
    }

    static SectionHandler withSubParser(BasePushParser parser) {
        return new SubParserSectionHandler(parser);
    }
}
