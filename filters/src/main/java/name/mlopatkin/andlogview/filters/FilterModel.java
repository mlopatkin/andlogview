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

import name.mlopatkin.andlogview.utils.events.Observable;

import java.util.Collection;
import java.util.Collections;

/**
 * A complete set of log filters.
 */
public interface FilterModel {
    /**
     * An observer to be notified about changes in the current filters.
     */
    interface Observer {
        /**
         * Called after the new filter is added to the model.
         *
         * @param model the origin of the notification
         * @param newFilter the new filter
         */
        void onFilterAdded(FilterModel model, Filter newFilter);

        /**
         * Called after the filter is removed from the model.
         *
         * @param model the origin of the notification
         * @param removedFilter the removed filter.
         */
        void onFilterRemoved(FilterModel model, Filter removedFilter);

        /**
         * Called after the filter was replaced with other filter.
         *
         * @param model the origin of the notification
         * @param oldFilter the filter that was removed
         * @param newFilter the filter that was added instead of the {@code oldFilter}
         * @implNote the default implementation just calls {@link #onFilterRemoved(FilterModel, Filter)} and
         *         {@link #onFilterAdded(FilterModel, Filter)}.
         */
        default void onFilterReplaced(FilterModel model, Filter oldFilter, Filter newFilter) {
            onFilterRemoved(model, oldFilter);
            onFilterAdded(model, newFilter);
        }
    }

    /**
     * Adds a new filter to the model if it is not there already. Otherwise, does nothing.
     *
     * @param filter the filter to add
     */
    void addFilter(Filter filter);

    /**
     * Removes the filter from the model if it is present. Otherwise, does nothing.
     *
     * @param filter the filter to remove
     */
    void removeFilter(Filter filter);

    /**
     * Replaces the filter with the new one. This might be more efficient than removing and adding. Throws
     * {@link IllegalArgumentException} if the filter is not in the model or if the filter to add is already in the
     * model. Does nothing if the replacement is the same filter.
     *
     * @param toReplace the filter to remove
     * @param newFilter the filter to add instead
     * @throws IllegalArgumentException if the filter {@code toReplace} is not in the model
     */
    void replaceFilter(Filter toReplace, Filter newFilter);

    /**
     * Allows to subscribe to events.
     *
     * @return Observable to subscribe for
     */
    Observable<Observer> asObservable();

    /**
     * Returns the list of filters currently available in the model. The filters are listed in the order of adding.
     *
     * @return the list of currently available filters
     */
    default Collection<? extends Filter> getFilters() {
        return Collections.emptyList();
    }
}
