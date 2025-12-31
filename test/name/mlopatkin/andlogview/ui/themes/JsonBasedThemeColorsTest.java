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

package name.mlopatkin.andlogview.ui.themes;

import name.mlopatkin.andlogview.base.AppResources;
import name.mlopatkin.andlogview.config.Utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

class JsonBasedThemeColorsTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "Light",
            "Test.Light",
            "Test.Dark"
    })
    void canParseBuiltinThemes(String theme) throws Exception {
        var gson = Utils.createConfigurationGson();
        try (
                var res = AppResources.getResource("ui/themes/AndLogView.%s.json".formatted(theme))
                        .asCharSource(StandardCharsets.UTF_8)
                        .openBufferedStream()
        ) {
            JsonBasedThemeColors.fromThemeDefinition(gson.fromJson(res, ThemeColorsJson.class));
        }
    }
}
