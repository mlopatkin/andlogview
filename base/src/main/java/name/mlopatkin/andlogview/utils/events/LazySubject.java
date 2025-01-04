/*
 * Copyright 2025 the Andlogview authors
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

import name.mlopatkin.andlogview.thirdparty.observerlist.ObserverList;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;

public class LazySubject<T> implements Iterable<T> {
    private final ObserverList<T> observers = new ObserverList<>();
    private final Runnable subscribeAction;
    private final Runnable unsubscribeAction;

    private final Observable<T> observable = new Observable<T>() {
        @Override
        public void addObserver(T observer) {
            if (observers.isEmpty()) {
                subscribeAction.run();
            }
            observers.addObserver(observer);
        }

        @Override
        public void removeObserver(@Nullable T observer) {
            if (observers.isEmpty()) {
                return;
            }
            observers.removeObserver(observer);
            if (observers.isEmpty()) {
                unsubscribeAction.run();
            }
        }
    };

    public LazySubject(Runnable subscribeAction, Runnable unsubscribeAction) {
        this.subscribeAction = subscribeAction;
        this.unsubscribeAction = unsubscribeAction;
    }

    @Override
    public Iterator<T> iterator() {
        return observers.iterator();
    }

    public Observable<T> asObservable() {
        return observable;
    }
}
