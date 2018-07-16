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

package org.bitbucket.mlopatkin.android.logviewer.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import dagger.Module;
import dagger.Provides;

import org.bitbucket.mlopatkin.utils.Threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class ConfigModule {
    static final String CONFIG_THREAD_POOL = "config_thread_pool";

    @Provides
    @Singleton
    @Named(CONFIG_THREAD_POOL)
    ExecutorService getConfigIoThreadPool() {
        return Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder().setThreadFactory(Threads.withName("StorageFileWorker"))
                                          .setDaemon(true).build());
    }
}
