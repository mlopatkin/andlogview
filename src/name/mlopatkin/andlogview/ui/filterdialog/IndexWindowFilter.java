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
import name.mlopatkin.andlogview.filters.ChildModelFilter;
import name.mlopatkin.andlogview.filters.Filter;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.filters.MutableFilterModel;
import name.mlopatkin.andlogview.filters.PredicateFilter;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.search.RequestCompilationException;

import com.google.common.collect.ImmutableSet;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

public class IndexWindowFilter extends AbstractFilter<IndexWindowFilter> implements FilterFromDialog, ChildModelFilter {
    private final FilterFromDialogData filterData;
    private final MutableFilterModel model;

    public IndexWindowFilter(boolean enabled, FilterFromDialogData filterData) throws RequestCompilationException {
        this(enabled, filterData, ImmutableSet.of());
    }

    public IndexWindowFilter(boolean enabled, FilterFromDialogData filterData,
            Collection<? extends Filter> childFilters) throws RequestCompilationException {
        super(FilteringMode.WINDOW, enabled);
        var hideFilter = predicateToHideFilter(filterData.compilePredicate());
        this.model =
                MutableFilterModel.create(ImmutableSet.<Filter>builder().add(hideFilter).addAll(childFilters).build());
        this.filterData = filterData;
    }

    private IndexWindowFilter(boolean enabled, IndexWindowFilter orig) {
        super(FilteringMode.WINDOW, enabled);
        this.model = orig.model;
        this.filterData = orig.filterData;
    }

    private static PredicateFilter predicateToHideFilter(Predicate<? super LogRecord> filterPredicate) {
        class HideFilter extends AbstractFilter<HideFilter> implements PredicateFilter {
            protected HideFilter() {
                super(FilteringMode.HIDE, true);
            }

            @Override
            protected HideFilter copy(boolean enabled) {
                throw new UnsupportedOperationException("Cannot disable!");
            }

            @Override
            public boolean test(LogRecord logRecord) {
                return !filterPredicate.test(logRecord);
            }
        }

        return new HideFilter();
    }

    @Override
    protected IndexWindowFilter copy(boolean enabled) {
        return new IndexWindowFilter(enabled, this);
    }

    @Override
    public FilterFromDialogData getData() {
        // TODO(mlopatkin) defensive copy?
        return filterData;
    }

    @Override
    public MutableFilterModel getChildren() {
        return model;
    }

    @Override
    public @Nullable String getName() {
        return filterData.getName();
    }
}
