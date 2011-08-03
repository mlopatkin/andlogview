package org.bitbucket.mlopatkin.android.logviewer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class Configuration {

    private Properties properties = new Properties();

    private static final String CONFIG_FILE_NAME = "logview.properties";

    private void loadFromFile() {
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(CONFIG_FILE_NAME));
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
}
