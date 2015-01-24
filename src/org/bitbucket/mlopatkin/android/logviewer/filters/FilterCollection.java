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

public interface FilterCollection {
    void addFilter(FilteringMode mode, Predicate<LogRecord> filter);
    void setFilterEnabled(FilteringMode mode, Predicate<LogRecord> filter, boolean enabled);
    void removeFilter(FilteringMode mode, Predicate<LogRecord> filter);
    // TODO make setFilterEnabled a default method when migrating to Java 8
}
