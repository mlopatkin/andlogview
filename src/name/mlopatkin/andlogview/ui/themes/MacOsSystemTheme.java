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

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * System L&amp;F to be used on Mac OS X.
 */
class MacOsSystemTheme implements Theme {
    @SuppressWarnings("LoggerInitializedWithForeignClass")
    private static final Logger logger = LoggerFactory.getLogger(Theme.class);

    @Override
    public String getName() {
        return "mac";
    }

    @Override
    public boolean isSupported() {
        return SystemUtils.IS_OS_MAC_OSX;
    }

    @Override
    public boolean install() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            return true;
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e) {
            logger.warn("Cannot load macOS native Look and Feel", e);
            return false;
        }
    }

    @Override
    public ThemedWidgetFactory getWidgetFactory() {
        return new BasicWidgetFactory();
    }
}
