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

/**
 * Represents the Application GUI theme and/or Swing L&amp;F.
 */
public interface Theme {
    /**
     * Returns a user-visible name for this theme.
     *
     * @return the name
     */
    String getDisplayName();

    /**
     * Tries to install the theme as the current theme of the application
     *
     * @throws ThemeException if loading or installing the theme fails
     */
    void install() throws ThemeException;

    ThemedWidgetFactory getWidgetFactory();

    /**
     * Returns a set of AndLogView-specific colors to use.
     *
     * @return the colors
     */
    ThemeColors getColors();

    /**
     * Creates a new theme, based on this theme with overrides applied.
     *
     * @param themeName the user-visible name for the new theme
     * @param jsonOverride the overrides
     * @return the new theme
     * @throws ThemeException if the overrides are incorrect or cannot be applied
     */
    Theme withOverrides(String themeName, ThemeColorsJson jsonOverride) throws ThemeException;

    /**
     * Returns the default application theme. It is guaranteed to be supported.
     *
     * @return the default theme
     */
    static Theme getDefault() {
        return light();
    }

    /**
     * Returns the light application theme.
     *
     * @return the light theme
     */
    static Theme light() {
        return FlatLafTheme.LIGHT;
    }

    /**
     * Returns the dark application theme.
     *
     * @return the dark theme
     */
    static Theme dark() {
        return FlatLafTheme.DARK;
    }
}
