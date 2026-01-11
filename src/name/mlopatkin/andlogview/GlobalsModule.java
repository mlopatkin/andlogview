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

import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.ConfigurationLocation;
import name.mlopatkin.andlogview.preferences.ThemePref;
import name.mlopatkin.andlogview.ui.themes.CurrentTheme;
import name.mlopatkin.andlogview.ui.themes.ThemeColors;
import name.mlopatkin.andlogview.ui.themes.ThemeException;
import name.mlopatkin.andlogview.utils.SystemPathResolver;

import dagger.Module;
import dagger.Provides;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.inject.Singleton;

@Module
public abstract class GlobalsModule {
    private static final Logger logger = LoggerFactory.getLogger(GlobalsModule.class);

    @Provides
    @Singleton
    static ConfigStorage getConfigStorage(ConfigurationLocation configurationLoc, ConfigStorage.Factory factory) {
        try {
            return factory.createForFile(configurationLoc.getConfigurationFile());
        } catch (IOException e) {
            logger.error("Cannot start at all", e);
            System.exit(-1);
            // Dummy return that won't happen because of System.exit
            throw new AssertionError("System.exit not working");
        }
    }

    @Provides
    static SystemPathResolver getSystemPathResolver() {
        return SystemPathResolver.getPathResolver();
    }

    @Provides
    static ThemeColors getThemeColors(CurrentTheme theme) {
        return theme.get().getColors();
    }

    @Provides
    @Singleton
    static CurrentTheme getCurrentTheme(ThemePref preference) {
        try {
            return new CurrentTheme(preference.getSelectedTheme());
        } catch (ThemeException e) {
            throw Main.showInitializationErrorAndExit("Cannot initialize GUI", e);
        }
    }
}
