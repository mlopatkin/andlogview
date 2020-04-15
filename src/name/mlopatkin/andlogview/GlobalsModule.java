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

import dagger.Module;
import dagger.Provides;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

@Module
class GlobalsModule {
    private static final Logger logger = Logger.getLogger(GlobalsModule.class);

    private final File appConfigDir;

    public GlobalsModule(File appConfigDir) {
        this.appConfigDir = appConfigDir;
    }

    @Provides
    @Singleton
    @SuppressWarnings("DaggerProvidesNull")
    ConfigStorage getConfigStorage(ConfigStorage.Factory factory) {
        try {
            return factory.createForFile(new File(appConfigDir, "logview.json"));
        } catch (IOException e) {
            logger.fatal("Cannot start at all", e);
            System.exit(-1);
            // Dummy return that won't happen because of System.exit
            throw new AssertionError("System.exit not working");
        }
    }
}
