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

/**
 * A complete set of log filters. This interface is a read-only view, but it contents may still change.
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
         */
        void onFilterReplaced(FilterModel model, Filter oldFilter, Filter newFilter);
    }

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
    Collection<? extends Filter> getFilters();

    /**
     * Creates a filter model with pre-defined filters.
     *
     * @param filters the filters to add to the model
     * @return the model with filters added
     */
    static FilterModel create(Collection<? extends Filter> filters) {
        return new FilterModelImpl(filters);
    }
}
