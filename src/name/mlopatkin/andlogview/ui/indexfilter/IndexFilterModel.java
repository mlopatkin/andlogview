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

package name.mlopatkin.andlogview.ui.indexfilter;

import name.mlopatkin.andlogview.filters.CompoundFilterModel;
import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.filters.PredicateFilter;
import name.mlopatkin.andlogview.logmodel.LogRecord;

import java.util.Collections;
import java.util.function.Predicate;

/**
 * Utility class to build child FilterModel used by the index window.
 */
final class IndexFilterModel {
    private IndexFilterModel() {}

    public static FilterModel createIndexFilterModel(FilterModel parentModel, Predicate<? super LogRecord> filter) {
        return new CompoundFilterModel(
                parentModel,
                FilterModel.create(Collections.singleton(makeOnlyFilter(filter))));
    }

    /**
     * Creates a filter that HIDEs all records except ones matching the given predicate (aka "only" filter).
     * @param filter the predicate
     * @return the HIDE filter.
     */
    private static PredicateFilter makeOnlyFilter(Predicate<? super LogRecord> filter) {
        return new PredicateFilter() {
            @Override
            public FilteringMode getMode() {
                return FilteringMode.HIDE;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public PredicateFilter enabled() {
                return this;
            }

            @Override
            public PredicateFilter disabled() {
                throw new UnsupportedOperationException("Cannot disable filter through child model");
            }

            @Override
            public boolean test(LogRecord logRecord) {
                return !filter.test(logRecord);
            }
        };
    }
}
