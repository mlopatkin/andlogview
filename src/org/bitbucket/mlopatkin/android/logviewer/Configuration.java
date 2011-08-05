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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;

public class Configuration {

    private Properties properties = new Properties();

    private static final String CONFIG_FILE_NAME = "logview.properties";

    private void loadFromFile() {
        try {
            InputStream in = getClass().getResourceAsStream("/logview.properties");
            File configFile = new File(CONFIG_FILE_NAME);
            if (configFile.exists()) {
                in.close();
                in = new BufferedInputStream(new FileInputStream(CONFIG_FILE_NAME));
            }
            try {
                properties.load(in);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Configuration() {
        loadFromFile();
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
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static class ui {
        private static final String PREFIX = "ui.";
        private static List<String> columns_;

        private static void initColumns() {
            String columnsValue = instance.properties.getProperty(PREFIX + "columns",
                    "time, pid, priority, tag, message");
            columns_ = splitCommaSeparatedValues(columnsValue);
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

        public static Color highlightColor() {
            Color defaultColor = Color.decode("#D0F0C0");
            return parseColor(PREFIX + "highlight_color", defaultColor);
        }

        public static Color backgroundColor() {
            return parseColor(PREFIX + "background_color", Color.WHITE);
        }
    }

    public static class adb {
        private static final String PREFIX = "adb.";

        public static String commandline() {
            return instance.properties.getProperty(PREFIX + "commandline",
                    "adb logcat -v threadtime");
        }

        public static String bufferswitch() {
            return instance.properties.getProperty(PREFIX + "bufferswitch", "-b");
        }

        public static List<String> buffers() {
            String buffersValue = instance.properties.getProperty(PREFIX + "buffers",
                    "system, main");
            return splitCommaSeparatedValues(buffersValue);
        }
    }

    public static class dump {
        private static final String PREFIX = "dump.";

        private static List<String> buffers_;

        private static void initBuffers() {
            String columnsValue = instance.properties.getProperty(PREFIX + "buffers",
                    "main, event, radio");
            buffers_ = splitCommaSeparatedValues(columnsValue);
        }

        public static boolean bufferEnabled(String bufferName) {
            bufferName = bufferName.toLowerCase();
            if (buffers_ == null) {
                initBuffers();
            }
            return buffers_.contains(bufferName);
        }
    }
}
