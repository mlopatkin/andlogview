/*
 * Copyright 2018 Mikhail Lopatkin
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

import static name.mlopatkin.andlogview.AppExecutors.FILE_EXECUTOR;
import static name.mlopatkin.andlogview.AppExecutors.UI_EXECUTOR;

import name.mlopatkin.andlogview.base.concurrent.SequentialExecutor;
import name.mlopatkin.andlogview.config.ConfigModule;
import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.ConfigurationLocation;
import name.mlopatkin.andlogview.device.AdbManager;
import name.mlopatkin.andlogview.features.Features;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.preferences.LastUsedDirPref;
import name.mlopatkin.andlogview.preferences.LegacyPrefsImport;
import name.mlopatkin.andlogview.preferences.WindowsPositionsPref;
import name.mlopatkin.andlogview.ui.GlobalClipboard;
import name.mlopatkin.andlogview.ui.SwingUiModule;
import name.mlopatkin.andlogview.ui.filters.FilterGlobals;
import name.mlopatkin.andlogview.ui.themes.Theme;
import name.mlopatkin.andlogview.ui.themes.ThemeColors;
import name.mlopatkin.andlogview.utils.UiThreadScheduler;

import dagger.BindsInstance;
import dagger.Component;

import java.util.concurrent.Executor;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Global application services available for all.
 */
@Singleton
@Component(modules = {
        AppExecutors.ExecutorsModule.class,
        ConfigModule.class,
        DeviceModule.class,
        GlobalsModule.class,
        SwingUiModule.class,
        FilterGlobals.class,
})
public interface AppGlobals {
    Main getMain();

    ConfigStorage getConfigStorage();

    @Named(UI_EXECUTOR)
    Executor getUiExecutor();

    @Named(UI_EXECUTOR)
    SequentialExecutor getUiSequentialExecutor();

    @Named(FILE_EXECUTOR)
    Executor getFileExecutor();

    UiThreadScheduler getUiTimer();

    GlobalClipboard getClipboard();

    AdbConfigurationPref getAdbConfiguration();

    LastUsedDirPref getLastUsedDir();

    WindowsPositionsPref getWindowsPositions();

    AdbManager getAdbManager();

    Theme getTheme();

    ThemeColors getThemeColors();

    Features getFeatures();

    LegacyPrefsImport getPreferenceImporter();

    @Component.Factory
    interface Factory {
        AppGlobals create(
                @BindsInstance ConfigurationLocation configurationLocation,
                @BindsInstance CommandLine cmdline,
                @BindsInstance Theme selectedTheme);
    }
}
