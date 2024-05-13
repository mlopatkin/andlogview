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

import name.mlopatkin.andlogview.filters.Filter;
import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.logmodel.LogRecord;

import java.util.function.Predicate;

/**
 * Utility class to build child FilterModel used by the index window.
 */
final class IndexFilterModel {
    private IndexFilterModel() {}

    public static FilterModel createIndexFilterModel(FilterModel parentModel, Predicate<? super LogRecord> filter) {
        var combinedModel = FilterModel.create();
        parentModel.getFilters().forEach(combinedModel::addFilter);
        parentModel.asObservable().addObserver(new FilterModel.Observer() {
            @Override
            public void onFilterAdded(FilterModel model, Filter newFilter) {
                combinedModel.addFilter(newFilter);
            }

            @Override
            public void onFilterRemoved(FilterModel model, Filter removedFilter) {
                // TODO(mlopatkin): this self-caused unsubscribe is not ideal, but it works for now
                if (removedFilter == filter) {
                    model.asObservable().removeObserver(this);
                }
                combinedModel.removeFilter(removedFilter);
            }

            @Override
            public void onFilterReplaced(FilterModel model, Filter oldFilter, Filter newFilter) {
                if (oldFilter == filter) {
                    model.asObservable().removeObserver(this);
                }
                combinedModel.replaceFilter(oldFilter, newFilter);
            }
        });

        combinedModel.addFilter(new Filter() {
            @Override
            public FilteringMode getMode() {
                return FilteringMode.HIDE;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public Filter enabled() {
                return this;
            }

            @Override
            public Filter disabled() {
                throw new UnsupportedOperationException("Cannot disable filter through child model");
            }

            @Override
            public boolean test(LogRecord logRecord) {
                return !filter.test(logRecord);
            }
        });
        return combinedModel;
    }
}
