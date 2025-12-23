/*
 * Copyright 2025 the Andlogview authors
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

import name.mlopatkin.andlogview.thirdparty.systemutils.SystemUtils;

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;

/**
 * Resolves the location of the configuration file. By default, on Windows the configuration is stored in
 * {@code %APPDATA%/logview/} and on Linux and macOS in {@code $HOME/.logview/}.
 */
public class ConfigurationLocation {

    @Inject
    public ConfigurationLocation() {}

    /**
     * Returns the configuration directory.
     *
     * @return the configuration directory
     */
    public File getConfigurationDir() {
        String appConfigDirName = SystemUtils.IS_OS_WINDOWS ? "logview" : ".logview";
        return new File(getSystemConfigDir(), appConfigDirName);
    }

    /**
     * Returns the legacy configuration file, in Properties format.
     *
     * @return the legacy file
     */
    public File getLegacyConfigurationFile() {
        return new File(getConfigurationDir(), "logview.properties");
    }

    /**
     * Returns the current configuration file, in JSON format.
     *
     * @return the current file
     */
    public File getConfigurationFile() {
        return new File(getConfigurationDir(), "logview.json");
    }

    private static File getSystemConfigDir() {
        String systemConfigDirPath = SystemUtils.IS_OS_WINDOWS ? System.getenv("APPDATA") : SystemUtils.USER_HOME;
        return new File(Objects.requireNonNull(systemConfigDirPath, "Can't find user home"));
    }
}
