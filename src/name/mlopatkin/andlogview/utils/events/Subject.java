/*
 * Copyright 2015 Mikhail Lopatkin
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

import java.util.Iterator;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * The Subject is something that allows embedder to notify registered parties.
 *
 * @param <T> the type of the observer
 */
@NotThreadSafe
public class Subject<T> implements Iterable<T> {
    private final ObserverList<T> observers = new ObserverList<>();

    private final Observable<T> observableView = new Observable<T>() {
        @Override
        public void addObserver(T observer) {
            observers.addObserver(observer);
        }

        @Override
        public void removeObserver(@Nullable T observer) {
            observers.removeObserver(observer);
        }
    };

    /**
     * Observable that can be passed to clients for subscribing. Note that it is impossible to cast returned observable
     * back to Subject.
     *
     * @return the observable
     */
    public Observable<T> asObservable() {
        return observableView;
    }

    @Override
    public Iterator<T> iterator() {
        return observers.iterator();
    }
}
