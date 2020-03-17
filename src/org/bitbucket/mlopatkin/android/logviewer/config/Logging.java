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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.bitbucket.mlopatkin.utils.properties.PropertyUtils;

/**
 * Logging configuration routines
 */
class Logging {
    private Logging() {
    }

    static void setUpDefault() {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout(
                PatternLayout.TTCC_CONVERSION_PATTERN), ConsoleAppender.SYSTEM_ERR));
    }

    static void loadNormal() {
        PropertyConfigurator.configure(PropertyUtils
                                               .getPropertiesFromResources("normal_log.properties"));
    }

    static void loadDebug() {
        PropertyConfigurator.configure(PropertyUtils
                                               .getPropertiesFromResources("debug_log.properties"));
    }
}
