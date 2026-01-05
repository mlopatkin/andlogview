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

import static com.google.common.collect.ImmutableMap.toImmutableMap;

import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.Preference;
import name.mlopatkin.andlogview.config.SimpleClient;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecord.Priority;
import name.mlopatkin.andlogview.preferences.WindowsPositionsPref.Frame;
import name.mlopatkin.andlogview.ui.FrameDimensions;
import name.mlopatkin.andlogview.ui.FrameLocation;
import name.mlopatkin.andlogview.ui.filters.BufferFilterModel;
import name.mlopatkin.andlogview.ui.themes.ThemeColors;
import name.mlopatkin.andlogview.ui.themes.ThemeColorsJson;
import name.mlopatkin.andlogview.ui.themes.ThemeColorsJson.LogTable;
import name.mlopatkin.andlogview.ui.themes.ThemeColorsJson.RowStyle;

import com.google.common.annotations.VisibleForTesting;

import dagger.Lazy;

import org.apache.commons.io.function.IOSupplier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Migrates legacy preferences from {@code logview.properties} to new structure.
 */
public class LegacyPrefsImport {
    private static final Logger log = LoggerFactory.getLogger(LegacyPrefsImport.class);

    private final ConfigStorage storage;
    private final Preference<Boolean> importPerformed;

    private final Lazy<WindowsPositionsPref> windowsPositionsPref;
    private final Lazy<AdbConfigurationPref> adbConfigurationPref;
    private final Lazy<ThemeColorsPref> themeColorsPref;

    @Inject
    public LegacyPrefsImport(
            ConfigStorage storage,
            Lazy<WindowsPositionsPref> windowsPositions,
            Lazy<AdbConfigurationPref> adbConfiguration,
            Lazy<ThemeColorsPref> themeColors
    ) {
        this.storage = storage;
        this.importPerformed = storage.preference(new SimpleClient<>("importPerformed", Boolean.class, () -> false));

        this.windowsPositionsPref = windowsPositions;
        this.adbConfigurationPref = adbConfiguration;
        this.themeColorsPref = themeColors;
    }

    public void importLegacyPreferences(IOSupplier<Optional<LegacyConfiguration>> legacyConfiguration) {
        if (importPerformed.get()) {
            log.info("Skipping import as it was already performed");
            return;
        }

        try {
            legacyConfiguration.get().ifPresentOrElse(
                    this::importLegacyPreferences,
                    () -> log.info("No legacy configuration found")
            );
        } catch (IOException e) {
            log.error("Failed to read legacy configuration. Import aborted.", e);
        } finally {
            importPerformed.set(true);
        }
    }

    @VisibleForTesting
    void importLegacyPreferences(LegacyConfiguration legacy) {
        log.info("Importing preferences from legacy configuration file");

        // Cases to consider
        // What if the legacy pref has some outdated value replaced by the modern preference?
        // We can expect that in majority of cases running migration, we'll have both legacy and modern preferences.

        // Let's start with preferences that were never integrated with the modern infrastructure before.
        importProcessListPosition(legacy, windowsPositionsPref.get());
        importBufferPrefs(legacy, BufferFilterModel.enabledBuffersPref(storage));
        importThemeColors(legacy, themeColorsPref.get());

        // These might have been migrated.
        importMainWindowPosition(legacy, windowsPositionsPref.get());
        importAdbConfiguration(legacy, adbConfigurationPref.get());
    }

    private void importProcessListPosition(LegacyConfiguration legacy, WindowsPositionsPref windowsPositions) {
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

    private void importBufferPrefs(LegacyConfiguration legacy, Preference<Set<LogRecord.Buffer>> bufferPref) {
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

    private void importThemeColors(LegacyConfiguration legacy, ThemeColorsPref themeColors) {
        themeColors.setOverride(legacyOverride(legacy, themeColors));
    }

    private ThemeColorsJson legacyOverride(LegacyConfiguration legacy, ThemeColorsPref themeColors) {
        var theme = themeColors.getBaseThemeColors();
        return new ThemeColorsJson(
                LogTable.create(
                        getIfDifferent(legacy.ui().backgroundColor(), theme.getBackgroundColor()),
                        new RowStyle(
                                getIfDifferent(legacy.ui().bookmarkBackground(), theme.getBookmarkBackgroundColor()),
                                getIfDifferent(legacy.ui().bookmarkedForeground(), theme.getBookmarkForegroundColor())
                        ),
                        importPriorityColorsMap(legacy, theme),
                        importHighlightColors(legacy, theme)
                )
        );
    }

    private @Nullable Color getIfDifferent(@Nullable Color overlayColor, Color baseColor) {
        return !Objects.equals(overlayColor, baseColor) ? overlayColor : null;
    }

    private @Nullable Map<Priority, RowStyle> importPriorityColorsMap(
            LegacyConfiguration legacy,
            ThemeColors baseTheme
    ) {
        // We only apply the colors if they're different from the color defined in the base theme. AndLogView used to
        // dump all colors into the user config file, so the presence of the value doesn't mean it is user-configured
        // per se.
        var colors = Arrays.stream(Priority.values())
                .filter(p -> {
                    var legacyColor = legacy.ui().priorityColor(p);
                    return legacyColor != null && !legacyColor.equals(baseTheme.getPriorityForegroundColor(p));
                })
                .collect(toImmutableMap(
                        Function.identity(),
                        p -> new RowStyle(null, legacy.ui().priorityColor(p))
                ));
        return !colors.isEmpty() ? colors : null;
    }

    private @Nullable List<RowStyle> importHighlightColors(LegacyConfiguration legacy, ThemeColors baseTheme) {
        // We only apply the colors if they're different from the color defined in the base theme. AndLogView used to
        // dump all colors into the user config file, so the presence of the value doesn't mean it is user-configured
        // per se.
        var legacyColors = legacy.ui().highlightColors();
        if (legacyColors == null || Objects.equals(legacyColors, baseTheme.getHighlightColors())) {
            // Nullable collection is meh, but we need to tell the theming code that there is no user-provided overlay.
            return null;
        }
        return legacyColors.stream().map(c -> new RowStyle(c, null)).toList();
    }

    private void importMainWindowPosition(LegacyConfiguration legacy, WindowsPositionsPref windowsPositionsPref) {
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

    private void importAdbConfiguration(LegacyConfiguration legacy, AdbConfigurationPref adbConfiguration) {
        if (storage.hasStoredDataFor(AdbConfigurationPref.CLIENT_NAME)) {
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
