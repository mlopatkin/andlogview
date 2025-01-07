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

import name.mlopatkin.andlogview.filters.AbstractFilter;
import name.mlopatkin.andlogview.filters.ColoringFilter;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.search.RequestCompilationException;

import com.google.common.base.MoreObjects;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;
import java.util.Objects;
import java.util.function.Predicate;

public class FilterFromDialogImpl extends AbstractFilter<FilterFromDialogImpl>
        implements FilterFromDialog, ColoringFilter {
    private final FilterFromDialogData data;
    private final Predicate<LogRecord> recordPredicate;

    FilterFromDialogImpl(boolean enabled, FilterFromDialogData data) throws RequestCompilationException {
        super(Objects.requireNonNull(data.getMode()), enabled);
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
        super(orig.getMode(), enabled);
        this.data = orig.data;
        this.recordPredicate = orig.recordPredicate;
    }

    @Override
    public boolean test(LogRecord input) {
        return recordPredicate.test(input);
    }

    @Override
    public @Nullable Color getHighlightColor() {
        return data.getHighlightColor();
    }

    @Override
    protected FilterFromDialogImpl copy(boolean enabled) {
        return new FilterFromDialogImpl(enabled, this);
    }

    @Override
    public FilterFromDialogData getData() {
        // TODO(mlopatkin) defensive copy?
        return data;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("enabled", isEnabled()).add("data", getData()).toString();
    }
}
