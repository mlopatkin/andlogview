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

import org.assertj.core.api.AbstractAssert;

@SuppressWarnings("UnusedReturnValue")
class DumpstateParserControlAssert extends AbstractAssert<DumpstateParserControlAssert, DumpstateParserControl> {
    protected DumpstateParserControlAssert(DumpstateParserControl actual) {
        super(actual, DumpstateParserControlAssert.class);
    }

    public DumpstateParserControlAssert shouldProceed() {
        if (actual.shouldSkip() || actual.shouldStop()) {
            throw failure("Expecting to proceed");
        }
        return this;
    }

    public DumpstateParserControlAssert shouldStop() {
        if (!actual.shouldStop()) {
            throw failure("Expecting to stop");
        }
        return this;
    }

    public DumpstateParserControlAssert shouldSkip() {
        if (!actual.shouldSkip()) {
            throw failure("Expecting to skip");
        }
        return this;
    }

    public DumpstateParserControlAssert hasSectionHandler() {
        if (actual.shouldSkip() || actual.shouldStop()) {
            throw failure("Expecting to have section handler");
        }
        return this;
    }

    public static DumpstateParserControlAssert assertThat(DumpstateParserControl actual) {
        return new DumpstateParserControlAssert(actual);
    }
}
