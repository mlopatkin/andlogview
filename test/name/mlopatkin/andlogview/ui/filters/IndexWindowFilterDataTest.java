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

import static name.mlopatkin.andlogview.filters.FilterModelAssert.assertThatFilters;

import static org.assertj.core.api.Assertions.assertThat;

import name.mlopatkin.andlogview.config.Utils;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.search.RequestCompilationException;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialogData;
import name.mlopatkin.andlogview.ui.filterdialog.IndexWindowFilter;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import org.junit.jupiter.api.Test;

import java.util.Objects;

class IndexWindowFilterDataTest {
    @Test
    void canSerializeAndDeserializeFilter() throws Exception {
        var filter = createFilter("ActivityManager");
        var data = new IndexWindowFilterData(filter);

        var restoredFilter = roundTrip(data);

        assertThat(restoredFilter.getMode()).isEqualTo(FilteringMode.WINDOW);
        assertThat(restoredFilter.getData().getTags()).containsExactly("ActivityManager");
    }

    @Test
    void canSerializeAndDeserializeFilterWithChild() throws Exception {
        var filter = createFilter("ActivityManager", "PackageManager");
        var childFilter = createFilter("ActivityManager");
        filter.getChildren().addFilter(childFilter);

        var data = new IndexWindowFilterData(filter);

        var restoredFilter = roundTrip(data);

        // A first element of a child filter is always
        assertThatFilters(restoredFilter.getChildren()).satisfiesExactly(
                first -> assertThat(first.getMode()).isEqualTo(FilteringMode.HIDE),
                second -> assertThat(second).matches(restoredChild ->
                        (restoredChild instanceof FilterFromDialog ffd)
                                && ffd.isEnabled() == childFilter.isEnabled()
                                && Objects.equals(ffd.getData(), childFilter.getData())
                )
        );
    }

    private static IndexWindowFilter createFilter(String... tags) throws RequestCompilationException {
        return (IndexWindowFilter) new FilterFromDialogData(FilteringMode.WINDOW)
                .setTags(ImmutableList.copyOf(tags))
                .toFilter();
    }


    private static IndexWindowFilter roundTrip(IndexWindowFilterData data) throws RequestCompilationException {
        var gson = gson();
        return gson.fromJson(gson.toJson(data), IndexWindowFilterData.class).fromSerializedForm();
    }

    private static Gson gson() {
        return Utils.createConfigurationGson()
                .newBuilder()
                .registerTypeAdapterFactory(FilterGlobals.typeAdapterFactory())
                .create();
    }
}
