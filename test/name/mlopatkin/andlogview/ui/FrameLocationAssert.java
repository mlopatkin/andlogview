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
import org.jspecify.annotations.Nullable;

public class FrameLocationAssert extends AbstractAssert<FrameLocationAssert, @Nullable FrameLocation> {
    protected FrameLocationAssert(@Nullable FrameLocation frameLocation) {
        super(frameLocation, FrameLocationAssert.class);
    }

    public static FrameLocationAssert assertThat(@Nullable FrameLocation frameLocation) {
        return new FrameLocationAssert(frameLocation);
    }

    public FrameLocationAssert hasX(int x) {
        if (x != actual.x()) {
            throw failureWithActualExpected(actual.x(), x, "Expected location's X <%d>, got <%d>", x, actual.x());
        }
        return this;
    }

    public FrameLocationAssert hasY(int y) {
        if (y != actual.y()) {
            throw failureWithActualExpected(actual.y(), y, "Expected location's Y <%d>, got <%d>", y, actual.y());
        }
        return this;
    }

    public FrameLocationAssert isAt(int x, int y) {
        if (x != actual.x() || y != actual.y()) {
            throw failureWithActualExpected(actual, new FrameLocation(x, y),
                    "Expected location <(%d, %d)>, got <(%d, %d)>", x, y, actual.x(), actual.y());
        }
        return this;
    }
}

