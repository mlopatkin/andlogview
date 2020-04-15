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

/**
 * Config storage that always returns a default value
 */
public class FakeDefaultConfigStorage implements ConfigStorage {
    @Override
    public <T> void saveConfig(ConfigStorageClient<T> client, T value) {}

    @Override
    public <T> T loadConfig(ConfigStorageClient<T> client) {
        return client.getDefault();
    }
}
