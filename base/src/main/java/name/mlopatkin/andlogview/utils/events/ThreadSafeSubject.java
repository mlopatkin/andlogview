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

import name.mlopatkin.andlogview.thirdparty.observerlist.ObserverList;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.concurrent.GuardedBy;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * A thread-safe version of {@link Subject}. Unlike the latter, only supports internal iteration to provide the proper
 * locking protocol.
 * <p>
 * It is safe to add or remove observers from any thread while iteration is ongoing, and to run several iterations
 * concurrently. Added observers will only be notified on the next iteration cycle. Removed observers won't be notified
 * in this cycle if removed on the same thread that performs the iteration. Otherwise, it is best-effort.
 * <p>
 * Memory consistency notes: registering an observer happens-before its first notification.
 *
 * @param <T> the type of the observer
 */
public class ThreadSafeSubject<T> {
    // ObserverList is not thread-safe, but we're serializing all structural modifications and
    @GuardedBy("observers")
    private final ObserverList<ObserverEntry<T>> observers = new ObserverList<>();
    @GuardedBy("observers")
    private final Map<T, ObserverEntry<T>> entries = new HashMap<>();

    private final ThreadSafeObservable<T> observableView = new ThreadSafeObservable<>() {
        @Override
        public void addObserver(T observer, Executor executor) {
            synchronized (observers) {
                Preconditions.checkArgument(!entries.containsKey(observer), "The observer %s is already registered",
                        observer);
                var entry = new ObserverEntry<>(observer, executor);
                entries.put(observer, entry);
                observers.addObserver(entry);
            }
        }

        @Override
        public void removeObserver(@Nullable T observer) {
            synchronized (observers) {
                var observerEntry = entries.remove(observer);
                if (observerEntry != null) {
                    observers.removeObserver(observerEntry);
                    // Prevent pending callbacks in the queue of the executor from running.
                    observerEntry.isRegistered = false;
                }
            }
        }
    };

    /**
     * Observable that can be passed to clients for subscribing. Note that it is impossible to cast returned observable
     * back to Subject.
     *
     * @return the observable
     */
    public ThreadSafeObservable<T> asObservable() {
        return observableView;
    }

    public boolean isEmpty() {
        synchronized (observers) {
            return observers.isEmpty();
        }
    }

    /**
     * Invokes {@code consumer} for all registered observers. The consumer might be invoked on other threads, or stored
     * in the queue of some executor. Beware of memory leaks and data races.
     *
     * @param consumer the consumer to perform notifications
     */
    public void forEach(Consumer<? super T> consumer) {
        final Iterator<ObserverEntry<T>> observerIterator;
        synchronized (observers) {
            observerIterator = observers.iterator();
        }
        while (next(observerIterator, consumer)) {
            // intentionally empty
        }
    }

    private boolean next(Iterator<ObserverEntry<T>> observerIterator, Consumer<? super T> consumer) {
        final ObserverEntry<T> nextObserver;
        synchronized (observers) {
            if (!observerIterator.hasNext()) {
                return false;
            }
            nextObserver = observerIterator.next();
        }
        nextObserver.executor.execute(() -> {
            // By the time the executor gets to executing the notification, the observer might be unregistered.
            // This check prevents spurious notification from happening.
            if (nextObserver.isRegistered) {
                consumer.accept(nextObserver.observer);
            }
        });
        return true;
    }

    private static class ObserverEntry<T> {
        final T observer;
        final Executor executor;
        volatile boolean isRegistered = true;

        public ObserverEntry(T observer, Executor executor) {
            this.observer = observer;
            this.executor = executor;
        }
    }
}
