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

public interface FilterCollection<T extends Filter> {
    void addFilter(T filter);

    void removeFilter(T filter);

    default void setFilterEnabled(T filter, boolean enabled) {
        if (enabled) {
            addFilter(filter);
        } else {
            removeFilter(filter);
        }
    }

    default void replaceFilter(T oldFilter, T newFilter) {
        removeFilter(oldFilter);
        addFilter(newFilter);
    }
}
