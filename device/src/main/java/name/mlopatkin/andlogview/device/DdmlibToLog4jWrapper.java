/*
 * Copyright 2022 the Andlogview authors
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
package name.mlopatkin.andlogview.device;

import com.android.ddmlib.Log.ILogOutput;
import com.android.ddmlib.Log.LogLevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DdmlibToLog4jWrapper implements ILogOutput {
    private static final Logger logger = LoggerFactory.getLogger("DDMLIB");

    @Override
    public void printLog(LogLevel logLevel, String tag, String message) {
        String formattedMessage = formatLogString(tag, message);
        switch (logLevel) {
            case ASSERT, VERBOSE -> logger.trace(formattedMessage);
            case DEBUG -> logger.debug(formattedMessage);
            case INFO -> logger.info(formattedMessage);
            case WARN -> logger.warn(formattedMessage);
            case ERROR -> logger.error(formattedMessage);
        }
    }

    private String formatLogString(String tag, String message) {
        return String.format("%s: %s", tag, message);
    }

    @Override
    public void printAndPromptLog(LogLevel logLevel, String tag, String message) {
        printLog(logLevel, tag, message);
    }
}
