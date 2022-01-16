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

import com.google.errorprone.annotations.concurrent.GuardedBy;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

class MemoizedPreference<T> implements Preference<T> {
    private final Object lock = new Object();
    private final Preference<T> basePreference;

    @GuardedBy("lock")
    private boolean hasValue;
    @GuardedBy("lock")
    private @MonotonicNonNull T cachedValue;

    public MemoizedPreference(Preference<T> basePreference) {
        this.basePreference = basePreference;
    }

    @Override
    public void set(T value) {
        synchronized (lock) {
            hasValue = true;
            cachedValue = value;
            basePreference.set(value);
        }
    }

    @Override
    public T get() {
        synchronized (lock) {
            if (hasValue) {
                return cachedValue;
            }
            hasValue = true;
            return cachedValue = basePreference.get();
        }
    }
}
