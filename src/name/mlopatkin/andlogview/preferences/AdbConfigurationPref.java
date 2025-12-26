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
import name.mlopatkin.andlogview.config.ConfigStorageClient;
import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.config.Preference;
import name.mlopatkin.andlogview.config.SimpleClient;
import name.mlopatkin.andlogview.utils.SystemPathResolver;

import com.google.common.base.MoreObjects;

import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A collection of the ADB-related preferences.
 */
@Singleton
public class AdbConfigurationPref {
    // Helper class to store inner configuration.
    private static class AdbConfiguration {
        final @Nullable String location;
        final boolean isAutoReconnectEnabled;
        final boolean shouldShowAutostartFailures;

        @SuppressWarnings("deprecation")
        public AdbConfiguration() {
            // Default configuration values. Gson uses this constructor to create the instance, then overrides all
            // values with what is present in JSON data.
            this(Configuration.adb.executable(), Configuration.adb.isAutoReconnectEnabled(), true);
        }

        public AdbConfiguration(
                @Nullable String location,
                boolean isAutoReconnectEnabled,
                boolean shouldShowAutostartFailures
        ) {
            this.location = location;
            this.isAutoReconnectEnabled = isAutoReconnectEnabled;
            this.shouldShowAutostartFailures = shouldShowAutostartFailures;
        }

        public AdbConfiguration withLocation(@Nullable String location) {
            return new AdbConfiguration(location, isAutoReconnectEnabled, shouldShowAutostartFailures);
        }

        public AdbConfiguration withAutoReconnectEnabled(boolean autoReconnectEnabled) {
            return new AdbConfiguration(location, autoReconnectEnabled, shouldShowAutostartFailures);
        }

        public AdbConfiguration withShouldShowAutostartFailures(boolean shouldShowAutostartFailures) {
            return new AdbConfiguration(location, isAutoReconnectEnabled, shouldShowAutostartFailures);
        }
    }

    private static final ConfigStorageClient<AdbConfiguration> STORAGE_CLIENT =
            new SimpleClient<>("adb", AdbConfiguration.class, AdbConfiguration::new);

    private final Preference<AdbConfiguration> preference;
    private final SystemPathResolver systemPathResolver;

    private AdbConfiguration current;
    private @Nullable File resolvedExecutable;

    @Inject
    public AdbConfigurationPref(ConfigStorage configStorage, SystemPathResolver systemPathResolver) {
        this.preference = configStorage.preference(STORAGE_CLIENT);
        this.systemPathResolver = systemPathResolver;
        this.current = preference.get();

        this.resolvedExecutable = resolveAdbLocation(getAdbLocation()).orElse(null);
    }

    /** @return the location of the ADB executable as set up by the user */
    public String getAdbLocation() {
        return MoreObjects.firstNonNull(current.location, Configuration.adb.DEFAULT_EXECUTABLE);
    }

    /**
     * Checks if the preference holds the valid ADB location
     *
     * @return true if the current ADB location is valid
     */
    public boolean hasValidAdbLocation() {
        return resolvedExecutable != null;
    }

    /**
     * Checks the ADB location for validity.
     *
     * @param rawAdbLocation the path to ADB or naked executable name
     * @return {@code true} if the ADB location is valid
     */
    public boolean checkAdbLocation(String rawAdbLocation) {
        return resolveAdbLocation(rawAdbLocation).isPresent();
    }

    /**
     * Tries to update the ADB location. Aborts the update if the executable cannot be resolved in the new location and
     * returns {@code false}.
     *
     * @param rawAdbLocation the new adb location
     * @return {@code true} if the update was successful or {@code false} if the new location is invalid.
     */
    public boolean trySetAdbLocation(String rawAdbLocation) {
        Optional<File> maybeResolved = resolveAdbLocation(rawAdbLocation);
        maybeResolved.ifPresent(resolvedExecutable -> setResolvedAdbLocation(rawAdbLocation, resolvedExecutable));
        return maybeResolved.isPresent();
    }

    private Optional<File> resolveAdbLocation(String rawAdbLocation) {
        try {
            return systemPathResolver.resolveExecutablePath(rawAdbLocation);
        } catch (InvalidPathException ex) {
            return Optional.empty();
        }
    }

    private void setResolvedAdbLocation(String rawAdbLocation, File resolvedExecutable) {
        this.resolvedExecutable = resolvedExecutable;
        setConfiguration(current.withLocation(rawAdbLocation));
    }

    public Optional<File> getExecutable() {
        return Optional.ofNullable(resolvedExecutable);
    }

    public boolean isAutoReconnectEnabled() {
        return current.isAutoReconnectEnabled;
    }

    public void setAutoReconnectEnabled(boolean enabled) {
        setConfiguration(current.withAutoReconnectEnabled(enabled));
    }

    public boolean shouldShowAutostartFailures() {
        return current.shouldShowAutostartFailures;
    }

    public void setShowAdbAutostartFailures(boolean enabled) {
        setConfiguration(current.withShouldShowAutostartFailures(enabled));
    }

    private void setConfiguration(AdbConfiguration newConfiguration) {
        current = newConfiguration;
        preference.set(current);
    }
}
