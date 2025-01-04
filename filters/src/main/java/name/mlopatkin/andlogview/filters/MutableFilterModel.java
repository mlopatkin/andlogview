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

import java.util.Collection;

/**
 * A mutable version of {@link FilterModel}.
 */
public interface MutableFilterModel extends FilterModel {
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
     * Returns a sub-model for a give child model filter or {@code null} if the {@code filter} isn't part of this model.
     * @param filter the filter to find a sub model for
     * @return the sub model for the filter or null.
     */
    @Nullable
    FilterModel findSubModel(ChildModelFilter filter);

    /**
     * Creates a new, empty FilterModel.
     *
     * @return the new FilterModel
     */
    static MutableFilterModel create() {
        return new FilterModelImpl();
    }

    /**
     * Creates a new FilterModel with provided filters as its content.
     *
     * @return the new FilterModel
     */
    static MutableFilterModel create(Collection<? extends Filter> filters) {
        return new FilterModelImpl(filters);
    }
}
