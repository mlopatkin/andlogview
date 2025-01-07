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

import java.util.function.Predicate;

public class ToggleFilter extends AbstractToggleFilter<ToggleFilter> {
    public ToggleFilter(FilteringMode mode, boolean enabled, Predicate<? super LogRecord> predicate) {
        super(mode, enabled, predicate);
    }

    @Override
    protected ToggleFilter copy(boolean enabled) {
        return new ToggleFilter(mode, enabled, predicate);
    }

    public static ToggleFilter show(Predicate<? super LogRecord> predicate) {
        return new ToggleFilter(FilteringMode.SHOW, true, predicate);
    }

    public static ToggleFilter hide(Predicate<? super LogRecord> predicate) {
        return new ToggleFilter(FilteringMode.HIDE, true, predicate);
    }

    public static ToggleFilter index(Predicate<? super LogRecord> predicate) {
        return new ToggleFilter(FilteringMode.WINDOW, true, predicate);
    }
}
