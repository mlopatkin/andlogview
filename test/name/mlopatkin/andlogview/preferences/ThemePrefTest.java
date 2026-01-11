/*
 * Copyright 2026 the Andlogview authors
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.config.Utils;
import name.mlopatkin.andlogview.ui.themes.Theme;
import name.mlopatkin.andlogview.ui.themes.ThemeColorsJson;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.Color;

class ThemePrefTest {
    private final FakeInMemoryConfigStorage storage = new FakeInMemoryConfigStorage(Utils.createConfigurationGson());

    @Test
    void darkThemeNotAvailableIfFeatureDisabled() {
        var pref = createPrefNoDarkMode();

        assertThat(pref.getAvailableThemes()).containsOnly(Theme.light());
    }

    @Test
    void darkThemeAvailableIfFeatureEnabled() {
        var pref = createPrefWithDarkMode();


        assertThat(pref.getAvailableThemes()).containsOnly(Theme.light(), Theme.dark());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void legacyThemeAvailableIfImported(boolean enableDarkMode) {
        var pref = enableDarkMode ? createPrefWithDarkMode() : createPrefNoDarkMode();

        pref.setOverride(new ThemeColorsJson(ThemeColorsJson.LogTable.create(Color.BLUE, null, null, null)));

        assertThat(pref.getSelectedTheme()).extracting(Theme::getDisplayName).isEqualTo("Legacy");
        assertThat(pref.getAvailableThemes()).map(Theme::getDisplayName).contains("Legacy");
    }

    @Test
    void canLoadAfterDisablingDarkModeFeature() {
        var initialPref = createPrefWithDarkMode();

        initialPref.setTheme(Theme.dark());

        var afterRestartWithoutDarkMode = createPrefNoDarkMode();

        assertThat(afterRestartWithoutDarkMode.getAvailableThemes()).containsOnly(Theme.light());
        assertThat(afterRestartWithoutDarkMode.getSelectedTheme()).isEqualTo(Theme.light());
    }

    @Test
    void canLoadDarkThemeAfterEnablingAfterDisabling() {
        createPrefWithDarkMode().setTheme(Theme.dark());

        assertThat(createPrefNoDarkMode().getSelectedTheme()).isEqualTo(Theme.light());

        var afterEnablingDarkModeAgain = createPrefWithDarkMode();
        assertThat(afterEnablingDarkModeAgain.getSelectedTheme()).isEqualTo(Theme.dark());
    }

    @Test
    void cannotAddArbitraryTheme() {
        var pref = createPrefWithDarkMode();

        assertThatThrownBy(
                () -> pref.setTheme(Theme.light().withOverrides("Custom theme", new ThemeColorsJson(null)))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    private ThemePref createPrefWithDarkMode() {
        return new ThemePref(storage, true);
    }

    private ThemePref createPrefNoDarkMode() {
        return new ThemePref(storage, false);
    }
}
