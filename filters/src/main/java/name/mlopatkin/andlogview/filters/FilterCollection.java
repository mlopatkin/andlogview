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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

public interface FilterCollection<T extends Filter> {
    void addFilter(T filter);

    void removeFilter(T filter);

    default void replaceFilter(T oldFilter, T newFilter) {
        removeFilter(oldFilter);
        addFilter(newFilter);
    }

    Function<? super Filter, ? extends @Nullable T> createObserverTransformer();

    default FilterModel.Observer setModel(FilterModel model) {
        var transformer = createObserverTransformer();
        for (var filter : model.getFilters()) {
            var transformed = transformer.apply(filter);
            if (transformed != null) {
                addFilter(transformed);
            }
        }

        var observer = new TransformingObserver<T>(transformer) {
            @Override
            protected void onMyFilterAdded(FilterModel model, T newFilter) {
                addFilter(newFilter);
            }

            @Override
            protected void onMyFilterRemoved(FilterModel model, T removedFilter) {
                removeFilter(removedFilter);
            }

            @Override
            protected void onMyFilterReplaced(FilterModel model, T oldFilter, T newFilter) {
                replaceFilter(oldFilter, newFilter);
            }
        };

        model.asObservable().addObserver(observer);
        return observer;
    }
}
