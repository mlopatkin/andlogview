/*
 * Copyright 2021 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.preferences;

import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.device.AdbLocation;
import name.mlopatkin.andlogview.utils.SystemPathResolver;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A collection of the ADB-related preferences.
 */
@Singleton
public class AdbConfigurationPref implements AdbLocation {
    // Helper class to store inner configuration.
    private static class AdbConfiguration {
        final String location;

        public AdbConfiguration(String location) {
            this.location = location;
        }
    }

    private static final ConfigStorage.ConfigStorageClient<AdbConfiguration> STORAGE_CLIENT =
            new ConfigStorage.ConfigStorageClient<AdbConfiguration>() {
                @Override
                public String getName() {
                    return "adb";
                }

                @Override
                public AdbConfiguration fromJson(Gson gson, JsonElement element) {
                    return gson.fromJson(element, AdbConfiguration.class);
                }

                @Override
                @SuppressWarnings("deprecation")
                public AdbConfiguration getDefault() {
                    return new AdbConfiguration(
                            MoreObjects.firstNonNull(
                                    Configuration.adb.executable(),
                                    Configuration.adb.DEFAULT_EXECUTABLE));
                }

                @Override
                public JsonElement toJson(Gson gson, AdbConfiguration value) {
                    return gson.toJsonTree(value);
                }
            };

    private final ConfigStorage configStorage;

    private String rawAdbLocation;
    @Nullable
    private File resolvedExecutable;

    @Inject
    AdbConfigurationPref(ConfigStorage configStorage) {
        this.configStorage = configStorage;
        setRawAdbLocation(configStorage.loadConfig(STORAGE_CLIENT).location);
    }

    /** @return the location of the ADB executable as set up by the user */
    public String getAdbLocation() {
        return rawAdbLocation;
    }

    /**
     * Stores the ADB location as selected by the user. The specified location might be invalid.
     *
     * @param rawAdbLocation the path to ADB or naked executable name
     */
    public void setAdbLocation(String rawAdbLocation) {
        setRawAdbLocation(rawAdbLocation);
        configStorage.saveConfig(STORAGE_CLIENT, new AdbConfiguration(rawAdbLocation));
    }

    /**
     * Tries to update the ADB location. Aborts the update if the executable cannot be resolved in the new location and
     * returns {@code false}.
     *
     * @param rawAdbLocation the new adb location
     * @return {@code true} if the update was successful or {@code false} if the new location is invalid.
     */
    public boolean trySetAdbLocation(String rawAdbLocation) {
        Optional<File> maybeResolved = SystemPathResolver.resolveExecutablePath(rawAdbLocation);
        maybeResolved.ifPresent(resolvedExecutable -> {
            this.rawAdbLocation = rawAdbLocation;
            this.resolvedExecutable = resolvedExecutable;
        });
        return maybeResolved.isPresent();
    }

    private void setRawAdbLocation(String rawAdbLocation) {
        this.rawAdbLocation = rawAdbLocation;
        this.resolvedExecutable = SystemPathResolver.resolveExecutablePath(rawAdbLocation).orElse(null);
    }

    @Override
    public boolean isValidExecutable() {
        return resolvedExecutable != null;
    }

    @Override
    public File getExecutable() {
        File resolvedExecutable = this.resolvedExecutable;
        Preconditions.checkState(resolvedExecutable != null, "Cannot resolve ADB executable from {}", rawAdbLocation);
        return resolvedExecutable;
    }
}
