/*
 * Copyright 2011 Mikhail Lopatkin
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
package org.bitbucket.mlopatkin.android.logviewer.config;

import static org.bitbucket.mlopatkin.android.logviewer.config.Utils.colorParser;
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.bool;
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.enumMap;
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.integer;
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.list;
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.point;
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.string;
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.type;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

import org.apache.log4j.Logger;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;
import org.bitbucket.mlopatkin.utils.SystemUtils;
import org.bitbucket.mlopatkin.utils.properties.ConfigurationMap;
import org.bitbucket.mlopatkin.utils.properties.IllegalConfigurationException;
import org.bitbucket.mlopatkin.utils.properties.Parsers;
import org.bitbucket.mlopatkin.utils.properties.PropertyBuilder;
import org.bitbucket.mlopatkin.utils.properties.PropertyUtils;
import org.bitbucket.mlopatkin.utils.properties.SynchronizedConfiguration;

public class Configuration {

    public static class ui {

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
            return config.get(COLUMNS_KEY);
        }

        public static Integer tooltipMaxWidth() {
            return config.get(TOOLTIP_MAX_WIDTH_KEY);
        }

        public static Color priorityColor(Priority p) {
            return config.get(PRIORITY_FOREGROUND_KEY, p);
        }

        public static Color bookmarkBackground() {
            return config.get(BOOKMARK_BACKGROUND_KEY);
        }

        public static Color bookmarkedForeground() {
            return config.get(BOOKMARK_FOREGROUND_KEY);
        }

        public static List<Color> highlightColors() {
            return config.get(HIGHLIGHT_FOREGROUNDS_KEY);
        }

        public static Color backgroundColor() {
            return config.get(BACKGROUND_COLOR_KEY);
        }

        public static Boolean bufferEnabled(Buffer buffer) {
            return config.get(BUFFER_ENABLED_KEY, buffer);
        }

        public static Boolean hideLoggingProcesses() {
            return config.get(HIDE_LOGGING_PROCESSES_KEY);
        }

        public static Point mainWindowPosition() {
            return config.get(MAIN_WINDOW_POSITION_KEY);
        }

        public static void mainWindowPosition(Point pos) {
            config.set(MAIN_WINDOW_POSITION_KEY, pos);
        }

        public static Integer mainWindowWidth() {
            return config.get(MAIN_WINDOW_WIDTH_KEY);
        }

        public static void mainWindowWidth(int width) {
            config.set(MAIN_WINDOW_WIDTH_KEY, width);
        }

        public static Integer mainWindowHeight() {
            return config.get(MAIN_WINDOW_HEIGHT_KEY);
        }

        public static void mainWindowHeight(int height) {
            config.set(MAIN_WINDOW_WIDTH_KEY, height);
        }

        public static Point processWindowPosition() {
            return config.get(PROCESS_LIST_WINDOW_POSITION_KEY);
        }

        public static void processWindowPosition(Point pos) {
            config.set(PROCESS_LIST_WINDOW_POSITION_KEY, pos);
        }
    }

    public static class adb {

        private static final String PREFIX = "adb.";

        private static final String SHOW_SETUP_DIALOG_KEY = PREFIX + "show_setup_dialog";
        private static final String EXECUTABLE_KEY = PREFIX + "executable";
        private static final String BUFFERSWITCH_KEY = PREFIX + "bufferswitch";
        private static final String LOGCAT_COMMANDLINE_KEY = PREFIX + "logcat_cmdline";
        private static final String PS_COMMANDLINE_KEY = PREFIX + "ps_cmdline";
        private static final String KMSG_COMMANDLINE_KEY = PREFIX + "kmsg_cmdline";
        private static final String BUFFER_NAME_KEY = PREFIX + "buffer";
        private static final String AUTORECONNECT_KEY = PREFIX + "autoreconnect";

        public static final String DEFAULT_EXECUTABLE = ((SystemUtils.IS_OS_WINDOWS) ? "adb.exe"
                : "adb").intern();

        public static String commandline() {
            return config.get(LOGCAT_COMMANDLINE_KEY);
        }

        public static String bufferswitch() {
            return config.get(BUFFERSWITCH_KEY);
        }

        public static String bufferName(Buffer buffer) {
            return config.get(BUFFER_NAME_KEY, buffer);
        }

        public static String psCommandLine() {
            return config.get(PS_COMMANDLINE_KEY);
        }

        public static String executable() {
            return config.get(EXECUTABLE_KEY);
        }

        public static void executable(String newExecutable) {
            config.set(EXECUTABLE_KEY, newExecutable);
        }

        public static Boolean showSetupDialog() {
            return config.get(SHOW_SETUP_DIALOG_KEY);
        }

        public static void showSetupDialog(boolean value) {
            config.set(SHOW_SETUP_DIALOG_KEY, value);
        }

        public static Boolean isAutoReconnectEnabled() {
            return config.get(AUTORECONNECT_KEY);
        }

        public static void setAutoReconnectEnabled(boolean enabled) {
            config.set(AUTORECONNECT_KEY, enabled);
        }
    }

    public static class dump {

        private static final String PREFIX = "dump.";
        private static final String BUFFER_HEADER_KEY = PREFIX + "buffer";

        public static String bufferHeader(Buffer buffer) {
            return config.get(BUFFER_HEADER_KEY, buffer);
        }
    }

    private static final Logger logger = Logger.getLogger(Configuration.class);

    private Configuration() {
    }

    public static void init() {
        Logging.setUpDefault();
    }

    public static void load(boolean debug) throws IllegalConfigurationException {

        // save on exit
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    Configuration.save();
                } catch (Throwable e) {
                    // exception in shutdown hook is bad
                    logger.error("Exception while saving configuration", e);
                }
            }
        });

        if (debug) {
            Logging.loadDebug();
            logger.info("debug mode on");
        } else {
            Logging.loadNormal();
        }
        Utils.loadConfiguration(config);
    }

    public static void save() {
        Utils.saveConfiguration(config);
    }

    private static final org.bitbucket.mlopatkin.utils.properties.Configuration config;

    static {
        PropertyBuilder<Color> color = type(Color.class, colorParser);
        ConfigurationMap cfg = new ConfigurationMap();

        // @formatter:off
        cfg.property(ui.BACKGROUND_COLOR_KEY, color);
        cfg.property(ui.BOOKMARK_BACKGROUND_KEY, color);
        cfg.property(ui.BOOKMARK_FOREGROUND_KEY, color);
        cfg.property(ui.BUFFER_ENABLED_KEY,
                enumMap(Buffer.class, Boolean.class, Parsers.booleanParser));
        cfg.property(ui.COLUMNS_KEY, list(String.class, Parsers.stringParser));
        cfg.property(ui.HIDE_LOGGING_PROCESSES_KEY, bool().defaultVal(true));
        cfg.property(ui.HIGHLIGHT_FOREGROUNDS_KEY, list(Color.class, colorParser));
        cfg.property(ui.PRIORITY_FOREGROUND_KEY,
                enumMap(Priority.class, Color.class, colorParser));
        cfg.property(ui.TOOLTIP_MAX_WIDTH_KEY, integer());

        cfg.property(ui.MAIN_WINDOW_POSITION_KEY, point().defaultVal(new Point(0, 0)));
        cfg.property(ui.PROCESS_LIST_WINDOW_POSITION_KEY, point().defaultVal(null));
        cfg.property(ui.MAIN_WINDOW_WIDTH_KEY, integer(800));
        cfg.property(ui.MAIN_WINDOW_HEIGHT_KEY, integer(600));

        cfg.property(adb.BUFFER_NAME_KEY,
                enumMap(Buffer.class, String.class, Parsers.stringParser));
        cfg.property(adb.BUFFERSWITCH_KEY, string());
        cfg.property(adb.EXECUTABLE_KEY, string().defaultVal(adb.DEFAULT_EXECUTABLE));
        cfg.property(adb.KMSG_COMMANDLINE_KEY, string());
        cfg.property(adb.LOGCAT_COMMANDLINE_KEY, string());
        cfg.property(adb.PS_COMMANDLINE_KEY, string());
        cfg.property(adb.SHOW_SETUP_DIALOG_KEY, bool().defaultVal(true));
        cfg.property(adb.AUTORECONNECT_KEY, bool().defaultVal(true));

        cfg.property(dump.BUFFER_HEADER_KEY,
                enumMap(Buffer.class, String.class, Parsers.stringParser));
        // @formatter:on

        // setup default values from resource
        PropertyUtils.loadValuesFromResource(cfg, "/logview.properties");

        config = new SynchronizedConfiguration(cfg);
    }

}

