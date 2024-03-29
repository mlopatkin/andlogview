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

package name.mlopatkin.andlogview.config;

class PreferenceImpl<T> implements Preference<T> {
    private final ConfigStorage storage;
    private final ConfigStorageClient<T> client;

    public PreferenceImpl(ConfigStorage storage, ConfigStorageClient<T> client) {
        this.storage = storage;
        this.client = client;
    }

    @Override
    public void set(T value) {
        storage.saveConfig(client, value);
    }

    @Override
    public T get() {
        return storage.loadConfig(client);
    }
}
