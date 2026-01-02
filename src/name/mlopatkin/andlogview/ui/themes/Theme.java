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
     * Tries to install the theme as the current theme of the application
     *
     * @throws ThemeException if loading or installing the theme fails
     */
    void install() throws ThemeException;

    ThemedWidgetFactory getWidgetFactory();

    /**
     * Returns the default application theme. It is guaranteed to be supported.
     *
     * @return the default theme
     */
    static Theme getDefault() {
        return new FlatLafTheme();
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
