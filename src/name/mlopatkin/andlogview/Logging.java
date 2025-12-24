/*
 * Copyright 2011 the Andlogview authors
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
package name.mlopatkin.andlogview;

import name.mlopatkin.andlogview.utils.properties.PropertyUtils;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.LoggerFactory;

/**
 * Logging configuration routines.
 */
public class Logging {
    private Logging() {}

    /**
     * Simple initial configuration that logs to console, must be performed ASAP.
     */
    public static void initialConfiguration() {
        BasicConfigurator.configure(new ConsoleAppender(
                new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN), ConsoleAppender.SYSTEM_ERR));
    }

    /**
     * Sets up production logging configuration, with logs redirected to the file.
     */
    public static void useProductionLogging() {
        PropertyConfigurator.configure(PropertyUtils.getPropertiesFromResources(Main.class, "normal_log.properties"));
    }

    /**
     * Sets up debug logging configuration with logs printed on stderr and to the file.
     */
    public static void useDebugLogging() {
        PropertyConfigurator.configure(PropertyUtils.getPropertiesFromResources(Main.class, "debug_log.properties"));

        LoggerFactory.getLogger(Logging.class).info("Debug logging enabled");
    }
}
