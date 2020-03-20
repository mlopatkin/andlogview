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

package org.bitbucket.mlopatkin.android.logviewer.filters;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

import java.util.function.Predicate;

public interface FilterCollection<T extends Predicate<LogRecord>> {
    void addFilter(FilteringMode mode, T filter);

    void removeFilter(FilteringMode mode, T filter);

    default void setFilterEnabled(FilteringMode mode, T filter, boolean enabled) {
        if (enabled) {
            addFilter(mode, filter);
        } else {
            removeFilter(mode, filter);
        }
    }

    default void replaceFilter(FilteringMode mode, T oldFilter, T newFilter) {
        removeFilter(mode, oldFilter);
        addFilter(mode, newFilter);
    }
}
