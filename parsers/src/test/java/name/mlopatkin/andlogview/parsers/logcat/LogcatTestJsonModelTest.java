/*
 * Copyright 2023 the Andlogview authors
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

package name.mlopatkin.andlogview.parsers.logcat;

import static org.assertj.core.api.Assertions.assertThat;

import name.mlopatkin.andlogview.base.AppResources;

import com.google.gson.Gson;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class LogcatTestJsonModelTest {
    @Test
    void jsonModelCanBeParsed() throws Exception {
        var gson = new Gson();

        try (var input = AppResources.getResource("parsers/logcat/golden.json")
                .asCharSource(StandardCharsets.UTF_8)
                .openBufferedStream()) {
            var model = gson.fromJson(input, LogcatTestJsonModel.class);

            assertThat(model.getResourceForFormat("parsers/logcat", Format.TIME)).isPresent();
            assertThat(model.getResourceForFormat("parsers/logcat", Format.STUDIO)).isNotPresent();
            assertThat(model.getRecords(Format.THREADTIME)).isNotEmpty();
        }
    }
}
