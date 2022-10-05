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

class SectionParserControl {
    private SectionParserControl() {}

    public boolean shouldSkip() {
        return false;
    }

    public boolean shouldStop() {
        return false;
    }

    public final boolean shouldProceed() {
        return !shouldSkip() && !shouldStop();
    }

    public static SectionParserControl proceed() {
        return new SectionParserControl();
    }

    public static SectionParserControl skipSection() {
        return new SectionParserControl() {
            @Override
            public boolean shouldSkip() {
                return true;
            }
        };
    }

    public static SectionParserControl stop() {
        return new SectionParserControl() {
            @Override
            public boolean shouldStop() {
                return true;
            }
        };
    }
}
