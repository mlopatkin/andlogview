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

package name.mlopatkin.andlogview.preferences;

import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.LegacyConfiguration;
import name.mlopatkin.andlogview.config.Preference;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.preferences.WindowsPositionsPref.Frame;
import name.mlopatkin.andlogview.ui.FrameDimensions;
import name.mlopatkin.andlogview.ui.FrameLocation;
import name.mlopatkin.andlogview.ui.filters.BufferFilterModel;

import dagger.Lazy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Migrates legacy preferences from {@code logview.properties} to new structure.
 */
public class LegacyPrefsImport {
    private static final Logger log = LoggerFactory.getLogger(LegacyPrefsImport.class);

    private final LegacyConfiguration legacy;
    private final Lazy<ConfigStorage> storage;
    private final Lazy<WindowsPositionsPref> windowsPositionsPref;
    private final Lazy<AdbConfigurationPref> adbConfigurationPref;

    @Inject
    public LegacyPrefsImport(
            LegacyConfiguration legacy,
            Lazy<ConfigStorage> storage,
            Lazy<WindowsPositionsPref> windowsPositions,
            Lazy<AdbConfigurationPref> adbConfiguration
    ) {
        this.legacy = legacy;
        this.storage = storage;
        this.windowsPositionsPref = windowsPositions;
        this.adbConfigurationPref = adbConfiguration;
    }

    public void importLegacyPreferences() {
        log.info("Importing preferences from legacy configuration file");

        // Cases to consider
        // What if the legacy pref has some outdated value replaced by the modern preference?
        // We can expect that in majority of cases running migration, we'll have both legacy and modern preferences.

        // Let's start with preferences that were never integrated with the modern infrastructure before.
        importProcessListPosition(windowsPositionsPref.get());
        importBufferPrefs(BufferFilterModel.enabledBuffersPref(storage.get()));

        // These might have been migrated.
        importMainWindowPosition(windowsPositionsPref.get());
        importAdbConfiguration(adbConfigurationPref.get());
    }

    private void importProcessListPosition(WindowsPositionsPref windowsPositions) {
        if (windowsPositions.getFrameLocation(Frame.PROCESS_LIST) != null) {
            log.info("Skip importing ui.proc_window_pos because it is already in the modern config.");
            return;
        }
        var legacyPosition = legacy.ui().processWindowPosition();
        if (legacyPosition != null) {
            log.info("Importing ui.proc_window_pos = {}", legacyPosition);
            var defaultDimensions = windowsPositions.getFrameDimensions(Frame.PROCESS_LIST);
            windowsPositions.setFrameInfo(
                    Frame.PROCESS_LIST,
                    new FrameLocation(legacyPosition.x, legacyPosition.y),
                    defaultDimensions
            );
        }
    }

    private void importBufferPrefs(Preference<Set<LogRecord.Buffer>> bufferPref) {
        if (bufferPref.isSet()) {
            log.info("Skip importing Buffer visibility preferences because they're already in the modern config.");
            return;
        }
        boolean hasAnyBuffers = false;
        Set<LogRecord.Buffer> legacyEnabledBuffers = new HashSet<>();

        for (var buffer : LogRecord.Buffer.values()) {
            var enabled = legacy.ui().bufferEnabled(buffer);
            if (enabled != null) {
                hasAnyBuffers = true;
                if (enabled) {
                    legacyEnabledBuffers.add(buffer);
                }
            }
        }

        if (hasAnyBuffers) {
            log.info("Importing ui.bufferEnabled.* for {}", legacyEnabledBuffers.stream().map(Enum::name).collect(
                    Collectors.joining(", ")));
            bufferPref.set(legacyEnabledBuffers);
        }
    }

    private void importMainWindowPosition(WindowsPositionsPref windowsPositionsPref) {
        var currentLocation = windowsPositionsPref.getFrameLocation(Frame.MAIN);
        // By default, with empty config, the location is not defined. If it is there, then we have already imported it
        // through old code or run the app and saved the location that way. It is impossible to run the app without
        // storing the location.
        if (currentLocation != null) {
            log.info("Skip importing ui.main_window_* because the values are already in the modern config.");
            return;
        }

        var legacyLocation = legacy.ui().mainWindowPosition();
        var legacyWidth = legacy.ui().mainWindowWidth();
        var legacyHeight = legacy.ui().mainWindowHeight();

        log.info("Importing ui.main_window_location = {}", legacyLocation);
        log.info("Importing ui.main_window_width = {}", legacyWidth);
        log.info("Importing ui.main_window_height = {}", legacyHeight);

        var importedLocation = legacyLocation != null ? new FrameLocation(legacyLocation.x, legacyLocation.y) : null;
        var defaultDimensions = windowsPositionsPref.getFrameDimensions(Frame.MAIN);
        var importedDimensions = new FrameDimensions(
                legacyWidth != null && legacyWidth > 0 ? legacyWidth : defaultDimensions.width(),
                legacyHeight != null && legacyHeight > 0 ? legacyHeight : defaultDimensions.height());

        windowsPositionsPref.setFrameInfo(Frame.MAIN, importedLocation, importedDimensions);
    }

    private void importAdbConfiguration(AdbConfigurationPref adbConfiguration) {
        if (storage.get().hasStoredDataFor(AdbConfigurationPref.CLIENT_NAME)) {
            log.info("Skip importing ADB preferences because they're already in the modern config.");
            return;
        }

        var legacyExecutable = legacy.adb().executable();
        if (legacyExecutable != null) {
            log.info("Importing adb.executable = {}", legacyExecutable);
            // Modern executable was in JSON from the very first commit.
            // If the new location isn't resolvable, we just drop it and rely on auto-detection instead.
            if (!adbConfiguration.trySetAdbLocation(legacyExecutable)) {
                log.info("Discarding invalid adb.executable at {}", legacyExecutable);
            }
        }
        var legacyAutoReconnect = legacy.adb().isAutoReconnectEnabled();
        if (legacyAutoReconnect != null) {
            log.info("Importing adb.autoreconnect = {}", legacyAutoReconnect);
            // 0.21 was the first version with the JSON pref, and it already had auto-reconnect.
            // Nightly builds do not count.
            adbConfiguration.setAutoReconnectEnabled(legacyAutoReconnect);
        }
    }
}
