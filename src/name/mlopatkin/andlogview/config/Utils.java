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
package name.mlopatkin.andlogview.config;

import name.mlopatkin.andlogview.Main;
import name.mlopatkin.andlogview.utils.properties.IllegalConfigurationException;
import name.mlopatkin.andlogview.utils.properties.Parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class Utils {
    @SuppressWarnings("LoggerInitializedWithForeignClass")
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private Utils() {}

    static final Parser<Color> colorParser = new Parser<>() {
        @Override
        public Color read(String value) {
            return Color.decode(value);
        }

        @Override
        public String write(@SuppressWarnings("NullableProblems") Color value) {
            return String.format("#%02x%02x%02x", value.getRed(), value.getGreen(), value.getBlue());
        }
    };

    private static final String CONFIG_FILE_NAME = "logview.properties";

    static void saveConfiguration(name.mlopatkin.andlogview.utils.properties.Configuration cfg) {
        try {
            File cfgDir = Main.getConfigurationDir();
            if (!cfgDir.exists()) {
                Files.createDirectories(cfgDir.toPath());
            }
            File cfgFile = new File(cfgDir, CONFIG_FILE_NAME);
            try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(cfgFile))) {
                cfg.save(output, "Do not modify while Logviewer is running");
            }
        } catch (IOException e) {
            logger.error("Failed to save configuration file", e);
        }
    }

    public static void loadConfiguration(name.mlopatkin.andlogview.utils.properties.Configuration config)
            throws IllegalConfigurationException {
        File cfgDir = Main.getConfigurationDir();
        if (!cfgDir.exists()) {
            return;
        }
        File cfgFile = new File(cfgDir, CONFIG_FILE_NAME);
        if (!cfgFile.exists()) {
            return;
        }

        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(cfgFile))) {
            config.load(input);
        } catch (IOException e) {
            logger.error("Failed to load configuration file", e);
        }
    }

    public static Gson createConfigurationGson() {
        return new GsonBuilder().registerTypeAdapter(Color.class, new ColorTypeAdapter()).create();
    }
}
