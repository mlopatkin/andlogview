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
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialogData;

import org.checkerframework.checker.nullness.qual.Nullable;

class SavedDialogFilterData extends SavedFilterData {
    private final FilterFromDialogData filterData;
    private transient @Nullable FilterFromDialog filter;

    SavedDialogFilterData(FilterFromDialog filter) {
        super(filter.isEnabled());
        this.filterData = filter.getData();
        this.filter = filter;
    }

    @Override
    public FilterFromDialog fromSerializedForm() throws RequestCompilationException {
        var filter = this.filter;
        if (filter == null) {
            // Deserialized version bypasses the constructor, and has initialized == false.
            this.filter = filter = filterData.toFilter(enabled);
        }
        return filter;
    }
}
