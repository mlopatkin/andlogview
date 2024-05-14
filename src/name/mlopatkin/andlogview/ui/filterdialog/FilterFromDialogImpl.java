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
import name.mlopatkin.andlogview.search.RequestCompilationException;

import com.google.common.base.Preconditions;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;
import java.util.Objects;
import java.util.function.Predicate;

public class FilterFromDialogImpl implements FilterFromDialog, ColoringFilter {
    private final boolean enabled;
    private final FilterFromDialogData data;
    private final Predicate<LogRecord> recordPredicate;

    FilterFromDialogImpl(boolean enabled, FilterFromDialogData data) throws RequestCompilationException {
        Preconditions.checkArgument(data.getMode() != null, "The mode cannot be null");
        this.enabled = enabled;
        this.data = data;
        this.recordPredicate = data.compilePredicate();
    }

    /**
     * Toggle constructor. It makes a shallow copy.
     *
     * @param enabled the new enabled status
     * @param orig the original filter
     */
    private FilterFromDialogImpl(boolean enabled, FilterFromDialogImpl orig) {
        this.enabled = enabled;
        this.data = orig.data;
        this.recordPredicate = orig.recordPredicate;
    }

    @Override
    public boolean test(LogRecord input) {
        return recordPredicate.test(input);
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
    public FilterFromDialogImpl enabled() {
        return enabled ? this : new FilterFromDialogImpl(true, this);
    }

    @Override
    public FilterFromDialogImpl disabled() {
        return enabled ? new FilterFromDialogImpl(false, this) : this;
    }

    @Override
    public FilterFromDialogData getData() {
        // TODO(mlopatkin) defensive copy?
        return data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, data);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof FilterFromDialogImpl that) {
            return enabled == that.enabled && Objects.equals(data, that.data);
        }
        return false;
    }
}
