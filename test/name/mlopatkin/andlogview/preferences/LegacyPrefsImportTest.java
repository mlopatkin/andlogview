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

import static name.mlopatkin.andlogview.preferences.LegacyPrefsImportTest.ThemeFlavor.Dark;
import static name.mlopatkin.andlogview.preferences.LegacyPrefsImportTest.ThemeFlavor.Light;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.base.AppResources;
import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.config.Utils;
import name.mlopatkin.andlogview.logmodel.LogRecord.Priority;
import name.mlopatkin.andlogview.sdkrepo.AdbLocationDiscovery;
import name.mlopatkin.andlogview.ui.themes.LegacyThemeColors;
import name.mlopatkin.andlogview.ui.themes.ThemeColorsJson;
import name.mlopatkin.andlogview.utils.FakePathResolver;
import name.mlopatkin.andlogview.utils.LazyInstance;
import name.mlopatkin.andlogview.utils.SystemPathResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

class LegacyPrefsImportTest {
    enum ThemeFlavor {
        Light(Color.WHITE),
        Dark(Color.BLACK);

        public final Color color;

        ThemeFlavor(Color color) {
            this.color = color;
        }
    }

    // Default behavior for the legacy configuration is to return nulls for any unspecified property.
    final LegacyConfiguration.Adb adb = mock(LegacyConfiguration.Adb.class, invocation -> null);
    final LegacyConfiguration.Ui ui = mock(LegacyConfiguration.Ui.class, invocation -> null);
    final LegacyConfiguration legacyConfiguration = mock();

    final FakeInMemoryConfigStorage configStorage = new FakeInMemoryConfigStorage(Utils.createConfigurationGson());

    @BeforeEach
    void setUp() {
        lenient().when(legacyConfiguration.adb()).thenReturn(adb);
        lenient().when(legacyConfiguration.ui()).thenReturn(ui);

        var legacyColors = new LegacyThemeColors();
        lenient().when(ui.backgroundColor()).thenReturn(legacyColors.getBackgroundColor());
        lenient().when(ui.bookmarkBackground()).thenReturn(legacyColors.getBookmarkBackgroundColor());
        lenient().when(ui.bookmarkedForeground()).thenReturn(legacyColors.getBookmarkForegroundColor());
        lenient().when(ui.highlightColors()).thenReturn(legacyColors.getHighlightColors());
        lenient().when(ui.priorityColor(any()))
                .thenAnswer(c -> legacyColors.getPriorityForegroundColor(c.getArgument(0)));
    }

    @Test
    void autoDiscoveryIsNotAllowedIfLegacyAdbIsAvailable() {
        var adbFile = Path.of("foo", "bar", "adb.exe").toFile();
        when(adb.executable()).thenReturn(adbFile.getPath());

        var adbPref = createAdbPref(FakePathResolver.acceptsAnything());
        var importer = createImport(adbPref);

        importer.importLegacyPreferences(legacyConfiguration);

        assertThat(adbPref.getAdbLocation()).isEqualTo(adbFile.getPath());
        assertThat(adbPref.hasValidAdbLocation()).isTrue();
        assertThat(adbPref.getExecutable()).contains(adbFile);
        assertThat(adbPref.isAdbAutoDiscoveryAllowed()).isFalse();
    }

    @Test
    void autoDiscoveryIsAllowedIfLegacyAdbIsAvailableButInvalid() {
        var adbFile = Path.of("foo", "bar", "adb.exe").toFile();
        when(adb.executable()).thenReturn(adbFile.getPath());

        var adbPref = createAdbPref(FakePathResolver.acceptsNothing());
        var importer = createImport(adbPref);

        importer.importLegacyPreferences(legacyConfiguration);

        assertThat(adbPref.hasValidAdbLocation()).isFalse();
        assertThat(adbPref.getAdbLocation()).isEqualTo(AdbLocationDiscovery.ADB_EXECUTABLE);
        assertThat(adbPref.isAdbAutoDiscoveryAllowed()).isTrue();
    }

    @Test
    void importThemeColors_withFullyCustomizedTheme() {
        var customBackground = new Color(0x123456);
        var customBookmarkBg = new Color(0xAABBCC);
        var customBookmarkFg = new Color(0xDDEEFF);
        var customErrorColor = new Color(0xDD1100);
        var customHighlights = List.of(new Color(0x111111), new Color(0x222222));

        when(ui.backgroundColor()).thenReturn(customBackground);
        when(ui.bookmarkBackground()).thenReturn(customBookmarkBg);
        when(ui.bookmarkedForeground()).thenReturn(customBookmarkFg);
        when(ui.priorityColor(Priority.ERROR)).thenReturn(customErrorColor);
        when(ui.highlightColors()).thenReturn(customHighlights);

        var themeColorsPref = createThemePref(Light);
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        var resultTheme = createThemePref(Dark).getThemeColors();
        assertThat(resultTheme.getBackgroundColor()).isEqualTo(customBackground);
        assertThat(resultTheme.getBookmarkBackgroundColor()).isEqualTo(customBookmarkBg);
        assertThat(resultTheme.getBookmarkForegroundColor()).isEqualTo(customBookmarkFg);
        assertThat(resultTheme.getPriorityForegroundColor(Priority.ERROR)).isEqualTo(customErrorColor);
        assertThat(resultTheme.getHighlightColors()).isEqualTo(customHighlights);
    }

    @Test
    void importThemeColors_importsCustomBackgroundColor() {
        var customBackground = new Color(0x123456);
        when(ui.backgroundColor()).thenReturn(customBackground);

        var themeColorsPref = createThemePref(Light);
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        assertThat(createThemePref(Dark).getThemeColors().getBackgroundColor()).isEqualTo(customBackground);
    }

    @Test
    void importThemeColors_importsCustomBookmarkColors() {
        var customBookmarkBg = new Color(0xAABBCC);
        var customBookmarkFg = new Color(0xDDEEFF);
        when(ui.bookmarkBackground()).thenReturn(customBookmarkBg);
        when(ui.bookmarkedForeground()).thenReturn(customBookmarkFg);

        var themeColorsPref = createThemePref(Light);
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        var resultTheme = createThemePref(Dark).getThemeColors();
        assertThat(resultTheme.getBookmarkBackgroundColor()).isEqualTo(customBookmarkBg);
        assertThat(resultTheme.getBookmarkForegroundColor()).isEqualTo(customBookmarkFg);
    }

    @Test
    void importThemeColors_importsCustomPriorityColors() {
        var customErrorColor = new Color(0xFF1000);
        var customWarnColor = new Color(0xFFFF00);
        when(ui.priorityColor(Priority.ERROR)).thenReturn(customErrorColor);
        when(ui.priorityColor(Priority.WARN)).thenReturn(customWarnColor);

        var themeColorsPref = createThemePref(Light);
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        var resultTheme = createThemePref(Dark).getThemeColors();
        assertThat(resultTheme.getPriorityForegroundColor(Priority.ERROR)).isEqualTo(customErrorColor);
        assertThat(resultTheme.getPriorityForegroundColor(Priority.WARN)).isEqualTo(customWarnColor);
        assertThat(resultTheme.getPriorityForegroundColor(Priority.INFO)).isEqualTo(Dark.color);
    }

    @Test
    void importThemeColors_importsCustomHighlightColors() {
        var customHighlights = List.of(
                new Color(0xFF0000),
                new Color(0x00FF00),
                new Color(0x0000FF)
        );
        when(ui.highlightColors()).thenReturn(customHighlights);

        var themeColorsPref = createThemePref(Light);
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        assertThat(createThemePref(Dark).getThemeColors().getHighlightColors()).isEqualTo(customHighlights);
    }

    @Test
    void importThemeColors_doesNotImportColorsSameAsBaseTheme() {
        var themeColorsPref = createThemePref(Light);
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        // Simulate user changing the base theme. Overlay should be empty.
        var resultTheme = createThemePref(Dark).getThemeColors();

        assertThat(resultTheme.getBackgroundColor()).isEqualTo(Dark.color);
        assertThat(resultTheme.getBookmarkBackgroundColor()).isEqualTo(Dark.color);
        assertThat(resultTheme.getBookmarkForegroundColor()).isEqualTo(Dark.color);
        assertThat(resultTheme.getPriorityForegroundColor(Priority.ERROR))
                .isEqualTo(Dark.color);
        assertThat(resultTheme.getHighlightColors()).isEqualTo(List.of(Dark.color));
    }

    @Test
    void importThemeColors_handlesNullLegacyColors() {
        var themeColorsPref = createThemePref(Light);
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        // Simulate user changing the base theme. Overlay should be empty.
        var resultTheme = createThemePref(Dark).getThemeColors();

        assertThat(resultTheme.getBackgroundColor()).isEqualTo(Dark.color);
        assertThat(resultTheme.getBookmarkBackgroundColor()).isEqualTo(Dark.color);
        assertThat(resultTheme.getBookmarkForegroundColor()).isEqualTo(Dark.color);
        assertThat(resultTheme.getPriorityForegroundColor(Priority.ERROR)).isEqualTo(Dark.color);
        assertThat(resultTheme.getHighlightColors()).isEqualTo(List.of(Dark.color));
    }

    @Test
    void importThemeColors_importsMixOfCustomAndDefaultColors() {
        var customBackground = new Color(0x123456);
        var customErrorColor = new Color(0xFF1000);

        when(ui.backgroundColor()).thenReturn(customBackground);
        when(ui.priorityColor(Priority.ERROR)).thenReturn(customErrorColor);

        var themeColorsPref = createThemePref(Light);
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        var resultTheme = createThemePref(Dark).getThemeColors();
        assertThat(resultTheme.getBackgroundColor()).isEqualTo(customBackground);
        assertThat(resultTheme.getBookmarkBackgroundColor()).isEqualTo(Dark.color);
        assertThat(resultTheme.getBookmarkForegroundColor()).isEqualTo(Dark.color);
        assertThat(resultTheme.getPriorityForegroundColor(Priority.ERROR)).isEqualTo(customErrorColor);
        assertThat(resultTheme.getPriorityForegroundColor(Priority.WARN)).isEqualTo(Dark.color);
        assertThat(resultTheme.getHighlightColors()).isEqualTo(List.of(Dark.color));
    }

    LegacyPrefsImport createImport(AdbConfigurationPref adbPref) {
        return new LegacyPrefsImport(
                configStorage,
                LazyInstance.of(new WindowsPositionsPref(configStorage)),
                LazyInstance.of(adbPref),
                LazyInstance.of(createThemePref(Light))
        );
    }

    LegacyPrefsImport createImport(ThemePref themePref) {
        return new LegacyPrefsImport(
                configStorage,
                LazyInstance.of(new WindowsPositionsPref(configStorage)),
                LazyInstance.of(createAdbPref(FakePathResolver.acceptsAnything())),
                LazyInstance.of(themePref)
        );
    }

    AdbConfigurationPref createAdbPref(SystemPathResolver pathResolver) {
        return new AdbConfigurationPref(configStorage, pathResolver);
    }

    private ThemePref createThemePref(ThemeFlavor themeFlavor) {
        return new ThemePref(configStorage, loadTestTheme(themeFlavor));
    }

    private static ThemeColorsJson loadTestTheme(ThemeFlavor themeFlavor) {
        ThemeColorsJson themeData;
        try (
                var res = AppResources.getResource("ui/themes/AndLogView.Test.%s.json".formatted(themeFlavor.name()))
                        .asCharSource(StandardCharsets.UTF_8)
                        .openBufferedStream()
        ) {
            themeData = Utils.createConfigurationGson().fromJson(res, ThemeColorsJson.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return themeData;
    }
}
