/*
 * Copyright 2011 Mikhail Lopatkin and the Andlogview authors
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

import static name.mlopatkin.andlogview.config.Utils.colorParser;
import static name.mlopatkin.andlogview.utils.properties.PropertyTraits.bool;
import static name.mlopatkin.andlogview.utils.properties.PropertyTraits.enumMap;
import static name.mlopatkin.andlogview.utils.properties.PropertyTraits.integer;
import static name.mlopatkin.andlogview.utils.properties.PropertyTraits.list;
import static name.mlopatkin.andlogview.utils.properties.PropertyTraits.point;
import static name.mlopatkin.andlogview.utils.properties.PropertyTraits.string;
import static name.mlopatkin.andlogview.utils.properties.PropertyTraits.type;

import name.mlopatkin.andlogview.Main;
import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;
import name.mlopatkin.andlogview.logmodel.LogRecord.Priority;
import name.mlopatkin.andlogview.utils.properties.ConfigurationMap;
import name.mlopatkin.andlogview.utils.properties.IllegalConfigurationException;
import name.mlopatkin.andlogview.utils.properties.Parsers;
import name.mlopatkin.andlogview.utils.properties.PropertyBuilder;
import name.mlopatkin.andlogview.utils.properties.PropertyUtils;
import name.mlopatkin.andlogview.utils.properties.SynchronizedConfiguration;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang3.SystemUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

public class Configuration {
    private static final AtomicReference<name.mlopatkin.andlogview.utils.properties.Configuration> config =
            new AtomicReference<>(createDefaultConfiguration());

    public static class ui { // NO CHECKSTYLE

        private static final String PREFIX = "ui.";

        private static final String HIDE_LOGGING_PROCESSES_KEY = PREFIX + "hide_logging_processes";
        private static final String BACKGROUND_COLOR_KEY = PREFIX + "background_color";
        private static final String BOOKMARK_FOREGROUND_KEY = PREFIX + "bookmark_foreground";
        private static final String BOOKMARK_BACKGROUND_KEY = PREFIX + "bookmark_background";
        private static final String PRIORITY_FOREGROUND_KEY = PREFIX + "priority_color";
        private static final String HIGHLIGHT_FOREGROUNDS_KEY = PREFIX + "highlight_colors";
        private static final String TOOLTIP_MAX_WIDTH_KEY = PREFIX + "tooltip_max_width";
        private static final String COLUMNS_KEY = PREFIX + "columns";
        private static final String BUFFER_ENABLED_KEY = PREFIX + "buffer_enabled";

        private static final String MAIN_WINDOW_POSITION_KEY = PREFIX + "main_window_pos";
        private static final String MAIN_WINDOW_WIDTH_KEY = PREFIX + "main_window_width";
        private static final String MAIN_WINDOW_HEIGHT_KEY = PREFIX + "main_window_height";

        private static final String PROCESS_LIST_WINDOW_POSITION_KEY = PREFIX + "proc_window_pos";

        public static List<String> columns() {
            return getConfig().get(COLUMNS_KEY);
        }

        public static Integer tooltipMaxWidth() {
            return getConfig().get(TOOLTIP_MAX_WIDTH_KEY);
        }

        public static Color priorityColor(Priority p) {
            return getConfig().get(PRIORITY_FOREGROUND_KEY, p);
        }

        public static Color bookmarkBackground() {
            return getConfig().get(BOOKMARK_BACKGROUND_KEY);
        }

        public static Color bookmarkedForeground() {
            return getConfig().get(BOOKMARK_FOREGROUND_KEY);
        }

        public static List<Color> highlightColors() {
            return getConfig().get(HIGHLIGHT_FOREGROUNDS_KEY);
        }

        public static Color backgroundColor() {
            return getConfig().get(BACKGROUND_COLOR_KEY);
        }

        public static Boolean bufferEnabled(Buffer buffer) {
            return getConfig().get(BUFFER_ENABLED_KEY, buffer);
        }

        public static Boolean hideLoggingProcesses() {
            return getConfig().get(HIDE_LOGGING_PROCESSES_KEY);
        }

        @Deprecated
        public static Point mainWindowPosition() {
            return getConfig().get(MAIN_WINDOW_POSITION_KEY);
        }

        @Deprecated
        public static Integer mainWindowWidth() {
            return getConfig().get(MAIN_WINDOW_WIDTH_KEY);
        }

        @Deprecated
        public static Integer mainWindowHeight() {
            return getConfig().get(MAIN_WINDOW_HEIGHT_KEY);
        }

        public static Point processWindowPosition() {
            return getConfig().get(PROCESS_LIST_WINDOW_POSITION_KEY);
        }

        public static void processWindowPosition(Point pos) {
            getConfig().set(PROCESS_LIST_WINDOW_POSITION_KEY, pos);
        }
    }

    public static class adb { // NO CHECKSTYLE

        private static final String PREFIX = "adb.";

        private static final String EXECUTABLE_KEY = PREFIX + "executable";
        private static final String KMSG_COMMANDLINE_KEY = PREFIX + "kmsg_cmdline";
        private static final String BUFFER_NAME_KEY = PREFIX + "buffer";
        private static final String AUTORECONNECT_KEY = PREFIX + "autoreconnect";

        public static final String DEFAULT_EXECUTABLE = (SystemUtils.IS_OS_WINDOWS ? "adb.exe" : "adb");


        public static String bufferName(Buffer buffer) {
            return getConfig().get(BUFFER_NAME_KEY, buffer);
        }

        /**
         * Returns the current path to the adb executable, as specified in logview.properties, or null if there is
         * no pre-existing value. This method doesn't fall back to default to help distinguish between not having value
         * and having the value that matches the default.
         *
         * @return the path to ADB specified in the legacy configuration or null if there is nothing.
         */
        @SuppressWarnings("DataFlowIssue") // The annotation on get() is off.
        @Deprecated
        public static @Nullable String executable() {
            return getConfig().get(EXECUTABLE_KEY);
        }

        @Deprecated
        public static Boolean isAutoReconnectEnabled() {
            return getConfig().get(AUTORECONNECT_KEY);
        }

        @Deprecated
        @VisibleForTesting
        public static void executable(String executable) {
            getConfig().set(EXECUTABLE_KEY, executable);
        }
    }

    public static class dump { // NO CHECKSTYLE

        private static final String PREFIX = "dump.";
        private static final String BUFFER_HEADER_KEY = PREFIX + "buffer";

        public static String bufferHeader(Buffer buffer) {
            return getConfig().get(BUFFER_HEADER_KEY, buffer);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private Configuration() {}

    public static void load(File cfgFile) throws IllegalConfigurationException {
        // save on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Configuration.save(cfgFile);
            } catch (Throwable e) { // OK to catch Throwable here
                // exception in shutdown hook is bad
                logger.error("Exception while saving configuration", e);
            }
        }));

        Utils.loadConfiguration(cfgFile, getConfig());
    }

    public static void save(File cfgFile) {
        Utils.saveConfiguration(cfgFile, getConfig());
    }

    private static name.mlopatkin.andlogview.utils.properties.Configuration getConfig() {
        return Objects.requireNonNull(config.get());
    }

    private static name.mlopatkin.andlogview.utils.properties.Configuration createDefaultConfiguration() {
        PropertyBuilder<Color> color = type(Color.class, colorParser);
        ConfigurationMap cfg = new ConfigurationMap();

        // @formatter:off
        cfg.property(ui.BACKGROUND_COLOR_KEY, color);
        cfg.property(ui.BOOKMARK_BACKGROUND_KEY, color);
        cfg.property(ui.BOOKMARK_FOREGROUND_KEY, color);
        cfg.property(ui.BUFFER_ENABLED_KEY, enumMap(Buffer.class, Boolean.class, Parsers.booleanParser));
        cfg.property(ui.COLUMNS_KEY, list(String.class, Parsers.stringParser));
        cfg.property(ui.HIDE_LOGGING_PROCESSES_KEY, bool().defaultVal(true));
        cfg.property(ui.HIGHLIGHT_FOREGROUNDS_KEY, list(Color.class, colorParser));
        cfg.property(ui.PRIORITY_FOREGROUND_KEY, enumMap(Priority.class, Color.class, colorParser));
        cfg.property(ui.TOOLTIP_MAX_WIDTH_KEY, integer());

        cfg.property(ui.MAIN_WINDOW_POSITION_KEY, point().defaultVal(null));
        cfg.property(ui.PROCESS_LIST_WINDOW_POSITION_KEY, point().defaultVal(null));
        cfg.property(ui.MAIN_WINDOW_WIDTH_KEY, integer(800));
        cfg.property(ui.MAIN_WINDOW_HEIGHT_KEY, integer(600));

        cfg.property(adb.BUFFER_NAME_KEY, enumMap(Buffer.class, String.class, Parsers.stringParser));
        cfg.property(adb.EXECUTABLE_KEY, string());
        cfg.property(adb.KMSG_COMMANDLINE_KEY, string());
        cfg.property(adb.AUTORECONNECT_KEY, bool().defaultVal(true));

        cfg.property(dump.BUFFER_HEADER_KEY, enumMap(Buffer.class, String.class, Parsers.stringParser));
        // @formatter:on

        // setup default values from resource
        PropertyUtils.loadValuesFromResource(cfg, Main.class, "logview.properties");

        return new SynchronizedConfiguration(cfg);
    }

    /**
     * A hook for test to execute with known default configuration values. Do not use at production.
     *
     * @param action the action to execute with the default configuration
     * @throws IllegalStateException if the configuration instance is modified outside the test
     * @throws Exception if the {@code action} throws the exception is propagated to the caller
     */
    @VisibleForTesting
    public static void withDefaultConfiguration(Callable<?> action) throws Exception {
        name.mlopatkin.andlogview.utils.properties.Configuration oldConfig = getConfig();
        name.mlopatkin.andlogview.utils.properties.Configuration defaultConfig = createDefaultConfiguration();
        trySetConfig(oldConfig, defaultConfig);
        try {
            action.call();
        } finally {
            trySetConfig(defaultConfig, oldConfig);
        }
    }

    private static void trySetConfig(name.mlopatkin.andlogview.utils.properties.Configuration expectedValue,
            name.mlopatkin.andlogview.utils.properties.Configuration newValue) {
        if (!config.compareAndSet(expectedValue, newValue)) {
            throw new IllegalStateException("Configuration instance was modified while overriding it");
        }
    }
}
