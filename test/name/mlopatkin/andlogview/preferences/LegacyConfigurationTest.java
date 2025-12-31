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

package name.mlopatkin.andlogview.preferences;

import static org.assertj.core.api.Assertions.assertThat;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.Color;
import java.awt.Point;
import java.util.List;
import java.util.Properties;

class LegacyConfigurationTest {
    @Test
    void adbExecutableReturnsValueWhenSet() {
        var config = config("adb.executable", "/usr/bin/adb");

        assertThat(config.adb().executable()).isEqualTo("/usr/bin/adb");
    }

    @Test
    void adbExecutableReturnsNullWhenNotSet() {
        var config = emptyConfig();

        assertThat(config.adb().executable()).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "true, true",
            "TRUE, true",
            "false, false",
            "FALSE, false",
            "yes, false",
            "1, false",
            "random, false"
    })
    void adbAutoReconnectParsesBooleanValues(String input, boolean expected) {
        var config = config("adb.autoreconnect", input);

        assertThat(config.adb().isAutoReconnectEnabled()).isEqualTo(expected);
    }

    @Test
    void adbAutoReconnectReturnsNullWhenNotSet() {
        var config = emptyConfig();

        assertThat(config.adb().isAutoReconnectEnabled()).isNull();
    }

    @Test
    void priorityColorReturnsColorWhenSet() {
        var config = config("ui.priority_color.ERROR", "#FF0000");

        assertThat(config.ui().priorityColor(LogRecord.Priority.ERROR))
                .isEqualTo(new Color(0xFF0000));
    }

    @Test
    void priorityColorReturnsNullWhenNotSet() {
        var config = emptyConfig();

        assertThat(config.ui().priorityColor(LogRecord.Priority.VERBOSE)).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "#FF0000, 0xFF0000",
            "#00FF00, 0x00FF00",
            "#0000FF, 0x0000FF",
            "0xFF0000, 0xFF0000",
            "0x00FF00, 0x00FF00"
    })
    void priorityColorParsesValidColorValues(String input, String expectedHex) {
        var config = config("ui.priority_color.ERROR", input);
        var expectedColor = Color.decode(expectedHex);

        assertThat(config.ui().priorityColor(LogRecord.Priority.ERROR))
                .isEqualTo(expectedColor);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-a-color",
            "rgb(255,0,0)",
            "#GGGGGG",
            "",
            "FF0000"
    })
    void priorityColorReturnsNullForInvalidValues(String invalidValue) {
        var config = config("ui.priority_color.ERROR", invalidValue);

        assertThat(config.ui().priorityColor(LogRecord.Priority.ERROR)).isNull();
    }

    @Test
    void bookmarkBackgroundReturnsColorWhenSet() {
        var config = config("ui.bookmark_background", "#FFFF00");

        assertThat(config.ui().bookmarkBackground()).isEqualTo(new Color(0xFFFF00));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-color",
            "#XYZ123",
            "",
            "red"
    })
    void bookmarkBackgroundReturnsNullForInvalidValues(String invalidValue) {
        var config = config("ui.bookmark_background", invalidValue);

        assertThat(config.ui().bookmarkBackground()).isNull();
    }

    @Test
    void bookmarkedForegroundReturnsColorWhenSet() {
        var config = config("ui.bookmark_foreground", "#000000");

        assertThat(config.ui().bookmarkedForeground()).isEqualTo(new Color(0x000000));
    }

    @Test
    void backgroundColorReturnsColorWhenSet() {
        var config = config("ui.background_color", "#FFFFFF");

        assertThat(config.ui().backgroundColor()).isEqualTo(new Color(0xFFFFFF));
    }

    @ParameterizedTest
    @CsvSource({
            "MAIN, ui.buffer_enabled.MAIN, true",
            "SYSTEM, ui.buffer_enabled.SYSTEM, false"
    })
    void bufferEnabledParsesBooleanValues(LogRecord.Buffer buffer, String bufferName, boolean expected) {
        var config = config(bufferName, String.valueOf(expected));

        assertThat(config.ui().bufferEnabled(buffer)).isEqualTo(expected);
    }

    @Test
    void bufferEnabledReturnsNullWhenNotSet() {
        var config = emptyConfig();

        assertThat(config.ui().bufferEnabled(LogRecord.Buffer.MAIN)).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "ui.main_window_width, 800",
            "ui.main_window_width, 1920",
            "ui.main_window_width, 0",
            "ui.main_window_width, -100"
    })
    void mainWindowWidthParsesIntegerValues(String key, int expected) {
        var config = config(key, String.valueOf(expected));

        assertThat(config.ui().mainWindowWidth()).isEqualTo(expected);
    }

    @Test
    void mainWindowWidthTrimsWhitespace() {
        var config = config("ui.main_window_width", "  800  ");

        assertThat(config.ui().mainWindowWidth()).isEqualTo(800);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-a-number",
            "12.34",
            "12abc",
            "",
            "1e5"
    })
    void mainWindowWidthReturnsNullForInvalidValues(String invalidValue) {
        var config = config("ui.main_window_width", invalidValue);

        assertThat(config.ui().mainWindowWidth()).isNull();
    }

    @Test
    void mainWindowWidthReturnsNullWhenNotSet() {
        var config = emptyConfig();

        assertThat(config.ui().mainWindowWidth()).isNull();
    }

    @Test
    void mainWindowHeightParsesIntegerValues() {
        var config = config("ui.main_window_height", "600");

        assertThat(config.ui().mainWindowHeight()).isEqualTo(600);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid",
            "12.34",
            "",
            "abc123"
    })
    void mainWindowHeightReturnsNullForInvalidValues(String invalidValue) {
        var config = config("ui.main_window_height", invalidValue);

        assertThat(config.ui().mainWindowHeight()).isNull();
    }

    @Test
    void mainWindowHeightReturnsNullWhenNotSet() {
        var config = emptyConfig();

        assertThat(config.ui().mainWindowHeight()).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "100, 200",
            "0, 0",
            "-50, -100",
            "1920, 1080"
    })
    void mainWindowPositionParsesValidPointValues(int x, int y) {
        var config = config("ui.main_window_pos", x + "," + y);

        assertThat(config.ui().mainWindowPosition()).isEqualTo(new Point(x, y));
    }

    @Test
    void mainWindowPositionHandlesWhitespace() {
        var config = config("ui.main_window_pos", " 100 , 200 ");

        assertThat(config.ui().mainWindowPosition()).isEqualTo(new Point(100, 200));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "100",
            "100,200,300",
            "100,abc",
            "abc,200",
            "",
            "100,",
            ",200",
            "100, 200, 300"
    })
    void mainWindowPositionReturnsNullForInvalidValues(String invalidValue) {
        var config = config("ui.main_window_pos", invalidValue);

        assertThat(config.ui().mainWindowPosition()).isNull();
    }

    @Test
    void mainWindowPositionReturnsNullWhenNotSet() {
        var config = emptyConfig();

        assertThat(config.ui().mainWindowPosition()).isNull();
    }

    @Test
    void processWindowPositionParsesValidPointValues() {
        var config = config("ui.proc_window_pos", "50,75");

        assertThat(config.ui().processWindowPosition()).isEqualTo(new Point(50, 75));
    }

    @Test
    void highlightColorsParsesValidColorList() {
        var config = config("ui.highlight_colors", "#FF0000,#00FF00,#0000FF");

        assertThat(config.ui().highlightColors()).isEqualTo(List.of(
                new Color(0xFF0000),
                new Color(0x00FF00),
                new Color(0x0000FF)
        ));
    }

    @Test
    void highlightColorsHandlesWhitespace() {
        var config = config("ui.highlight_colors", " #FF0000 , #00FF00 , #0000FF ");

        assertThat(config.ui().highlightColors()).isEqualTo(List.of(
                new Color(0xFF0000),
                new Color(0x00FF00),
                new Color(0x0000FF)
        ));
    }

    @Test
    void highlightColorsParsesEmptyStringAsEmptyList() {
        var config = config("ui.highlight_colors", "");

        assertThat(config.ui().highlightColors()).isEmpty();
    }

    @Test
    void highlightColorsParsesWithTrailingCommas() {
        var config = config("ui.highlight_colors", "#FF0000,#00FF00,");

        assertThat(config.ui().highlightColors()).isEqualTo(List.of(
                new Color(0xFF0000),
                new Color(0x00FF00)
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "#FF0000,invalid,#0000FF",
            "invalid",
            "#FF0000,#GG0000",
            "#FF0000,rgb(0,0,0)",
            "#FF0000,#00FF00,not-a-color"
    })
    void highlightColorsReturnsNullForInvalidValues(String invalidValue) {
        var config = config("ui.highlight_colors", invalidValue);

        assertThat(config.ui().highlightColors()).isNull();
    }

    @Test
    void highlightColorsReturnsNullWhenNotSet() {
        var config = emptyConfig();

        assertThat(config.ui().highlightColors()).isNull();
    }

    // Helper methods to build test Properties
    private static LegacyConfiguration config(String key, String value) {
        var props = new Properties();
        props.setProperty(key, value);
        return new LegacyConfiguration(props);
    }

    private static LegacyConfiguration emptyConfig() {
        return new LegacyConfiguration(new Properties());
    }
}
