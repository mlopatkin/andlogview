/*
 * Copyright 2024 the Andlogview authors
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
 * A combination of two observables. The observers registered with this observable will receive notifications from both.
 *
 * @param <T> the type of the observer
 */
public class CompoundObservable<T> implements Observable<T> {
    private final Observable<? super T> first;
    private final Observable<? super T> second;

    public CompoundObservable(Observable<? super T> first, Observable<? super T> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public void addObserver(T observer) {
        first.addObserver(observer);
        second.addObserver(observer);
    }

    @Override
    public void removeObserver(@Nullable T observer) {
        first.removeObserver(observer);
        second.removeObserver(observer);
    }
}
