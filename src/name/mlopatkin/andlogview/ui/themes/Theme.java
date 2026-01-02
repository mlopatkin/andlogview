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

import java.util.List;

/**
 * Represents the Application GUI theme and/or Swing L&amp;F.
 */
public interface Theme {
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
        return List.of(new FlatLafTheme());
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
