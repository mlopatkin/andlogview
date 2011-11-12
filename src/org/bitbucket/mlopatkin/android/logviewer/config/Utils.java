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

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.utils.properties.Configuration;
import org.bitbucket.mlopatkin.utils.properties.Parser;
import org.bitbucket.mlopatkin.utils.properties.PropertyUtils;

class Utils {
    private static final Logger logger = Logger
            .getLogger(org.bitbucket.mlopatkin.android.logviewer.config.Configuration.class);

    private Utils() {
    }

    static final Parser<Color> colorParser = new Parser<Color>() {

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

    private static final String CONFIG_FILE_NAME = "logview.properties";
    private static final String CONFIG_APP_NAME = "logview";

    static final void saveConfiguration(Configuration cfg) {
        File cfgDir = PropertyUtils.getAppConfigDir(CONFIG_APP_NAME);
        if (!cfgDir.exists()) {
            cfgDir.mkdirs();
        }
        File cfgFile = new File(cfgDir, CONFIG_FILE_NAME);
        try {
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(cfgFile));
            try {
                cfg.save(output);
            } finally {
                output.close();
            }
        } catch (IOException e) {
            logger.error("Failed to save configuration file", e);
        }
    }
}
