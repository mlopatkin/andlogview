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

package name.mlopatkin.andlogview.preferences;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.utils.Try;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * DI-aware accessor for legacy preferences in {@code logview.properties}.
 */
public class LegacyConfiguration {
    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
    private static final Logger log = LoggerFactory.getLogger(LegacyConfiguration.class);

    private final Adb adb = new Adb();
    private final Ui ui = new Ui();

    private final Properties configuration = new Properties();

    public LegacyConfiguration(Properties properties) {
        configuration.putAll(properties);
    }

    Adb adb() {
        return adb;
    }

    Ui ui() {
        return ui;
    }

    class Adb {
        private Adb() {}

        public @Nullable String executable() {
            return getString("adb.executable");
        }

        public @Nullable Boolean isAutoReconnectEnabled() {
            return getBoolean("adb.autoreconnect");
        }
    }

    class Ui {
        private Ui() {}

        public @Nullable Color priorityColor(LogRecord.Priority p) {
            return getColor("ui.priority_color." + p.name());
        }

        public @Nullable Color bookmarkBackground() {
            return getColor("ui.bookmark_background");
        }

        public @Nullable Color bookmarkedForeground() {
            return getColor("ui.bookmark_foreground");
        }

        public @Nullable List<Color> highlightColors() {
            return getColors("ui.highlight_colors");
        }

        public @Nullable Color backgroundColor() {
            return getColor("ui.background_color");
        }

        public @Nullable Boolean bufferEnabled(LogRecord.Buffer buffer) {
            return getBoolean("ui.buffer_enabled." + buffer.name());
        }

        public @Nullable Point mainWindowPosition() {
            return getPoint("ui.main_window_pos");
        }

        public @Nullable Integer mainWindowWidth() {
            return getInteger("ui.main_window_width");
        }

        public @Nullable Integer mainWindowHeight() {
            return getInteger("ui.main_window_height");
        }

        public @Nullable Point processWindowPosition() {
            return getPoint("ui.proc_window_pos");
        }
    }

    private @Nullable String getString(String key) {
        return configuration.getProperty(key);
    }

    private @Nullable Integer getInteger(String key) {
        var value = configuration.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(CharMatcher.whitespace().trimFrom(value));
            } catch (NumberFormatException e) {
                log.error("Cannot parse value `{}` for key `{}` as integer", value, key, e);
            }
        }
        return null;
    }

    private @Nullable Boolean getBoolean(String key) {
        var value = configuration.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return null;
    }

    private @Nullable Color getColor(String key) {
        var value = configuration.getProperty(key);
        if (value != null) {
            try {
                return Color.decode(value);
            } catch (NumberFormatException e) {
                log.error("Cannot parse value `{}` for key `{}` as color", value, key, e);
            }
        }
        return null;
    }

    private @Nullable Point getPoint(String key) {
        var value = configuration.getProperty(key);
        if (value == null) {
            return null;
        }
        var components = COMMA_SPLITTER.limit(3).splitToList(value);
        if (components.size() != 2) {
            log.error("Cannot parse value `{}` for key `{}` as a point", value, key);
            return null;
        }

        try {
            return new Point(Integer.parseInt(components.get(0)), Integer.parseInt(components.get(1)));
        } catch (NumberFormatException e) {
            log.error("Cannot parse value `{}` for key `{}` as a point", value, key, e);
        }

        return null;
    }

    private @Nullable List<Color> getColors(String key) {
        var value = configuration.getProperty(key);
        if (value == null) {
            return null;
        }
        var parseResult = COMMA_SPLITTER.splitToStream(value)
                .map(element -> Try.ofCallable(() -> Color.decode(element)))
                .collect(Try.liftToList())
                .handleError(th ->
                        log.error("Cannot parse value `{}` for key `{}` as a list of colors", value, key, th)
                );
        return parseResult.isPresent() ? parseResult.get() : null;
    }

    public static Optional<LegacyConfiguration> loadIfPresent(File file) throws IOException {
        if (!file.isFile()) {
            return Optional.empty();
        }

        try (var in = Files.newBufferedReader(file.toPath(), StandardCharsets.ISO_8859_1)) {
            var properties = new Properties();
            properties.load(in);
            return Optional.of(new LegacyConfiguration(properties));
        }
    }
}
