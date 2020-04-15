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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Simplest conforming implementation of the {@link ConfigStorage}. It stores data in serialized form to help catch
 * errors in serialization/deserialization routines and properly return different objects from different calls to
 * {@link #loadConfig(ConfigStorageClient)}.
 */
public class FakeInMemoryConfigStorage implements ConfigStorage {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, JsonElement> data = new HashMap<>();

    @Override
    public <T> void saveConfig(ConfigStorageClient<T> client, T value) {
        data.put(client.getName(), client.toJson(gson, value));
    }

    @Override
    public <T> T loadConfig(ConfigStorageClient<T> client) {
        if (data.containsKey(client.getName())) {
            try {
                return client.fromJson(gson, data.get(client.getName()));
            } catch (InvalidJsonContentException e) {
                // ok to catch InvalidJsonContentException there
            }
        }
        return client.getDefault();
    }
}
