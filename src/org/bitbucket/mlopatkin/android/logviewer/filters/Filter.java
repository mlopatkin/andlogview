/*
 * Copyright 2014 Mikhail Lopatkin
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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.utils.jsonp.JsonWritable;

/**
 * Filter is just a predicate with metadata. The metadata tells other machinery what to do with the
 * records that satisfy the predicate.
 * <p>
 * Another important property of filter is the ability to serialize itself to JSON.
 */
public abstract class Filter implements JsonWritable {
    // examples of what this system is designed for:
    // - interface for FilterPanel that allows toggling filters on/off temporarily
    // - interface for opening filters in the dialogs
    // - (?) interfaces for FilterHandlers to reach necessary action-specific data

    private final Predicate<LogRecord> predicate;

    public Filter(Predicate<LogRecord> predicate) {
        this.predicate = Preconditions.checkNotNull(predicate);
    }

    public boolean apply(LogRecord logRecord) {
        return predicate.apply(logRecord);
    }
}
