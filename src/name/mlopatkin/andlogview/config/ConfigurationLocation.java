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

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;

/**
 * Resolves the location of the configuration file. By default, on Windows the configuration is stored in
 * {@code %APPDATA%/logview/} and on Linux and macOS in {@code $HOME/.logview/}.
 */
public class ConfigurationLocation {
    // TODO(mlopatkin): we're relying on environment variables and the likes to fetch the configuration and assume that
    //  the configuration directories are writable. This may not always be the case, but the app can still function,
    //  at least with some degradation.

    // Name of the system property that overrides the default configuration directory
    private static final String CONFIG_DIR_OVERRIDE_KEY = "name.mlopatkin.andlogview.config.dir";
    private final File configurationDir;
    private final File localConfigurationDir;

    @Inject
    public ConfigurationLocation() {
        var override = System.getProperty(CONFIG_DIR_OVERRIDE_KEY);
        configurationDir = override != null ? new File(override) : getDefaultConfigurationDir();
        localConfigurationDir = override != null ? configurationDir : getDefaultLocalConfigurationDir();
    }

    /**
     * Returns the configuration directory.
     *
     * @return the configuration directory
     */
    public File getConfigurationDir() {
        return configurationDir;
    }

    /**
     * Returns the local configuration directory. On Windows it is typically {@code AppData/Local}.
     *
     * @return the local configuration directory.
     */
    public File getLocalConfigurationDir() {
        return localConfigurationDir;
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

    private static String getAppConfigDirName() {
        return SystemUtils.IS_OS_WINDOWS ? "logview" : ".logview";
    }

    private static File getDefaultConfigurationDir() {
        return new File(getSystemConfigDir(), getAppConfigDirName());
    }

    private static File getDefaultLocalConfigurationDir() {
        return new File(getSystemLocalConfigDir(), getAppConfigDirName());
    }

    private static File getSystemConfigDir() {
        String systemConfigDirPath = SystemUtils.IS_OS_WINDOWS ? System.getenv("APPDATA") : SystemUtils.USER_HOME;
        // TODO(mlopatkin) What if it is null?
        return new File(Objects.requireNonNull(systemConfigDirPath, "Can't find user home"));
    }

    private static File getSystemLocalConfigDir() {
        if (!SystemUtils.IS_OS_WINDOWS) {
            // This isn't XDG-compliant, but who cares :)
            return getSystemConfigDir();
        }
        // Windows is tricky. Since Windows Vista, the local configuration lives in C:\Users\<username>\AppData\Local
        // and is available through LOCALAPPDATA env var.
        // On XP and 2K it lives in C:\Documents and Settings\<username>\Local Settings\Application data
        // and there is no env var to fetch it.
        // See: https://web.archive.org/web/20251002193525/https://learn.microsoft.com/en-us/previous-versions/ms995853(v=msdn.10)?redirectedfrom=MSDN#using-application-data-folders
        // for pre-Windows Vista definitions.
        var localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null) {
            return new File(localAppData);
        }

        if (SystemUtils.IS_OS_WINDOWS_XP || SystemUtils.IS_OS_WINDOWS_2000 || SystemUtils.IS_OS_WINDOWS_2003) {
            return new File(SystemUtils.getUserHome(), "Local Settings/Application data");
        }

        // Fallback on weird Vista+ that doesn't have the env var set.
        return getSystemConfigDir();
    }
}
