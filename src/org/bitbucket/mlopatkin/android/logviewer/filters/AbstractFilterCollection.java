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

import com.google.common.base.Predicate;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

/**
 * Poor man's defender methods.
 */
abstract class AbstractFilterCollection<T extends Predicate<LogRecord>> implements FilterCollection<T> {

    @Override
    public void setFilterEnabled(FilteringMode mode, T filter, boolean enabled) {
        if (enabled) {
            addFilter(mode, filter);
        } else {
            removeFilter(mode, filter);
        }
    }

    @Override
    public void replaceFilter(FilteringMode mode, T oldFilter, T newFilter) {
        removeFilter(mode, oldFilter);
        addFilter(mode, newFilter);
    }
}
