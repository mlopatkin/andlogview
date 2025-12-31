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

package name.mlopatkin.andlogview.config;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

import javax.inject.Inject;

/**
 * DI-aware accessor for legacy preferences in {@code logview.properties}.
 */
public class LegacyConfiguration {
    private final Adb adb = new Adb();
    private final Ui ui = new Ui();

    @Inject
    public LegacyConfiguration() {}

    public Adb adb() {
        return adb;
    }

    public Ui ui() {
        return ui;
    }

    @SuppressWarnings("deprecation")
    public static class Adb {
        private Adb() {}

        public @Nullable String executable() {
            return Configuration.adb.executable();
        }

        public @Nullable Boolean isAutoReconnectEnabled() {
            return Configuration.adb.isAutoReconnectEnabled();
        }
    }

    @SuppressWarnings("deprecation")
    public static class Ui {
        private Ui() {}

        public Color priorityColor(LogRecord.Priority p) {
            return Configuration.ui.priorityColor(p);
        }

        public Color bookmarkBackground() {
            return Configuration.ui.bookmarkBackground();
        }

        public Color bookmarkedForeground() {
            return Configuration.ui.bookmarkedForeground();
        }

        public List<Color> highlightColors() {
            return Configuration.ui.highlightColors();
        }

        public Color backgroundColor() {
            return Configuration.ui.backgroundColor();
        }

        public @Nullable Boolean bufferEnabled(LogRecord.Buffer buffer) {
            return Configuration.ui.bufferEnabled(buffer);
        }

        public @Nullable Point mainWindowPosition() {
            return Configuration.ui.mainWindowPosition();
        }

        public @Nullable Integer mainWindowWidth() {
            return Configuration.ui.mainWindowWidth();
        }

        public @Nullable Integer mainWindowHeight() {
            return Configuration.ui.mainWindowHeight();
        }

        public @Nullable Point processWindowPosition() {
            return Configuration.ui.processWindowPosition();
        }
    }
}
