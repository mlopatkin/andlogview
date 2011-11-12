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
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.string;
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.type;

import java.awt.Color;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;
import org.bitbucket.mlopatkin.utils.properties.ConfigurationMap;
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

        public static List<String> columns() {
            return config.get(COLUMNS_KEY);
        }

        public static int tooltipMaxWidth() {
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

        public static boolean bufferEnabled(Buffer buffer) {
            return config.get(BUFFER_ENABLED_KEY, buffer);
        }

        public static boolean hideLoggingProcesses() {
            return config.get(HIDE_LOGGING_PROCESSES_KEY);
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

        public static boolean showSetupDialog() {
            return config.get(SHOW_SETUP_DIALOG_KEY);
        }

        public static void showSetupDialog(boolean value) {
            config.set(SHOW_SETUP_DIALOG_KEY, value);
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

        // save on exit
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Configuration.save();
            }
        });
        Logging.setUpDefault();
        Logging.loadDebug();
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
        
        cfg.property(adb.BUFFER_NAME_KEY, 
                enumMap(Buffer.class, String.class, Parsers.stringParser));
        cfg.property(adb.BUFFERSWITCH_KEY, string());
        cfg.property(adb.EXECUTABLE_KEY, string().defaultVal(adb.DEFAULT_EXECUTABLE));
        cfg.property(adb.KMSG_COMMANDLINE_KEY, string());
        cfg.property(adb.LOGCAT_COMMANDLINE_KEY, string());
        cfg.property(adb.PS_COMMANDLINE_KEY, string());
        cfg.property(adb.SHOW_SETUP_DIALOG_KEY, bool().defaultVal(true));
        
        cfg.property(dump.BUFFER_HEADER_KEY, 
                enumMap(Buffer.class, String.class, Parsers.stringParser));
        // @formatter:on

        // setup default values from resource
        PropertyUtils.loadValuesFromResource(cfg, "/logview.properties");

        config = new SynchronizedConfiguration(cfg);
    }

}
