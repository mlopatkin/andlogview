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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Default Java L&amp;F (Metal).
 */
class BasicTheme implements Theme {
    @SuppressWarnings("LoggerInitializedWithForeignClass")
    private static final Logger logger = LoggerFactory.getLogger(Theme.class);

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            logger.error("Failed to initialize default L&F, proceeding and hoping for the best", e);
        }
    }

    @Override
    public ThemedWidgetFactory getWidgetFactory() {
        return new BasicWidgetFactory();
    }
}
