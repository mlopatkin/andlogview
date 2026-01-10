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

package name.mlopatkin.andlogview.ui.themes;

/**
 * The controller that manages the current theme.
 */
public class CurrentTheme {
    private Theme theme;

    /**
     * Initializes controller and sets up the given theme as the initial one.
     * @param initialTheme the initial theme
     * @throws ThemeException if loading or installing the theme fails
     */
    public CurrentTheme(Theme initialTheme) throws ThemeException {
        this.theme = initialTheme;
        this.theme.install();
    }

    /**
     * Tries to install the theme as the current theme of the application.
     *
     * @throws ThemeException if loading or installing the theme fails
     */
    public void set(Theme newTheme) throws ThemeException {
        newTheme.install();
        this.theme = newTheme;
    }

    /**
     * Returns the currently installed theme.
     *
     * @return the current theme
     */
    public Theme get() {
        return theme;
    }
}
