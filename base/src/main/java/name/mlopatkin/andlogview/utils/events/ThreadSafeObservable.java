/*
 * Copyright 2023 the Andlogview authors
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

import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executor;

/**
 * Thread-safe version of {@link Observable}. Observers can be added or removed on any thread. Observers may be notified
 * on any thread, even concurrently, though this depends on the embedder.
 *
 * @param <T> the type of the observer
 */
public interface ThreadSafeObservable<T> extends Observable<T> {
    /**
     * Adds an observer to be notified on the provided executor. Adding the same observer multiple times, even with
     * different executors is an error.
     *
     * @param observer the observer
     * @param executor the executor to run its callbacks on
     * @throws IllegalArgumentException if the observer is already added
     */
    void addObserver(T observer, Executor executor);

    /**
     * Adds an observer to be notified on the provided executor and returns a {@link ScopedObserver} instance that can
     * be used to remove the added observer. Adding the same observer multiple times, even with different executors is
     * an error.
     *
     * @param observer the observer
     * @param executor the executor to run its callbacks on
     * @return the scoped observer that removes the added observer upon {@link ScopedObserver#close()} call
     * @throws IllegalArgumentException if the observer is already added
     */
    default ScopedObserver addScopedObserver(T observer, Executor executor) {
        addObserver(observer, executor);
        return () -> removeObserver(observer);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Adding the same observer multiple times is an error. The thread on which the observer is going to be notified is
     * not defined.
     *
     * @throws IllegalArgumentException if the observer is already added
     */
    @Override
    default void addObserver(T observer) {
        addObserver(observer, MoreExecutors.directExecutor());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Adding the same observer multiple times is an error. The thread on which the observer is going to be notified is
     * not defined.
     *
     * @throws IllegalArgumentException if the observer is already added
     */
    @Override
    default ScopedObserver addScopedObserver(T observer) {
        return Observable.super.addScopedObserver(observer);
    }
}
