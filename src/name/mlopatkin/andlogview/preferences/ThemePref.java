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

import static com.google.common.collect.ImmutableList.toImmutableList;

import name.mlopatkin.andlogview.base.collections.MyStreams;
import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.Preference;
import name.mlopatkin.andlogview.config.SimpleClient;
import name.mlopatkin.andlogview.features.Features;
import name.mlopatkin.andlogview.ui.themes.Theme;
import name.mlopatkin.andlogview.ui.themes.ThemeColorsJson;
import name.mlopatkin.andlogview.ui.themes.ThemeException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonSyntaxException;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;

/**
 * GUI colors configuration. There are several built-in themes (light, dark). User can potentially define custom themes
 * stored there too.
 */
public class ThemePref {
    private static final Logger log = LoggerFactory.getLogger(ThemePref.class);

    private static final String THEME_LIGHT = "light";
    private static final String THEME_DARK = "dark";
    private static final String THEME_LEGACY = "legacy";

    /** Serialized form of the preference */
    private record ThemeData(
            String selectedTheme,
            List<UserTheme> userThemes
    ) {
        ThemeData(@Nullable String selectedTheme, @Nullable List<@Nullable UserTheme> userThemes) {
            // TODO(mlopatkin) Not sure if we should handle nulls this way.
            this.selectedTheme = MoreObjects.firstNonNull(selectedTheme, THEME_LIGHT);
            this.userThemes = sanitize(userThemes);
        }

        private static List<UserTheme> sanitize(@Nullable List<@Nullable UserTheme> userThemes) {
            if (userThemes == null) {
                return ImmutableList.of();
            }
            return MyStreams.withoutNulls(userThemes.stream()).collect(toImmutableList());
        }

        @SuppressWarnings("RedundantCast")
        public ThemeData withSelectedTheme(String selectedTheme) {
            return new ThemeData(selectedTheme, (List<@Nullable UserTheme>) userThemes());
        }
    }

    private record UserTheme(
            String name,
            String baseTheme,
            ThemeColorsJson override
    ) {
        UserTheme(@Nullable String name, @Nullable String baseTheme, @Nullable ThemeColorsJson override) {
            // TODO(mlopatkin) Not sure if we should handle nulls this way.
            if (name == null) {
                throw new JsonSyntaxException("name is required for the user theme");
            }
            this.name = name;
            this.baseTheme = MoreObjects.firstNonNull(baseTheme, THEME_LIGHT);
            this.override = MoreObjects.firstNonNull(override, new ThemeColorsJson(null));
        }
    }

    private final Preference<ThemeData> preference;

    private Theme theme;
    private final Map<String, Theme> availableThemes = new HashMap<>();

    @Inject
    public ThemePref(ConfigStorage storage, Features features) {
        this(storage, features.darkModeSelector.isEnabled());
    }

    @VisibleForTesting
    public ThemePref(ConfigStorage storage) {
        this(storage, true);
    }

    @VisibleForTesting
    ThemePref(ConfigStorage storage, boolean enableDarkTheme) {
        this.preference = storage.preference(new SimpleClient<>(
                "theme",
                ThemeData.class,
                () -> new ThemeData(THEME_LIGHT, List.of())
        )).memoize();

        // Light theme is always available
        availableThemes.put(THEME_LIGHT, Theme.light());
        if (enableDarkTheme) {
            availableThemes.put(THEME_DARK, Theme.dark());
        }
        preference.get().userThemes().forEach(userThemeData -> {
            if (THEME_LEGACY.equals(userThemeData.name())) {
                try {
                    availableThemes.put(THEME_LEGACY, createLegacyTheme(userThemeData.override()));
                } catch (ThemeException e) {
                    log.error("Cannot parse the legacy theme");
                }
            }
        });

        theme = availableThemes.getOrDefault(preference.get().selectedTheme, Theme.getDefault());
    }

    /**
     * Sets the override JSON for the theme. This enables the "legacy" theme and selects it as active.
     *
     * @param jsonThemeData the override
     */
    public void setOverride(ThemeColorsJson jsonThemeData) {
        // TODO(mlopatkin) What if I already have something? E.g. someone decided to rerun the import.

        if (jsonThemeData.logTable() != null) {
            var legacyTheme = createLegacyTheme(jsonThemeData);
            availableThemes.put(THEME_LEGACY, legacyTheme);
            this.theme = legacyTheme;
            preference.set(
                    new ThemeData(THEME_LEGACY, List.of(new UserTheme(THEME_LEGACY, THEME_LIGHT, jsonThemeData)))
            );
        }
    }

    /**
     * Returns the currently selected theme.
     *
     * @return the selected theme.
     */
    public Theme getSelectedTheme() {
        return theme;
    }

    /**
     * Updates the currently selected theme. It must be one of the available themes.
     *
     * @param newTheme the new theme
     */
    public void setTheme(Theme newTheme) {
        preference.set(preference.get().withSelectedTheme(getThemeName(newTheme)));
        this.theme = newTheme;
    }

    private String getThemeName(Theme theme) {
        return availableThemes.entrySet().stream()
                .filter(e -> e.getValue() == theme)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Supplied theme %s is not one of the available themes", theme.getDisplayName())
                )).getKey();
    }

    /**
     * Returns the list of themes available to select. The returned list is never empty.
     *
     * @return the list of themes
     */
    public List<Theme> getAvailableThemes() {
        return MyStreams.withoutNulls(Stream.of(
                availableThemes.get(THEME_LIGHT),
                availableThemes.get(THEME_DARK),
                availableThemes.get(THEME_LEGACY)
        )).toList();
    }

    private Theme createLegacyTheme(ThemeColorsJson override) {
        return Theme.light().withOverrides("Legacy", override);
    }
}
