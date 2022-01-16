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

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;

import java.awt.Dimension;

@Immutable
public class FrameDimensions {
    public final int width;
    public final int height;

    public FrameDimensions(int width, int height) {
        Preconditions.checkArgument(width >= 0, "Negative width (%s) not supported", width);
        Preconditions.checkArgument(height >= 0, "Negative height (%s) not supported", height);

        this.width = width;
        this.height = height;
    }

    public Dimension toAwtDimension() {
        return new Dimension(width, height);
    }
}
