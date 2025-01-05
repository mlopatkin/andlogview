/*
 * Copyright 2020 Mikhail Lopatkin
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

import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.filters.MutableFilterModel;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class StoredFiltersTest {
    private final Gson gson = new GsonBuilder().registerTypeAdapterFactory(FilterGlobals.typeAdapterFactory()).create();

    @Test
    void deserializeExistingFilter() throws Exception {
        var storage = new FakeInMemoryConfigStorage(gson);
        storage.setJsonData("filters", loadTestResource("filters_v1.json"));
        StoredFilters filters = new StoredFilters(storage, MutableFilterModel.create());

        // TODO(mlopatkin) add more thorough checking here when refactoring filter saving.
        assertThatFilters(filters.getStorageBackedModel()).hasSize(1);
    }

    private String loadTestResource(String resourceName) throws IOException {
        return Resources.asCharSource(Resources.getResource(getClass(), resourceName), StandardCharsets.UTF_8).read();
    }
}
