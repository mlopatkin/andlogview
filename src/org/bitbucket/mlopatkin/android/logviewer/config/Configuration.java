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

import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.bool;
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.enumMap;
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.integer;
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.list;
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.string;
import static org.bitbucket.mlopatkin.utils.properties.PropertyTraits.type;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;
import org.bitbucket.mlopatkin.utils.MyStringUtils;
import org.bitbucket.mlopatkin.utils.properties.Parser;
import org.bitbucket.mlopatkin.utils.properties.Parsers;
import org.bitbucket.mlopatkin.utils.properties.PropertyBuilder;
import org.bitbucket.mlopatkin.utils.properties.PropertyUtils;

public class Configuration {
    private static final boolean DEBUG_MODE = System.getProperty("logview.debug") != null;

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
            return cfg.get(COLUMNS_KEY);
        }

        public static int tooltipMaxWidth() {
            return cfg.get(TOOLTIP_MAX_WIDTH_KEY);
        }

        public static Color priorityColor(Priority p) {
            return cfg.get(PRIORITY_FOREGROUND_KEY, p);
        }

        public static Color bookmarkBackground() {
            return cfg.get(BOOKMARK_BACKGROUND_KEY);
        }

        public static Color bookmarkedForeground() {
            return cfg.get(BOOKMARK_FOREGROUND_KEY);
        }

        public static List<Color> highlightColors() {
            return cfg.get(HIGHLIGHT_FOREGROUNDS_KEY);
        }

        public static Color backgroundColor() {
            return cfg.get(BACKGROUND_COLOR_KEY);
        }

        public static boolean bufferEnabled(Buffer buffer) {
            return cfg.get(BUFFER_ENABLED_KEY, buffer);
        }

        public static boolean hideLoggingProcesses() {
            return cfg.get(HIDE_LOGGING_PROCESSES_KEY);
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
            return cfg.get(LOGCAT_COMMANDLINE_KEY);
        }

        public static String bufferswitch() {
            return cfg.get(BUFFERSWITCH_KEY);
        }

        public static String bufferName(Buffer buffer) {
            return cfg.get(BUFFER_NAME_KEY, buffer);
        }

        public static String psCommandLine() {
            return cfg.get(PS_COMMANDLINE_KEY);
        }

        public static String executable() {
            return cfg.get(EXECUTABLE_KEY);
        }

        public static void executable(String newExecutable) {
            cfg.set(EXECUTABLE_KEY, newExecutable);
        }

        public static boolean showSetupDialog() {
            return cfg.get(SHOW_SETUP_DIALOG_KEY);
        }

        public static void showSetupDialog(boolean value) {
            cfg.set(SHOW_SETUP_DIALOG_KEY, value);
        }
    }

    public static class dump {
        private static final String PREFIX = "dump.";
        private static final String BUFFER_HEADER_KEY = PREFIX + "buffer";

        public static String bufferHeader(Buffer buffer) {
            return cfg.get(BUFFER_HEADER_KEY, buffer);
        }
    }

    private static final Logger logger = Logger.getLogger(Configuration.class);

    private Properties properties = new Properties();

    private void setUpDefaults() {
        // set up default logging configuration
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout(
                PatternLayout.TTCC_CONVERSION_PATTERN), ConsoleAppender.SYSTEM_ERR));
    }

    private Properties loadFromResources() {
        Properties result = new Properties();
        try {
            InputStream in = getClass().getResourceAsStream("/" + CONFIG_FILE_NAME);
            if (in == null) {
                logger.error("Missing configuration file in resources - broken package?");
                return result;
            }
            try {
                result.load(in);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            logger.error("Unexpected error when parsing properties", e);
        }
        return result;
    }

    private Properties loadFromFile(String fileName) {
        Properties result = new Properties();
        File configFile = new File(fileName);
        if (configFile.exists()) {
            try {
                InputStream in = new FileInputStream(configFile);
                try {
                    result.load(in);
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                logger.error("Unexpected error when parsing properties", e);
            }
        }
        return result;
    }

    private Configuration() {
        if (DEBUG_MODE) {
            System.err.println("DEBUG MODE ENABLED!");
        }
        setUpDefaults();
        properties.putAll(loadFromResources());
        properties.putAll(loadFromFile(getConfigFileName()));
        PropertyConfigurator.configure(properties);
    }

    private static Configuration instance = new Configuration();

    public static void forceInit() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Configuration.save();
            }
        });
    }

    private static final String CONFIG_FILE_DIR = ".logview";
    private static final String CONFIG_FILE_DIR_WIN = "logview";
    private static final String CONFIG_FILE_NAME = "logview.properties";

    private String getSystemConfigDir() {
        if (SystemUtils.IS_OS_WINDOWS) {
            String appdata = System.getenv("APPDATA");
            // dirty hack to get eclipse work properly with the environment
            // variables
            // when I start project in Debug under JDK 1.6_22 debug JRE it
            // receives environment variables in CP866 but thinks that they are
            // in CP1251. My login contains russian letters and APPDATA points
            // to nowhere :(
            if (DEBUG_MODE && !(new File(appdata).exists())) {
                logger.warn("DEBUG_MODE is ON");
                logger.warn("Appdata value: " + Arrays.toString(appdata.getBytes()));
                try {
                    appdata = new String(appdata.getBytes("WINDOWS-1251"), "CP866");
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(e.toString());
                }
            }
            return appdata;
        } else {
            return SystemUtils.USER_HOME;
        }
    }

    private String getConfigFileDir() {
        String systemConfig = getSystemConfigDir();
        if (systemConfig == null) {
            return null;
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            return MyStringUtils.joinPath(systemConfig, CONFIG_FILE_DIR_WIN);
        } else {
            return MyStringUtils.joinPath(systemConfig, CONFIG_FILE_DIR);
        }
    }

    private String getConfigFileName() {
        String configDir = getConfigFileDir();
        if (configDir == null) {
            return null;
        }
        return MyStringUtils.joinPath(configDir, CONFIG_FILE_NAME);
    }

    private void ensureDir() {
        String dir = getConfigFileDir();
        if (dir != null) {
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
        }
    }

    private void saveToFile() {
        String configFile = getConfigFileName();
        if (configFile == null) {
            logger.error("Could not obtain system config file dir");
            return;
        }
        File file = new File(configFile);
        ensureDir();
        try {
            FileOutputStream output = new FileOutputStream(file);
            try {
                cfg.save(output);
            } finally {
                output.close();
            }
        } catch (IOException e) {
            logger.error("Cannot save properties", e);
        }
    }

    public static void save() {
        instance.saveToFile();
    }

    private static final Parser<Color> colorParser = new Parser<Color>() {

        @Override
        public Color read(String value) {
            return Color.decode(value);
        }

        @Override
        public String write(Color value) {
            return String
                    .format("#%02x%02x%02x", value.getRed(), value.getGreen(), value.getBlue());
        }

    };

    private static final org.bitbucket.mlopatkin.utils.properties.Configuration cfg = new org.bitbucket.mlopatkin.utils.properties.Configuration();
    static {
        PropertyBuilder<Color> color = type(Color.class, colorParser);

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
    }

}
