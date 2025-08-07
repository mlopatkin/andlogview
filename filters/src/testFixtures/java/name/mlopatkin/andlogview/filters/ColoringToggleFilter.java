/*
 * Copyright 2024 the Andlogview authors
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

package name.mlopatkin.andlogview.filters;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.function.Predicate;

public class ColoringToggleFilter extends AbstractToggleFilter<ColoringToggleFilter> implements ColoringFilter {
    private final Color color;

    public ColoringToggleFilter(Color color, boolean enabled, Predicate<? super LogRecord> predicate) {
        super(FilteringMode.HIGHLIGHT, enabled, predicate);
        this.color = color;
    }

    @Override
    public @Nullable Color getHighlightColor() {
        return color;
    }

    @Override
    protected ColoringToggleFilter copy(boolean enabled) {
        return new ColoringToggleFilter(color, enabled, predicate);
    }
}
