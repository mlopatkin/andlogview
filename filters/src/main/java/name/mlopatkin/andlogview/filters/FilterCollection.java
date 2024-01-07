/*
 * Copyright 2015 Mikhail Lopatkin
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

import java.util.function.Function;

public interface FilterCollection<T extends Filter> {
    void addFilter(T filter);

    void removeFilter(T filter);

    boolean supportsMode(FilteringMode mode);

    default void replaceFilter(T oldFilter, T newFilter) {
        removeFilter(oldFilter);
        addFilter(newFilter);
    }

    default FilterModel.Observer createObserver(Function<? super Filter, ? extends T> filterMapper) {
        return new FilterModel.Observer() {
            @Override
            public void onFilterAdded(FilterModel model, Filter newFilter) {
                if (supportsMode(newFilter.getMode())) {
                    addFilter(filterMapper.apply(newFilter));
                }
            }

            @Override
            public void onFilterRemoved(FilterModel model, Filter removedFilter) {
                if (supportsMode(removedFilter.getMode())) {
                    removeFilter(filterMapper.apply(removedFilter));
                }
            }

            @Override
            public void onFilterReplaced(FilterModel model, Filter oldFilter, Filter newFilter) {
                var hadOldFilter = supportsMode(oldFilter.getMode());
                var willHaveNewFilter = supportsMode(newFilter.getMode());

                if (hadOldFilter && willHaveNewFilter) {
                    replaceFilter(filterMapper.apply(oldFilter), filterMapper.apply(newFilter));
                } else if (hadOldFilter) {
                    removeFilter(filterMapper.apply(oldFilter));
                } else if (willHaveNewFilter) {
                    addFilter(filterMapper.apply(newFilter));
                }
            }
        };
    }
}
