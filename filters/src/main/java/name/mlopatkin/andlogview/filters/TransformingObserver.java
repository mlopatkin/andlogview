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

package name.mlopatkin.andlogview.filters;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

/**
 * A helper observer implementation that can apply filtering and transformations to the updated filters.
 *
 * @param <T> the type of the data objects
 */
abstract class TransformingObserver<T> implements FilterModel.Observer {
    private final Function<? super Filter, ? extends @Nullable T> filterFunction;

    /**
     * Creates the observer. The provided function may return null to indicate that the observer is not interested in
     * the notification for this particular filter.
     *
     * @param filterFunction the filter function
     */
    public TransformingObserver(Function<? super Filter, ? extends @Nullable T> filterFunction) {
        this.filterFunction = filterFunction;
    }

    @Override
    public final void onFilterAdded(FilterModel model, Filter newFilter) {
        var transformed = filterFunction.apply(newFilter);
        if (transformed != null) {
            onMyFilterAdded(model, transformed);
        }
    }

    @Override
    public final void onFilterRemoved(FilterModel model, Filter removedFilter) {
        var transformed = filterFunction.apply(removedFilter);
        if (transformed != null) {
            onMyFilterRemoved(model, transformed);
        }
    }

    @Override
    public final void onFilterReplaced(FilterModel model, Filter oldFilter, Filter newFilter) {
        var oldTransformed = filterFunction.apply(oldFilter);
        var newTransformed = filterFunction.apply(newFilter);

        if (oldTransformed != null && newTransformed != null) {
            onMyFilterReplaced(model, oldTransformed, newTransformed);
        } else if (oldTransformed != null) {
            assert newTransformed == null;
            onMyFilterRemoved(model, oldTransformed);
        } else if (newTransformed != null) {
            assert oldTransformed == null;
            onMyFilterAdded(model, newTransformed);
        }
    }

    /**
     * Called when a matching filter is added, or a non-matching filter is replaced with a matching one.
     *
     * @param model the model
     * @param newFilter the data of the added filter
     */
    protected abstract void onMyFilterAdded(FilterModel model, T newFilter);

    /**
     * Called when a matching filter is removed, or a matching filter is replaced with a non-matching one.
     *
     * @param model the model
     * @param removedFilter the data of the removed filter
     */
    protected abstract void onMyFilterRemoved(FilterModel model, T removedFilter);

    /**
     * Called when a matching filter is replaced with a matching new filter.
     *
     * @param model the model
     * @param oldFilter the data of the replaced filter
     * @param newFilter the data of the replacement filter
     */
    protected abstract void onMyFilterReplaced(FilterModel model, T oldFilter, T newFilter);
}
