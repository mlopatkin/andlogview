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

package name.mlopatkin.andlogview.ui;

import org.assertj.core.api.AbstractAssert;

public class FrameDimensionsAssert extends AbstractAssert<FrameDimensionsAssert, FrameDimensions> {
    protected FrameDimensionsAssert(FrameDimensions frameDimensions) {
        super(frameDimensions, FrameDimensionsAssert.class);
    }

    public static FrameDimensionsAssert assertThat(FrameDimensions dimensions) {
        return new FrameDimensionsAssert(dimensions);
    }

    public FrameDimensionsAssert hasWidth(int width) {
        if (width != actual.width()) {
            throw failureWithActualExpected(actual.width(), width,
                    "Expected frame width <%d>, got <%d>", width, actual.width());
        }
        return this;
    }

    public FrameDimensionsAssert hasHeight(int height) {
        if (height != actual.height()) {
            throw failureWithActualExpected(actual.height(), height,
                    "Expected frame height <%d>, got <%d>", height, actual.height());
        }
        return this;
    }

    public FrameDimensionsAssert hasDimensions(int width, int height) {
        if (width != actual.width() || height != actual.height()) {
            throw failureWithActualExpected(actual, new FrameDimensions(width, height),
                    "Expected dimensions <%dx%d>, got <%dx%d>", width, height, actual.width(), actual.height());
        }
        return this;
    }
}
