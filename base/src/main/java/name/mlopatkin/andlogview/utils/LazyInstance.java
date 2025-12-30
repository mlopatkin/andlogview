/*
 * Copyright 2022 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.utils;

import dagger.Lazy;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * The lazy value. The actual value is computed upon the first access and then cached.
 * <p>
 * Thread safety note: the holder itself is thread-safe and the value is computed by a single thread only. The supplied
 * computations must be thread-safe too if the lazy instance is going to be accessed from multiple threads.
 *
 * @param <T> the type of the underlying value.
 */
public abstract class LazyInstance<T> implements Lazy<T> {
    private LazyInstance() {
    }

    /**
     * Creates a lazy holder that uses the provided supplier to compute the value upon the first access.
     *
     * @param supplier the supplier to compute the value
     * @param <T> the type of the value
     * @return a lazy holder
     */
    public static <T> LazyInstance<T> lazy(Supplier<? extends T> supplier) {
        return new LazyInstance<>() {
            private volatile @Nullable T instance;

            @Override
            public T get() {
                T value = instance;
                if (value == null) {
                    synchronized (this) {
                        value = instance;
                        if (value == null) {
                            instance = value = Objects.requireNonNull(supplier.get());
                        }
                    }
                }
                return value;
            }
        };
    }

    /**
     * Creates a lazy holder for an existing value, useful for passing stuff into things that expect lazy.
     *
     * @param value the value to wrap
     * @param <T> the type of the value
     * @return the value wrapped in the holder
     */
    public static <T> LazyInstance<T> of(T value) {
        return new LazyInstance<>() {
            @Override
            public T get() {
                return value;
            }
        };
    }
}
