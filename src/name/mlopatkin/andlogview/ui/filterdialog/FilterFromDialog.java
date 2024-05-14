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

package name.mlopatkin.andlogview.ui.filterdialog;

import name.mlopatkin.andlogview.filters.ColoringFilter;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.logmodel.LogRecord;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;

public class FilterFromDialog implements ColoringFilter {
    private final boolean enabled;
    private final FilterFromDialogData data;

    FilterFromDialog(boolean enabled, FilterFromDialogData data) {
        this.enabled = enabled;
        this.data = data;
    }

    @Override
    public boolean test(LogRecord input) {
        return data.test(input);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public FilteringMode getMode() {
        return data.getMode();
    }

    @Override
    public @Nullable Color getHighlightColor() {
        return data.getHighlightColor();
    }

    @Override
    public FilterFromDialog enabled() {
        return enabled ? this : new FilterFromDialog(true, data);
    }

    @Override
    public FilterFromDialog disabled() {
        return enabled ? new FilterFromDialog(false, data) : this;
    }

    public FilterFromDialogData getData() {
        // TODO(mlopatkin) defensive copy?
        return data;
    }
}
