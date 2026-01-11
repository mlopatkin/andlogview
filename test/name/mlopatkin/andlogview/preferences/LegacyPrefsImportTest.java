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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.config.Utils;
import name.mlopatkin.andlogview.logmodel.LogRecord.Priority;
import name.mlopatkin.andlogview.sdkrepo.AdbLocationDiscovery;
import name.mlopatkin.andlogview.ui.themes.LegacyThemeColors;
import name.mlopatkin.andlogview.utils.FakePathResolver;
import name.mlopatkin.andlogview.utils.LazyInstance;
import name.mlopatkin.andlogview.utils.SystemPathResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.nio.file.Path;
import java.util.List;

class LegacyPrefsImportTest {
    private final LegacyThemeColors legacyColors = new LegacyThemeColors();

    // Default behavior for the legacy configuration is to return nulls for any unspecified property.
    final LegacyConfiguration.Adb adb = mock(LegacyConfiguration.Adb.class, invocation -> null);
    final LegacyConfiguration.Ui ui = mock(LegacyConfiguration.Ui.class, invocation -> null);
    final LegacyConfiguration legacyConfiguration = mock();

    final FakeInMemoryConfigStorage configStorage = new FakeInMemoryConfigStorage(Utils.createConfigurationGson());

    @BeforeEach
    void setUp() {
        lenient().when(legacyConfiguration.adb()).thenReturn(adb);
        lenient().when(legacyConfiguration.ui()).thenReturn(ui);

        lenient().when(ui.backgroundColor()).thenReturn(legacyColors.getBackgroundColor());
        lenient().when(ui.bookmarkBackground()).thenReturn(legacyColors.getBookmarkBackgroundColor());
        lenient().when(ui.bookmarkedForeground()).thenReturn(legacyColors.getBookmarkForegroundColor());
        lenient().when(ui.highlightColors()).thenReturn(legacyColors.getHighlightColors());
        lenient().when(ui.priorityColor(any()))
                .thenAnswer(c -> c.getArgument(0) != null
                        ? legacyColors.getPriorityForegroundColor(c.getArgument(0))
                        : null);
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

        var themeColorsPref = createThemePref();
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        var resultTheme = createThemePref().getThemeColors();
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

        var themeColorsPref = createThemePref();
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        assertThat(createThemePref().getThemeColors().getBackgroundColor()).isEqualTo(customBackground);
    }

    @Test
    void importThemeColors_importsCustomBookmarkColors() {
        var customBookmarkBg = new Color(0xAABBCC);
        var customBookmarkFg = new Color(0xDDEEFF);
        when(ui.bookmarkBackground()).thenReturn(customBookmarkBg);
        when(ui.bookmarkedForeground()).thenReturn(customBookmarkFg);

        var themeColorsPref = createThemePref();
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        var resultTheme = createThemePref().getThemeColors();
        assertThat(resultTheme.getBookmarkBackgroundColor()).isEqualTo(customBookmarkBg);
        assertThat(resultTheme.getBookmarkForegroundColor()).isEqualTo(customBookmarkFg);
    }

    @Test
    void importThemeColors_importsCustomPriorityColors() {
        var customErrorColor = new Color(0xFF1000);
        var customWarnColor = new Color(0xFFFF00);
        when(ui.priorityColor(Priority.ERROR)).thenReturn(customErrorColor);
        when(ui.priorityColor(Priority.WARN)).thenReturn(customWarnColor);

        var themeColorsPref = createThemePref();
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        var resultTheme = createThemePref().getThemeColors();
        assertThat(resultTheme.getPriorityForegroundColor(Priority.ERROR)).isEqualTo(customErrorColor);
        assertThat(resultTheme.getPriorityForegroundColor(Priority.WARN)).isEqualTo(customWarnColor);
        assertThat(resultTheme.getPriorityForegroundColor(Priority.INFO))
                .isEqualTo(legacyColors.getPriorityForegroundColor(Priority.INFO));
    }

    @Test
    void importThemeColors_importsCustomHighlightColors() {
        var customHighlights = List.of(
                new Color(0xFF0000),
                new Color(0x00FF00),
                new Color(0x0000FF)
        );
        when(ui.highlightColors()).thenReturn(customHighlights);

        var themeColorsPref = createThemePref();
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        assertThat(createThemePref().getThemeColors().getHighlightColors()).isEqualTo(customHighlights);
    }

    @Test
    void importThemeColors_doesNotImportColorsSameAsBaseTheme() {
        var themeColorsPref = createThemePref();
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        var resultTheme = createThemePref().getThemeColors();

        assertThat(resultTheme.getBackgroundColor()).isEqualTo(legacyColors.getBackgroundColor());
        assertThat(resultTheme.getBookmarkBackgroundColor()).isEqualTo(legacyColors.getBookmarkBackgroundColor());
        assertThat(resultTheme.getBookmarkForegroundColor()).isEqualTo(legacyColors.getBookmarkForegroundColor());
        assertThat(resultTheme.getPriorityForegroundColor(Priority.ERROR))
                .isEqualTo(legacyColors.getPriorityForegroundColor(Priority.ERROR));
        assertThat(resultTheme.getHighlightColors()).isEqualTo(legacyColors.getHighlightColors());
    }

    @Test
    void importThemeColors_handlesNullLegacyColors() {
        when(ui.backgroundColor()).thenReturn(null);
        when(ui.bookmarkBackground()).thenReturn(null);
        when(ui.bookmarkedForeground()).thenReturn(null);
        when(ui.priorityColor(any())).thenReturn(null);
        when(ui.highlightColors()).thenReturn(null);

        var themeColorsPref = createThemePref();
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        var resultTheme = createThemePref().getThemeColors();

        assertThat(resultTheme.getBackgroundColor()).isEqualTo(legacyColors.getBackgroundColor());
        assertThat(resultTheme.getBookmarkBackgroundColor()).isEqualTo(legacyColors.getBookmarkBackgroundColor());
        assertThat(resultTheme.getBookmarkForegroundColor()).isEqualTo(legacyColors.getBookmarkForegroundColor());
        assertThat(resultTheme.getPriorityForegroundColor(Priority.ERROR))
                .isEqualTo(legacyColors.getPriorityForegroundColor(Priority.ERROR));
        assertThat(resultTheme.getHighlightColors()).isEqualTo(legacyColors.getHighlightColors());
    }

    @Test
    void importThemeColors_importsMixOfCustomAndDefaultColors() {
        var customBackground = new Color(0x123456);
        var customErrorColor = new Color(0xFF1000);

        when(ui.backgroundColor()).thenReturn(customBackground);
        when(ui.priorityColor(Priority.ERROR)).thenReturn(customErrorColor);

        var themeColorsPref = createThemePref();
        var importer = createImport(themeColorsPref);

        importer.importLegacyPreferences(legacyConfiguration);

        var resultTheme = createThemePref().getThemeColors();
        assertThat(resultTheme.getBackgroundColor()).isEqualTo(customBackground);
        assertThat(resultTheme.getBookmarkBackgroundColor()).isEqualTo(legacyColors.getBookmarkBackgroundColor());
        assertThat(resultTheme.getBookmarkForegroundColor()).isEqualTo(legacyColors.getBookmarkForegroundColor());
        assertThat(resultTheme.getPriorityForegroundColor(Priority.ERROR)).isEqualTo(customErrorColor);
        assertThat(resultTheme.getPriorityForegroundColor(Priority.WARN))
                .isEqualTo(legacyColors.getPriorityForegroundColor(Priority.WARN));
        assertThat(resultTheme.getHighlightColors()).isEqualTo(legacyColors.getHighlightColors());
    }

    LegacyPrefsImport createImport(AdbConfigurationPref adbPref) {
        return new LegacyPrefsImport(
                configStorage,
                LazyInstance.of(new WindowsPositionsPref(configStorage)),
                LazyInstance.of(adbPref),
                LazyInstance.of(createThemePref())
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

    private ThemePref createThemePref() {
        return new ThemePref(configStorage);
    }
}
