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
package name.mlopatkin.andlogview.filters;

public enum FilteringMode {
    /**
     * Show only matching records.
     */
    SHOW("Show only matching lines", true),
    /**
     * Hide matching records.
     */
    HIDE("Hide matching lines", false),
    /**
     * Highlight matching records.
     */
    HIGHLIGHT("Highlight matching lines", false),
    /**
     * Show separate window with search results.
     */
    WINDOW("Show index window", false);

    private final String description;
    private final boolean defaultResult;

    FilteringMode(String description, boolean defaultResult) {
        this.description = description;
        this.defaultResult = defaultResult;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns whether the record should match the combined list of filters if there are no active filters for this
     * mode. For example, we HIDE a record that matches any of the hiding filters in the collection, but we should not
     * HIDE a record if there is no HIDING filters. Contrary to that we SHOW only records that match any of the filters
     * in the collection but if there is no SHOW filters at all whe still should SHOW the record.
     *
     * @return {@code true} if the record should be processed
     */
    public boolean getDefaultResult() {
        return defaultResult;
    }

    public static FilteringMode getDefaultMode() {
        return HIGHLIGHT;
    }
}
