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
 * An implementation of {@link SectionHandler} that delegates all parsing to the {@link BasePushParser}. The subparser
 * is closed when the session ends.
 */
class SubParserSectionHandler implements SectionHandler {
    private final BasePushParser subParser;

    SubParserSectionHandler(BasePushParser subParser) {
        this.subParser = subParser;
    }

    @Override
    public SectionParserControl nextLine(CharSequence line) {
        return subParser.nextLine(line) ? SectionParserControl.proceed() : SectionParserControl.skipSection();
    }

    @Override
    public ParserControl end() {
        subParser.close();
        return ParserControl.proceed();
    }
}
