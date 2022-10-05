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

import org.assertj.core.api.AbstractAssert;

@SuppressWarnings("UnusedReturnValue")
public class ParserControlAssert extends AbstractAssert<ParserControlAssert, ParserControl> {
    protected ParserControlAssert(ParserControl actual) {
        super(actual, ParserControlAssert.class);
    }

    public ParserControlAssert shouldProceed() {
        if (!actual.shouldProceed()) {
            throw failure("Expecting to proceed");
        }
        return this;
    }

    public ParserControlAssert shouldStop() {
        if (actual.shouldProceed()) {
            throw failure("Expecting to stop");
        }
        return this;
    }

    public static ParserControlAssert assertThat(ParserControl actual) {
        return new ParserControlAssert(actual);
    }
}
