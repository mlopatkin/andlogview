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
import name.mlopatkin.andlogview.filters.FilterCollection;
import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.utils.events.ScopedObserver;

import com.google.common.collect.ImmutableList;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * A base class that implements the common logic of adapting the {@link FilterModel} to filter views.
 */
public abstract class BaseFilterModelAdapter<P extends BaseFilterPresenter> implements FilterCollection<P> {
    private final Function<? super Filter, ? extends @Nullable P> presenterFactory;
    private final Map<Filter, P> filters = new HashMap<>();
    private @MonotonicNonNull FilterModel model;

    protected BaseFilterModelAdapter(Function<? super Filter, ? extends @Nullable P> presenterFactory) {
        this.presenterFactory = presenterFactory;
    }

    @Override
    public ScopedObserver setModel(FilterModel model) {
        this.model = model;
        return FilterCollection.super.setModel(model);
    }

    @Override
    public @Nullable P transformFilter(Filter filter) {
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
