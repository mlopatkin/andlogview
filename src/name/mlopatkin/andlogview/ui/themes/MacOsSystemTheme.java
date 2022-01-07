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

import name.mlopatkin.andlogview.thirdparty.systemutils.SystemUtils;

import org.apache.log4j.Logger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * System L&amp;F to be used on MacOS X.
 */
class MacOsSystemTheme implements Theme {
    private static final Logger logger = Logger.getLogger(Theme.class);

    @Override
    public String getName() {
        return "mac";
    }

    @Override
    public boolean isSupported() {
        return SystemUtils.IS_OS_MACOS;
    }

    @Override
    public boolean install() {
        // Move JMenuBar to macOS native global Menu bar.
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        // Change App name in menu bar.
        System.setProperty("apple.awt.application.name", "AndLogView");
        // Force default light style even with global system dark mode to fix black-on-black text in some controls.
        System.setProperty("apple.awt.application.appearance", "NSAppearanceNameAqua");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            return true;
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e) {
            logger.warn("Cannot load macOS native Look and Feel", e);
            return false;
        }
    }
}
