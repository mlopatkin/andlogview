/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.about;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

class OssComponentTest {
    private final Gson gson = new Gson();

    @Test
    void canLoadLicenses() throws Exception {
        var info = gson.fromJson(loadTestResource("dependencies.json"), new TypeToken<List<OssComponent>>() {});

        assertThat(info).hasSize(4);
    }

    private String loadTestResource(String resourceName) throws IOException {
        return Resources.asCharSource(Resources.getResource(getClass(), resourceName), StandardCharsets.UTF_8).read();
    }
}
