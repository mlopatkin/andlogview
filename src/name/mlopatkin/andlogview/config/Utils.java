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

import org.apache.log4j.Logger;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

class Utils {
    private static final Logger logger = Logger.getLogger(Configuration.class);

    private Utils() {}

    static final Parser<Color> colorParser = new Parser<Color>() {
        @Override
        public Color read(String value) {
            return Color.decode(value);
        }

        @Override
        public String write(Color value) {
            return String.format("#%02x%02x%02x", value.getRed(), value.getGreen(), value.getBlue());
        }
    };

    private static final String CONFIG_FILE_NAME = "logview.properties";

    static final void saveConfiguration(name.mlopatkin.andlogview.utils.properties.Configuration cfg) {
        File cfgDir = Main.getConfigurationDir();
        if (!cfgDir.exists()) {
            cfgDir.mkdirs();
        }
        File cfgFile = new File(cfgDir, CONFIG_FILE_NAME);
        try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(cfgFile))) {
            cfg.save(output, "Do not modify while Logviewer is running");
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
}
