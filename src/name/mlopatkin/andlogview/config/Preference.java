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

/**
 * Preference binds {@link ConfigStorage} and {@link ConfigStorageClient} together.
 *
 * @param <T> the type of the stored value
 */
public interface Preference<T> {
    /**
     * Stores the value in the bound config storage.
     */
    void set(T value);

    /**
     * Retrieves the value from the bound config storage.
     */
    T get();

    /**
     * Returns a memoized version of this preference. Memoized preference caches the value on first read/write and
     * subsequent reads return cached value without hitting the config storage.
     */
    default Preference<T> memoize() {
        if (this instanceof MemoizedPreference) {
            return this;
        }
        return new MemoizedPreference<>(this);
    }
}
