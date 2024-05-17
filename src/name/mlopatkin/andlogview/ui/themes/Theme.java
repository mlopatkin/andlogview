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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the Application GUI theme and/or Swing L&amp;F.
 */
public interface Theme {
    /**
     * Returns the name of the theme. The name can be stored in preferences and used to look up the theme.
     *
     * @return the name of the theme
     */
    String getName();

    /**
     * @return {@code true} if the theme is supported on this platform
     */
    boolean isSupported();

    /**
     * Tries to install the theme as the current theme of the application
     *
     * @return {@code true} if the theme was installed successfully
     */
    boolean install();

    ThemedWidgetFactory getWidgetFactory();

    /**
     * Returns the list of all available themes. Some themes may be unsupported on some platforms.
     *
     * @return the list of available themes
     */
    static List<Theme> getAvailableThemes() {
        return Arrays.asList(new MacOsSystemTheme(), new FlatLafTheme(), new BasicTheme());
    }

    /**
     * Returns the default application theme. It is guaranteed to be supported.
     *
     * @return the default theme
     */
    static Theme getDefault() {
        for (Theme theme : getAvailableThemes()) {
            if (theme.isSupported()) {
                return theme;
            }
        }
        return getFallback();
    }

    /**
     * Looks up a theme by the given name and returns it if found. Empty Optional is returned if there is no theme with
     * this name or this theme is unsupported on the current platform.
     *
     * @param themeName the name of the theme (can be {@code null}
     * @return the theme with the given name or an empty Optional
     * @see #getName()
     */
    static Optional<Theme> findByName(@Nullable String themeName) {
        if (themeName != null) {
            for (Theme theme : getAvailableThemes()) {
                if (Objects.equals(themeName, theme.getName()) && theme.isSupported()) {
                    return Optional.of(theme);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * @return the fallback theme which is always supported
     */
    static Theme getFallback() {
        return new BasicTheme();
    }

    /**
     * Checks if the theme supports the filter tree view component properly.
     *
     * @return true if the theme supports filter tree view
     */
    default boolean supportsFilterTreeView() {
        // FilterTreeView uses FlatTreeUI which is part of the FlatLaF.
        return false;
    }
}
