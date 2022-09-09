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

package name.mlopatkin.andlogview.config;

import name.mlopatkin.andlogview.base.AtExitManager;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Provides an access to a persistent storage for filters.
 */
@ThreadSafe
public interface ConfigStorage {
    <T> void saveConfig(ConfigStorageClient<T> client, T value);

    <T> T loadConfig(ConfigStorageClient<T> client);

    default <T> Preference<T> preference(ConfigStorageClient<T> client) {
        return new PreferenceImpl<>(this, client);
    }

    @Singleton
    class Factory {
        private final ExecutorService ioThreadPool;
        private final AtExitManager atExitManager;

        @Inject
        Factory(@Named(ConfigModule.CONFIG_THREAD_POOL) ExecutorService ioThreadPool, AtExitManager atExitManager) {
            this.ioThreadPool = ioThreadPool;
            this.atExitManager = atExitManager;
        }

        public ConfigStorage createForFile(File file) throws IOException {
            if (!file.exists()) {
                Files.touch(file);
            }
            final ConfigStorageImpl result = new ConfigStorageImpl(
                    Files.asCharSource(file, StandardCharsets.UTF_8), Files.asCharSink(file, StandardCharsets.UTF_8),
                    ioThreadPool);
            atExitManager.registerExitAction(() -> {
                try {
                    result.shutdown(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            result.load();
            return result;
        }
    }
}
