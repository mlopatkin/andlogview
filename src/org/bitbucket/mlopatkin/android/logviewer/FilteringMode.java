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
package org.bitbucket.mlopatkin.android.logviewer;

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
    HIGHLIGHT("Highlight matching lines", false);

    private String description;
    private boolean defaultResult;

    private FilteringMode(String description, boolean defaultResult) {
        this.description = description;
        this.defaultResult = defaultResult;
    }

    public String getDescription() {
        return description;
    }

    public boolean getDefaultResult() {
        return defaultResult;
    }

    public static FilteringMode getDefaultMode() {
        return HIGHLIGHT;
    }
}
