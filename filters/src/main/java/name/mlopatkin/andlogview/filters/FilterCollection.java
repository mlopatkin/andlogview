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

/**
 * A filtered and transformed view of contents of the {@link FilterModel}. This is basically an observer.
 *
 * @param <T> the type of the derived element
 */
public interface FilterCollection<T> {
    /**
     * Called when a new filter is added to {@link FilterModel}, which is not filtered out.
     *
     * @param filter the transformed filter to be added
     */
    void addFilter(T filter);

    /**
     * Called when a filter was removed from {@link FilterModel}, which was not filtered out before. The supplied
     * instance may or may not be passed to {@link #addFilter(Object)} or {@link #replaceFilter(Object, Object)}}
     * before.
     *
     * @param filter the transformed filter to be removed
     */
    void removeFilter(T filter);

    /**
     * Called when a filter is being replaced with another in {@link FilterModel}.
     *
     * @param oldFilter the transformed filter to be removed
     * @param newFilter the transformed filter to be added instead
     * @implSpec the default implementation calls {@link #removeFilter(Object)}, then
     *         {@link #addFilter(Object)}.
     */
    default void replaceFilter(T oldFilter, T newFilter) {
        removeFilter(oldFilter);
        addFilter(newFilter);
    }

    /**
     * Transforms and potentially filters out the filter. The returned value is going to be supplied to
     * {@link #addFilter(Object)}, {@link #removeFilter(Object)}, or {@link #replaceFilter(Object, Object)}, unless
     * {@code null} is returned.
     * <p>
     * If this method filters one but not other argument of {@link FilterModel#replaceFilter(Filter, Filter)}, then only
     * {@link #addFilter(Object)} or {@link #removeFilter(Object)} methods of this object are going to be called.
     *
     * @param filter the filter that is being operated by the {@link FilterModel}
     * @return the transformed filter or {@code null} if the implementation is not interested in this particular filter
     */
    @Nullable
    T transformFilter(Filter filter);

    /**
     * Connects this object to the {@link FilterModel}. This method should only be called once. There is no need to
     * override the default implementation.
     *
     * @param model the model to connect this collection to
     * @return the observer that is subscribed to the provided model. It can be used to unsubscribe.
     */
    default FilterModel.Observer setModel(FilterModel model) {
        for (var filter : model.getFilters()) {
            var transformed = transformFilter(filter);
            if (transformed != null) {
                addFilter(transformed);
            }
        }

        var observer = new TransformingObserver<>(this::transformFilter) {
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
