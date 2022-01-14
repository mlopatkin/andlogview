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

package name.mlopatkin.andlogview.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import name.mlopatkin.andlogview.config.Utils;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

class FilterListSerializerTest {
    private final Gson gson = Utils.createConfigurationGson();
    private FilterListSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new FilterListSerializer();
    }

    @Test
    void deserializeExistingFilter() throws Exception {
        JsonElement filtersJson = loadTestResource("filters_v1.json");
        List<MainFilterController.SavedFilterData> filters = serializer.fromJson(gson, filtersJson);

        // TODO(mlopatkin) add more thorough checking here when refactoring filter saving.
        assertEquals(1, filters.size());
    }


    private JsonElement loadTestResource(String resourceName) throws IOException {
        return JsonParser.parseString(
                Resources.asCharSource(Resources.getResource(getClass(), resourceName), StandardCharsets.UTF_8).read());
    }
}
