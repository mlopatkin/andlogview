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
import name.mlopatkin.andlogview.ui.themes.JsonBasedThemeColors;
import name.mlopatkin.andlogview.ui.themes.ThemeColors;
import name.mlopatkin.andlogview.ui.themes.ThemeColorsJson;

import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

/**
 * GUI colors configuration. We have a base theme (only one today) and a set of overrides for specific aspects.
 * The user can specify only the overrides.
 */
public class ThemeColorsPref {
    private final Preference<ThemeData> preference;
    // TODO(mlopatkin): the base theme should also be stored in the preferences. Or, even better, maybe we can have
    //  multiple custom user themes without inheritance at all?
    private final ThemeColorsJson baseThemeData;

    /** Serialized form of the preference */
    private record ThemeData(ThemeColorsJson override) {}

    @Inject
    public ThemeColorsPref(ConfigStorage storage) {
        this(storage, getDefaultThemeData());
    }

    @VisibleForTesting
    ThemeColorsPref(ConfigStorage storage, ThemeColorsJson baseThemeData) {
        this.preference = storage.preference(new SimpleClient<>(
                "theme", ThemeData.class, () -> new ThemeData(new ThemeColorsJson(null))
        ));
        this.baseThemeData = baseThemeData;
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
     * @param jsonThemeData the override
     */
    public void setOverride(ThemeColorsJson jsonThemeData) {
        preference.set(new ThemeData(jsonThemeData));
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
