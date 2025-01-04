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
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialogData;
import name.mlopatkin.andlogview.ui.filterdialog.IndexWindowFilter;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class IndexWindowFilterData extends SavedFilterData {
    private final FilterFromDialogData filterData;
    private final List<SavedFilterData> childFilters;
    private transient @Nullable IndexWindowFilter filter;

    public IndexWindowFilterData(IndexWindowFilter filter) {
        super(filter.isEnabled());
        this.filter = filter;
        this.filterData = filter.getData();
        this.childFilters = codec().encode(filter.getChildren().getFilters());
    }

    @Override
    public IndexWindowFilter fromSerializedForm() throws RequestCompilationException {
        var filter = this.filter;
        if (filter == null) {
            this.filter = filter = new IndexWindowFilter(enabled, filterData, codec().decode(childFilters));
        }

        return filter;
    }

    private static FiltersCodec codec() {
        return new FiltersCodec();
    }
}
