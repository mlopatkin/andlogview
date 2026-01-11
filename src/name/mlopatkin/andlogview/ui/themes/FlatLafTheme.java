/*
 * Copyright 2022 the Andlogview authors
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

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.google.common.io.Resources;
import com.google.gson.JsonParseException;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * FlatLaf L&amp;F with Light Flat IDEA theme.
 */
class FlatLafTheme implements Theme {
    static final Theme LIGHT = new FlatLafTheme("Light", FlatLafTheme::createLight, FlatLafTheme::loadJson);
    static final Theme DARK = new FlatLafTheme("Dark", FlatLafTheme::createDark, FlatLafTheme::loadJson);

    private final String displayName;
    private final Supplier<FlatLaf> lafBuilder;
    private final ThemeColors themeColors;
    private final Supplier<ThemeColorsJson> themeJson;

    static {
        FlatLaf.registerCustomDefaultsSource(FlatLafTheme.class.getPackageName());
    }

    private FlatLafTheme(String displayName, Supplier<FlatLaf> lafBuilder, Supplier<ThemeColorsJson> themeJson) {
        this.displayName = displayName;
        this.lafBuilder = lafBuilder;
        this.themeJson = themeJson;
        this.themeColors = JsonBasedThemeColors.fromThemeDefinition(themeJson.get());
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void install() {
        if (!FlatLaf.setup(lafBuilder.get())) {
            failure(displayName, null);
        }
    }

    @Override
    public ThemedWidgetFactory getWidgetFactory() {
        return new FlatLafWidgetFactory();
    }

    @Override
    public ThemeColors getColors() {
        return themeColors;
    }

    private static FlatLaf createLight() throws ThemeException {
        try {
            return IntelliJTheme.createLaf(Resources.asByteSource(FlatLafThemes.LIGHTFLAT.getUrl()).openStream());
        } catch (IOException e) {
            throw failure("Light", e);
        }
    }

    private static FlatLaf createDark() {
        return new FlatDarkLaf();
    }

    private static ThemeException failure(String theme, @Nullable Throwable exception) {
        throw new ThemeException("Failed to load %s theme".formatted(theme), exception);
    }

    @Override
    public String toString() {
        return "FlatLafTheme(" + getDisplayName() + ")";
    }

    @Override
    public Theme withOverrides(String themeName, ThemeColorsJson jsonOverride) throws ThemeException {
        return new FlatLafTheme(themeName, lafBuilder, () -> themeJson.get().merge(jsonOverride));
    }

    private static ThemeColorsJson loadJson() throws ThemeException {
        try (
                var res = AppResources.getResource("ui/themes/AndLogView.Light.json")
                        .asCharSource(StandardCharsets.UTF_8)
                        .openBufferedStream()
        ) {
            return Utils.createConfigurationGson().fromJson(res, ThemeColorsJson.class);
        } catch (IOException | JsonParseException e) {
            // TODO(mlopatkin): name is not correct
            throw failure("Light", e);
        }
    }
}
