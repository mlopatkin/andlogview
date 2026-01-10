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

import name.mlopatkin.andlogview.base.AppResources;
import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.Preference;
import name.mlopatkin.andlogview.config.SimpleClient;
import name.mlopatkin.andlogview.config.Utils;
import name.mlopatkin.andlogview.features.Features;
import name.mlopatkin.andlogview.ui.themes.JsonBasedThemeColors;
import name.mlopatkin.andlogview.ui.themes.Theme;
import name.mlopatkin.andlogview.ui.themes.ThemeColors;
import name.mlopatkin.andlogview.ui.themes.ThemeColorsJson;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Inject;

/**
 * GUI colors configuration. There are several built-in themes (light, dark). User can potentially define custom themes
 * stored there too.
 */
public class ThemePref {
    private static final String THEME_LIGHT = "light";
    private static final String THEME_DARK = "dark";

    private final Preference<ThemeData> preference;
    // TODO(mlopatkin): the base theme should also be stored in the preferences. Or, even better, maybe we can have
    //  multiple custom user themes without inheritance at all?
    private final ThemeColorsJson baseThemeData;
    private final boolean enableDarkTheme;

    /** Serialized form of the preference */
    private record ThemeData(
            String selectedTheme,
            ThemeColorsJson override
    ) {
        ThemeData(@Nullable String selectedTheme, @Nullable ThemeColorsJson override) {
            // TODO(mlopatkin) Not sure if we should handle nulls this way.
            this.selectedTheme = MoreObjects.firstNonNull(selectedTheme, THEME_LIGHT);
            this.override = MoreObjects.firstNonNull(override, new ThemeColorsJson(null));
        }
    }

    @Inject
    public ThemePref(ConfigStorage storage, Features features) {
        this(storage, getDefaultThemeData(), features.darkModeSelector.isEnabled());
    }

    @VisibleForTesting
    public ThemePref(ConfigStorage storage) {
        this(storage, getDefaultThemeData());
    }

    @VisibleForTesting
    ThemePref(ConfigStorage storage, ThemeColorsJson baseThemeData) {
        this(storage, baseThemeData, true);
    }

    private ThemePref(ConfigStorage storage, ThemeColorsJson baseThemeData, boolean enableDarkTheme) {
        this.preference = storage.preference(new SimpleClient<>(
                "theme",
                ThemeData.class,
                () -> new ThemeData(
                        THEME_LIGHT,
                        new ThemeColorsJson(null)
                )
        ));
        this.baseThemeData = baseThemeData;
        this.enableDarkTheme = enableDarkTheme;
    }

    /**
     * Returns current theme colors based on preferences.
     *
     * @return the color set
     */
    public ThemeColors getThemeColors() {
        return JsonBasedThemeColors.withOverlay(baseThemeData, preference.get().override());
    }

    /**
     * Sets the override JSON for the theme.
     *
     * @param jsonThemeData the override
     */
    public void setOverride(ThemeColorsJson jsonThemeData) {
        if (jsonThemeData.logTable() != null) {
            preference.set(new ThemeData(preference.get().selectedTheme(), jsonThemeData));
        }
    }

    /**
     * Returns the currently selected theme.
     *
     * @return the selected theme.
     */
    public Theme getSelectedTheme() {
        return switch (preference.get().selectedTheme()) {
            case THEME_LIGHT -> Theme.light();
            case THEME_DARK -> enableDarkTheme ? Theme.dark() : Theme.getDefault();
            default -> Theme.getDefault();
        };
    }

    /**
     * Updates the currently selected theme. It must be one of the available themes.
     *
     * @param newTheme the new theme
     */
    public void setTheme(Theme newTheme) {
        Preconditions.checkArgument(
                getAvailableThemes().contains(newTheme),
                "Supplied theme %s is not one of the available themes",
                newTheme.getDisplayName()
        );
        preference.set(new ThemeData(getThemeName(newTheme), preference.get().override()));
    }

    private String getThemeName(Theme theme) {
        if (theme == Theme.dark()) {
            return THEME_DARK;
        }
        // TODO(mlopatkin) this is lame again
        return THEME_LIGHT;
    }

    /**
     * Returns the list of themes available to select. The returned list is never empty.
     *
     * @return the list of themes
     */
    public List<Theme> getAvailableThemes() {
        if (enableDarkTheme) {
            return List.of(Theme.light(), Theme.dark());
        } else {
            return List.of(Theme.light());
        }
    }

    private static ThemeColorsJson getDefaultThemeData() {
        ThemeColorsJson themeData;
        try (
                var res = AppResources.getResource("ui/themes/AndLogView.Light.json")
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
