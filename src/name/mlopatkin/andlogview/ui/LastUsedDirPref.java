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

package name.mlopatkin.andlogview.ui;

import name.mlopatkin.andlogview.config.ConfigStorage;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.Optional;

import javax.inject.Inject;

/**
 * This preference stores the directory last used to load or save a file. It is intended to be used as an initial
 * directory in file selection dialogs.
 */
public class LastUsedDirPref {
    private static final ConfigStorage.ConfigStorageClient<Optional<String>> STORAGE_CLIENT =
            new ConfigStorage.ConfigStorageClient<Optional<String>>() {
                @Override
                public String getName() {
                    return "last_used_dir";
                }

                @Override
                public Optional<String> fromJson(Gson gson, JsonElement element)
                        throws ConfigStorage.InvalidJsonContentException {
                    if (!(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())) {
                        throw new ConfigStorage.InvalidJsonContentException("Expecting string");
                    }
                    return Optional.of(element.getAsString());
                }

                @Override
                public Optional<String> getDefault() {
                    return Optional.empty();
                }

                @Override
                public JsonElement toJson(Gson gson, Optional<String> value) {
                    return value.map(gson::toJsonTree).orElse(JsonNull.INSTANCE);
                }
            };
    private final ConfigStorage configStorage;

    private @Nullable File lastUsedDir;

    @Inject
    LastUsedDirPref(ConfigStorage configStorage) {
        this.configStorage = configStorage;
        set(configStorage.loadConfig(STORAGE_CLIENT).flatMap(fileName -> {
            File file = new File(fileName);
            if (file.exists() && file.isDirectory()) {
                return Optional.of(file);
            }
            return Optional.empty();
        }).orElse(null));
    }

    public @Nullable File get() {
        return lastUsedDir;
    }

    public void set(@Nullable File lastUsedDir) {
        this.lastUsedDir = lastUsedDir;
        configStorage.saveConfig(STORAGE_CLIENT, Optional.ofNullable(lastUsedDir).map(File::getAbsolutePath));
    }
}
