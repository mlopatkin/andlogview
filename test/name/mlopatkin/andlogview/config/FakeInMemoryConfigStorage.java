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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Simplest conforming implementation of the {@link ConfigStorage}. It stores data in serialized form to help catch
 * errors in serialization/deserialization routines and properly return different objects from different calls to
 * {@link #loadConfig(ConfigStorageClient)}.
 */
public class FakeInMemoryConfigStorage implements ConfigStorage {
    private final Gson gson;
    private final Map<String, JsonElement> data = new HashMap<>();

    public FakeInMemoryConfigStorage(Gson gson) {
        this.gson = gson;
    }

    public FakeInMemoryConfigStorage() {
        this(new GsonBuilder().setPrettyPrinting().create());
    }

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

    @Override
    public boolean hasStoredDataFor(String clientName) {
        return data.containsKey(clientName);
    }

    public void setJsonData(String key, String json) {
        data.put(key, JsonParser.parseString(json));
    }

    public @Nullable Object getJsonData(String element0, String... keyElements) {
        StringBuilder path = new StringBuilder(element0);
        JsonElement current = data.get(element0);
        for (var p : keyElements) {
            if (current instanceof JsonObject jsonObject) {
                current = jsonObject.get(p);
                path.append('.').append(p);
            } else {
                throw new IllegalArgumentException(
                        "Expected element " + path + " to be a JsonObject but got " + current
                );
            }
        }

        return current;
    }
}
