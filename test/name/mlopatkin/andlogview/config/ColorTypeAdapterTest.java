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

package name.mlopatkin.andlogview.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.Color;

class ColorTypeAdapterTest {
    private final Gson gson = new GsonBuilder().registerTypeAdapter(Color.class, new ColorTypeAdapter()).create();

    @Test
    void canParseNegativeNumericalColorValues() {
        var color = parseColor("""
                {
                  "value": -78912
                }""");

        assertThat(color.getRGB()).inHexadecimal().isEqualTo(0xfffecbc0);
    }

    @Test
    void canParsePositiveNumericalColorValues() {
        var color = parseColor("""
                {
                  "value": 16698304
                }""");

        assertThat(color.getRGB()).inHexadecimal().isEqualTo(0xfffecbc0);
    }

    @Test
    void canParseHexStringValue() {
        var color = parseColor("""
                "#fecbc0"
                """);

        assertThat(color.getRGB()).inHexadecimal().isEqualTo(0xfffecbc0);
    }

    @Test
    void canParseOpaqueHexStringValueWithAlpha() {
        var color = parseColor("""
                "#fffecbc0"
                """);

        assertThat(color.getRGB()).inHexadecimal().isEqualTo(0xfffecbc0);
    }

    @Test
    void canParseTransparentHexStringValueWithAlpha() {
        var color = parseColor("""
                "#00fecbc0"
                """);

        assertThat(color.getRGB()).inHexadecimal().isEqualTo(0x00fecbc0);
    }

    @Test
    void failsOnInvalidPrefix() {
        assertThatThrownBy(() -> parseColor("""
                "0xFFFFFF"
                """)).isInstanceOf(JsonParseException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "#FF",
            "#0FFFFFF",
            "#000FFFFFFF"
    })
    void failsOnInvalidLength(String failValue) {
        assertThatThrownBy(() -> parseColor(String.format("""
                "%s"
                """, failValue))).isInstanceOf(JsonParseException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "#01234Z",
            "##F0000"
    })
    void failsOnInvalidInteger(String failValue) {
        assertThatThrownBy(() -> parseColor(String.format("""
                "%s"
                """, failValue))).isInstanceOf(JsonParseException.class);
    }

    @Test
    void canParseNull() {
        assertThat(parseColor("""
                  null
                """)).isNull();
    }

    @Test
    void canParseNullField() {
        record Colored(@Nullable Color color) {}

        var colored = gson.fromJson("""
                {
                    "color": null
                }
                """, Colored.class);

        assertThat(colored.color).isNull();
    }

    @ParameterizedTest
    @ValueSource(longs = {
            0xFFA1B201,
            0x00000000,
            0x01020304
    })
    void writesColorInLegacyFormat(long rgba) {
        var color = new Color((int) rgba, true);

        record ColorRepresentation(int value) {}

        var json =  gson.toJson(color);
        var r = gson.fromJson(json, ColorRepresentation.class);

        assertThat(r.value).isEqualTo(color.getRGB());
    }

    private Color parseColor(@Language("JSON") String json) {
        return gson.fromJson(json, Color.class);
    }
}
