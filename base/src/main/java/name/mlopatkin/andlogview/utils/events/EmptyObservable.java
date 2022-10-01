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

package name.mlopatkin.andlogview.utils.events;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An empty observable that ignores added/removed observers.
 *
 * @param <T> the type of the observer
 */
public class EmptyObservable<T> implements Observable<T> {
    private static final EmptyObservable<?> INSTANCE = new EmptyObservable<>();
    private static final ScopedObserver EMPTY_SCOPED = () -> {};

    private EmptyObservable() {}

    @Override
    public void addObserver(T observer) {}

    @Override
    public void removeObserver(@Nullable T observer) {}

    @Override
    public ScopedObserver addScopedObserver(T observer) {
        return EMPTY_SCOPED;
    }

    @SuppressWarnings("unchecked")  // Safe as the actual type of the observer is ignored.
    public static <T> EmptyObservable<T> instance() {
        return (EmptyObservable<T>) INSTANCE;
    }
}
