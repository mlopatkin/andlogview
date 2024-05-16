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

package name.mlopatkin.andlogview.ui.filtertree;

/**
 * This interface provides information necessary to display a filter in the {@link FilterNodeRenderer}.
 */
public interface FilterNodeViewModel {
    /**
     * Returns true if the filter is enabled.
     *
     * @return true if the filter is currently enabled
     */
    boolean isEnabled();

    /**
     * Returns the text representation of the filter to be shown in the tree
     *
     * @return the text representation of the filter
     */
    String getText();
}
