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

package name.mlopatkin.andlogview.ui.filters;

import name.mlopatkin.andlogview.search.RequestCompilationException;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;

class SavedDialogFilterData extends SavedFilterData {
    private final FilterFromDialog filterData;
    private transient boolean initialized;

    SavedDialogFilterData(FilterFromDialog filterData) {
        super(filterData.isEnabled());
        this.filterData = filterData;
        this.initialized = true;
    }

    @Override
    public FilterFromDialog fromSerializedForm() throws RequestCompilationException {
        if (!initialized) {
            // Deserialized version bypasses the constructor, and has initialized == false.
            filterData.initialize();
            filterData.setEnabled(enabled);
            initialized = true;
        }
        return filterData;
    }
}
