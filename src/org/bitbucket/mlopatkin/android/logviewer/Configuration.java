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
package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;
import org.bitbucket.mlopatkin.utils.MyStringUtils;

public class Configuration {
    private static final boolean DEBUG_MODE = System.getProperty("logview.debug") != null;

    public static class ui {
        private static final String PREFIX = "ui.";
        private static List<String> columns_;
        private static EnumSet<Buffer> buffers_;

        private static void initColumns() {
            String columnsValue = instance.properties.getProperty(PREFIX + "columns",
                    "time, pid, priority, tag, message");
            columns_ = splitCommaSeparatedValues(columnsValue);
        }

        private static void initBuffers() {
            buffers_ = EnumSet.noneOf(Buffer.class);
            String columnsValue = instance.properties.getProperty(PREFIX + "buffers", "MAIN");
            for (String bufferName : splitCommaSeparatedValues(columnsValue)) {
                buffers_.add(Buffer.valueOf(bufferName.toUpperCase()));
            }
        }

        public synchronized static List<String> columns() {
            if (columns_ == null) {
                initColumns();
            }
            return columns_;
        }

        public static int tooltipMaxWidth() {
            return parseInt(PREFIX + "tooltip_max_width", 120);
        }

        public static int autoscrollThreshold() {
            return parseInt(PREFIX + "autoscroll_threshold", 20);
        }

        public static Color priorityColor(Priority p) {
            String priorityName = p.name().toLowerCase();
            return parseColor(PREFIX + "priority_color." + priorityName, Color.BLACK);
        }

        public static Color bookmarkBackground() {
            Color defaultColor = Color.decode("#D0F0C0");
            return parseColor(PREFIX + "bookmark_background", defaultColor);
        }

        public static Color bookmarkedForeground() {
            return parseColor(PREFIX + "bookmark_foreground", null);
        }

        private static void initHighlightColors() {
            Color defaultColor = Color.decode("#D0F0C0");
            String prefix = PREFIX + "highlight_color.";
            TreeMap<Integer, Color> colors = new TreeMap<Integer, Color>();
            for (String param : instance.properties.stringPropertyNames()) {
                if (param.startsWith(prefix)) {
                    int id = Integer.parseInt(param.substring(prefix.length()));
                    colors.put(id, parseColor(param, defaultColor));
                }
            }
            _highlightColors = new Color[colors.size()];
            int i = 0;
            for (Color color : colors.values()) {
                _highlightColors[i++] = color;
            }
        }

        private static Color[] _highlightColors;

        public static Color[] highlightColors() {
            if (_highlightColors == null) {
                initHighlightColors();
            }
            return _highlightColors;
        }

        public static Color backgroundColor() {
            return parseColor(PREFIX + "background_color", Color.WHITE);
        }

        public static boolean bufferEnabled(Buffer buffer) {
            if (buffers_ == null) {
                initBuffers();
            }
            return buffers_.contains(buffer);
        }
    }

    public static class adb {
        private static final String PREFIX = "adb.";
        public static final String DEFAULT_EXECUTABLE = ((SystemUtils.IS_OS_WINDOWS) ? "adb.exe"
                : "adb").intern();

        public static String commandline() {
            return instance.properties.getProperty(PREFIX + "commandline", "logcat -v threadtime");
        }

        public static String bufferswitch() {
            return instance.properties.getProperty(PREFIX + "bufferswitch", "-b");
        }

        public static String bufferName(Buffer buffer) {
            return instance.properties.getProperty(PREFIX + "buffer." + buffer.toString());
        }

        public static String psCommandLine() {
            return "ps -P";
        }

        public static String executable() {
            return instance.properties.getProperty(PREFIX + "executable", DEFAULT_EXECUTABLE);
        }

        public static void executable(String newExecutable) {
            instance.properties.setProperty(PREFIX + "executable", newExecutable);
        }

        public static boolean showSetupDialog() {
            return parseBoolean(PREFIX + "show_setup_dialog", true);
        }

        public static void showSetupDialog(boolean value) {
            instance.properties.setProperty(PREFIX + "show_setup_dialog",
                    BooleanUtils.toStringTrueFalse(value));
        }
    }

    public static class dump {
        private static final String PREFIX = "dump.";

        public static String bufferHeader(Buffer buffer) {
            return instance.properties.getProperty(PREFIX + "buffer." + buffer.toString());
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

    private static List<String> splitCommaSeparatedValues(String valuesString) {
        String[] values = StringUtils.split(valuesString, ",");
        List<String> result = new ArrayList<String>();
        for (String s : values) {
            result.add(s.toLowerCase().trim());
        }
        return Collections.unmodifiableList(result);
    }

    private static int parseInt(String key, int defaultValue) {
        String widthValue = instance.properties.getProperty(key);
        if (widthValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(widthValue.trim());
        } catch (NumberFormatException e) {
            logger.warn("Incorrect number in " + key, e);
            return defaultValue;
        }
    }

    private static boolean parseBoolean(String key, boolean defaultValue) {
        String boolValue = instance.properties.getProperty(key);
        if (boolValue != null) {
            return BooleanUtils.toBoolean(boolValue);
        } else {
            return defaultValue;
        }
    }

    private static Color parseColor(String key, Color defaultValue) {
        String colorValue = instance.properties.getProperty(key);
        if (colorValue == null) {
            return defaultValue;
        }
        try {
            return Color.decode(colorValue);
        } catch (NumberFormatException e) {
            logger.warn("Incorrect color format in " + key, e);
            return defaultValue;
        }
    }

    static void forceInit() {
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
            Writer writer = new FileWriter(file);
            try {
                properties
                        .store(writer,
                                "Don't edit this file while application is running or your changes will be lost\n");
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            logger.error("Cannot save properties", e);
        }
    }

    public static void save() {
        instance.saveToFile();
    }
}
