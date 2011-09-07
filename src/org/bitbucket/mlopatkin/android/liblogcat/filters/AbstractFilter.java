/*
 * Copyright 2011 Mikhail Lopatkin
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
package org.bitbucket.mlopatkin.android.liblogcat.filters;

/**
 * Base class for filters that allows filter decomposition.
 */
abstract class AbstractFilter {
    /**
     * Fill {@code data} with filter data in this method.
     * 
     * @param data
     *            object to be filled
     */
    protected abstract void dumpFilter(FilterData data);

    FilterData dumpFilter() {
        FilterData data = new FilterData();
        dumpFilter(data);
        return data;
    }
}
