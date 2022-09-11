/*
 * Copyright 2022 Mikhail Lopatkin
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

import name.mlopatkin.andlogview.base.AtExitManager;
import name.mlopatkin.andlogview.device.AdbManager;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;

import dagger.Module;
import dagger.Provides;

import java.util.concurrent.Executor;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public abstract class DeviceModule {
    private DeviceModule() {
    }

    @Provides
    @Singleton
    static AdbManager getAdbManager(AtExitManager atExitManager, @Named(AppExecutors.FILE_EXECUTOR) Executor ioExecutor,
            AdbConfigurationPref adbLocation) {
        return AdbManager.create(atExitManager, ioExecutor, adbLocation);
    }
}
