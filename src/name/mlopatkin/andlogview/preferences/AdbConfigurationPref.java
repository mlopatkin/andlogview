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
import com.google.errorprone.annotations.concurrent.GuardedBy;

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
    static final String CLIENT_NAME = "adb";

    // Helper class to store inner configuration.
    private static class AdbConfiguration {
        final @Nullable String location;
        final boolean isAutoReconnectEnabled;
        final boolean shouldShowAutostartFailures;
        final boolean isAutoDiscoveryAllowed;

        @SuppressWarnings("deprecation")
        public AdbConfiguration() {
            // Default configuration values. Gson uses this constructor to create the instance, then overrides all
            // values with what is present in JSON data.
            this(
                    Configuration.adb.executable(),
                    true /* isAutoReconnectEnabled */,
                    true /* shouldShowAutostartFailures */,
                    // Disable auto discovery by default if the legacy configuration has location
                    Configuration.adb.executable() == null /* isAutoDiscoveryAllowed */
            );
        }

        public AdbConfiguration(
                @Nullable String location,
                boolean isAutoReconnectEnabled,
                boolean shouldShowAutostartFailures,
                boolean isAutoDiscoveryAllowed
        ) {
            this.location = location;
            this.isAutoReconnectEnabled = isAutoReconnectEnabled;
            this.shouldShowAutostartFailures = shouldShowAutostartFailures;
            this.isAutoDiscoveryAllowed = isAutoDiscoveryAllowed;
        }

        public AdbConfiguration withLocation(@Nullable String location) {
            return new AdbConfiguration(
                    location,
                    isAutoReconnectEnabled,
                    shouldShowAutostartFailures,
                    isAutoDiscoveryAllowed
            );
        }

        public AdbConfiguration withAutoReconnectEnabled(boolean isAutoReconnectEnabled) {
            if (isAutoReconnectEnabled == this.isAutoReconnectEnabled) {
                return this;
            }
            return new AdbConfiguration(
                    location,
                    isAutoReconnectEnabled,
                    shouldShowAutostartFailures,
                    isAutoDiscoveryAllowed
            );
        }

        public AdbConfiguration withShouldShowAutostartFailures(boolean shouldShowAutostartFailures) {
            if (shouldShowAutostartFailures == this.shouldShowAutostartFailures) {
                return this;
            }
            return new AdbConfiguration(
                    location,
                    isAutoReconnectEnabled,
                    shouldShowAutostartFailures,
                    isAutoDiscoveryAllowed
            );
        }

        public AdbConfiguration withAutoDiscoveryAllowed(boolean isAutoDiscoveryAllowed) {
            if (isAutoDiscoveryAllowed == this.isAutoDiscoveryAllowed) {
                return this;
            }
            return new AdbConfiguration(
                    location,
                    isAutoReconnectEnabled,
                    shouldShowAutostartFailures,
                    isAutoDiscoveryAllowed
            );
        }
    }

    private static final ConfigStorageClient<AdbConfiguration> STORAGE_CLIENT =
            new SimpleClient<>(CLIENT_NAME, AdbConfiguration.class, AdbConfiguration::new);

    private final Object lock = new Object();

    private final Preference<AdbConfiguration> preference;
    private final SystemPathResolver systemPathResolver;

    @GuardedBy("lock")
    private AdbConfiguration current;
    @GuardedBy("lock")
    private @Nullable File resolvedExecutable;

    @Inject
    public AdbConfigurationPref(ConfigStorage configStorage, SystemPathResolver systemPathResolver) {
        // This constructor should not write preferences to storage! Otherwise, invariants of the LegacyPrefsImport
        // go off!
        this.preference = configStorage.preference(STORAGE_CLIENT);
        this.systemPathResolver = systemPathResolver;
        this.current = preference.get();

        this.resolvedExecutable = resolveAdbLocation(getAdbLocation()).orElse(null);
    }

    /** @return the location of the ADB executable as set up by the user */
    public String getAdbLocation() {
        synchronized (lock) {
            return MoreObjects.firstNonNull(current.location, Configuration.adb.DEFAULT_EXECUTABLE);
        }
    }

    /**
     * Checks if the preference holds the valid ADB location
     *
     * @return true if the current ADB location is valid
     */
    public boolean hasValidAdbLocation() {
        synchronized (lock) {
            return resolvedExecutable != null;
        }
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
        return trySetAdbLocation(false, rawAdbLocation);
    }

    private boolean trySetAdbLocation(boolean autoDiscovery, String rawAdbLocation) {
        Optional<File> maybeResolved = resolveAdbLocation(rawAdbLocation);
        return maybeResolved.map(
                resolvedExecutable -> setResolvedAdbLocation(autoDiscovery, rawAdbLocation, resolvedExecutable)
        ).orElse(false);
    }

    private Optional<File> resolveAdbLocation(String rawAdbLocation) {
        try {
            return systemPathResolver.resolveExecutablePath(rawAdbLocation);
        } catch (InvalidPathException ex) {
            return Optional.empty();
        }
    }

    private boolean setResolvedAdbLocation(boolean autoDiscovery, String rawAdbLocation, File resolvedExecutable) {
        synchronized (lock) {
            if (!autoDiscovery || current.isAutoDiscoveryAllowed) {
                this.resolvedExecutable = resolvedExecutable;
                setConfiguration(
                        current.withLocation(rawAdbLocation)
                                .withAutoDiscoveryAllowed(current.isAutoDiscoveryAllowed && autoDiscovery)
                );
                return true;
            }
        }
        return false;
    }

    public Optional<File> getExecutable() {
        synchronized (lock) {
            return Optional.ofNullable(resolvedExecutable);
        }
    }

    public boolean isAutoReconnectEnabled() {
        synchronized (lock) {
            return current.isAutoReconnectEnabled;
        }
    }

    public void setAutoReconnectEnabled(boolean enabled) {
        synchronized (lock) {
            setConfiguration(current.withAutoReconnectEnabled(enabled));
        }
    }

    public boolean shouldShowAutostartFailures() {
        synchronized (lock) {
            return current.shouldShowAutostartFailures;
        }
    }

    public void setShowAdbAutostartFailures(boolean enabled) {
        synchronized (lock) {
            setConfiguration(current.withShouldShowAutostartFailures(enabled));
        }
    }

    @GuardedBy("lock")
    private void setConfiguration(AdbConfiguration newConfiguration) {
        // Do not grab the lock inside, otherwise you may end up with the newConfiguration being based on stale values
        // if other thread updates the current between this thread reading base value and setting the updated.
        // Doing reading and writing under the same hold avoids this.
        current = newConfiguration;
        preference.set(current);
    }

    /**
     * Returns true if ADB auto-discovery is allowed. In particular, there is no user-provided ADB location.
     *
     * @return {@code true} if the discovery is allowed
     */
    public boolean isAdbAutoDiscoveryAllowed() {
        synchronized (lock) {
            return current.isAutoDiscoveryAllowed;
        }
    }

    /**
     * Commits the auto-discovered location if it is valid and no location has been set yet.
     *
     * @param location the auto-discovered location
     */
    public boolean trySetAutoDiscoveredLocation(String location) {
        return trySetAdbLocation(true, location);
    }
}
