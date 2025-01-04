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

package name.mlopatkin.andlogview.ui.filters;

import static com.google.common.collect.ImmutableList.toImmutableList;

import name.mlopatkin.andlogview.filters.Filter;
import name.mlopatkin.andlogview.filters.FilterModel;

import com.google.common.collect.ImmutableList;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * A base class that implements the common logic of adapting the {@link FilterModel} to filter views.
 */
public abstract class BaseFilterModelAdapter<P extends BaseFilterPresenter> {
    private final Function<? super Filter, ? extends @Nullable P> presenterFactory;
    private final Map<Filter, P> filters = new HashMap<>();
    private final FilterModel model;

    protected BaseFilterModelAdapter(
            FilterModel model,
            Function<? super Filter, ? extends @Nullable P> presenterFactory
    ) {
        this.presenterFactory = presenterFactory;
        this.model = model;

        model.asObservable().addObserver(new FilterModel.Observer() {
            @Override
            public void onFilterAdded(FilterModel model, Filter newFilter) {
                var newTransformedFilter = transformFilter(newFilter);
                if (newTransformedFilter != null) {
                    addFilter(newTransformedFilter);
                }
            }

            @Override
            public void onFilterRemoved(FilterModel model, Filter removedFilter) {
                var removedTransformedFilter = filters.remove(removedFilter);
                if (removedTransformedFilter != null) {
                    removeFilter(removedTransformedFilter);
                }
            }

            @Override
            public void onFilterReplaced(FilterModel model, Filter oldFilter, Filter newFilter) {
                var newTransformedFilter = transformFilter(newFilter);
                var oldTransformedFilter = filters.remove(oldFilter);

                if (newTransformedFilter != null && oldTransformedFilter != null) {
                    replaceFilter(oldTransformedFilter, newTransformedFilter);
                } else if (newTransformedFilter != null) {
                    assert oldTransformedFilter == null;
                    addFilter(newTransformedFilter);
                } else {
                    assert newTransformedFilter == null;
                    assert oldTransformedFilter != null;
                    removeFilter(oldTransformedFilter);
                }
            }
        });
    }

    protected abstract void addFilter(P filter);

    protected abstract void removeFilter(P filter);

    protected abstract void replaceFilter(P oldFilter, P newFilter);

    private @Nullable P transformFilter(Filter filter) {
        return filters.computeIfAbsent(filter, presenterFactory);
    }

    public ImmutableList<P> getFilters() {
        return model.getFilters()
                .stream()
                .map(this::transformFilter)
                .filter(Objects::nonNull)
                .collect(toImmutableList());
    }
}
