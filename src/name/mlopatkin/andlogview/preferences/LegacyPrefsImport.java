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
import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.preferences.WindowsPositionsPref.Frame;
import name.mlopatkin.andlogview.ui.FrameLocation;

import dagger.Lazy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Migrates legacy preferences from {@code logview.properties} to new structure.
 */
public class LegacyPrefsImport {
    private static final Logger log = LoggerFactory.getLogger(LegacyPrefsImport.class);

    private final Lazy<ConfigStorage> storage;
    private final Lazy<WindowsPositionsPref> windowsPositionsPref;
    private final Lazy<AdbConfigurationPref> adbConfigurationPref;

    @Inject
    public LegacyPrefsImport(
            Lazy<ConfigStorage> storage,
            Lazy<WindowsPositionsPref> windowsPositions,
            Lazy<AdbConfigurationPref> adbConfiguration
    ) {
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
        // TODO(mlopatkin) test this somehow (make Configuration injectable when nothing accesses it anymore?)
        importProcessListPosition(windowsPositionsPref.get());

        // These might have been migrated.
        importAdbConfiguration(adbConfigurationPref.get());
    }

    @SuppressWarnings("deprecation")
    private void importProcessListPosition(WindowsPositionsPref windowsPositions) {
        var legacyPosition = Configuration.ui.processWindowPosition();
        if (legacyPosition != null) {
            log.debug("Importing ui.proc_window_pos = {}", legacyPosition);
            var defaultDimensions = windowsPositions.getFrameDimensions(Frame.PROCESS_LIST);
            windowsPositions.setFrameInfo(
                    Frame.PROCESS_LIST,
                    new FrameLocation(legacyPosition.x, legacyPosition.y),
                    defaultDimensions
            );
            Configuration.ui.clearProcessWindowPosition();
        }
    }

    @SuppressWarnings({"deprecation", "unused"})
    private void importAdbConfiguration(AdbConfigurationPref adbConfiguration) {
        var hasModernAdbPref = storage.get().hasStoredDataFor(AdbConfigurationPref.CLIENT_NAME);
        var legacyAutoReconnect = Configuration.adb.isAutoReconnectEnabled();
        if (legacyAutoReconnect != null && !hasModernAdbPref) {
            // 0.21 was the first version with the JSON pref, and it already had auto-reconnect.
            // Nightly builds do not count.
            adbConfiguration.setAutoReconnectEnabled(legacyAutoReconnect);
        }
    }
}
