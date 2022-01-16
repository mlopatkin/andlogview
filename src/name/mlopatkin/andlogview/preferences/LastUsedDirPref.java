/*
 * Copyright 2022 the Andlogview authors
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
import name.mlopatkin.andlogview.config.ConfigStorageClient;
import name.mlopatkin.andlogview.config.InvalidJsonContentException;
import name.mlopatkin.andlogview.config.NamedClient;
import name.mlopatkin.andlogview.config.Preference;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This preference stores the directory last used to load or save a file. It is intended to be used as an initial
 * directory in file selection dialogs.
 */
@Singleton
public class LastUsedDirPref {
    private static final ConfigStorageClient<Optional<String>> STORAGE_CLIENT =
            new NamedClient<Optional<String>>("last_used_dir") {
                @Override
                public Optional<String> fromJson(Gson gson, JsonElement element)
                        throws InvalidJsonContentException {
                    if (!(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())) {
                        throw new InvalidJsonContentException("Expecting string");
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
    private final Preference<Optional<String>> preference;

    private @Nullable File lastUsedDir;

    @Inject
    LastUsedDirPref(ConfigStorage configStorage) {
        this.preference = configStorage.preference(STORAGE_CLIENT);
        set(preference.get().flatMap(fileName -> {
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
        preference.set(Optional.ofNullable(lastUsedDir).map(File::getAbsolutePath));
    }
}
