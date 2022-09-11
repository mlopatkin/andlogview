/*
 * Copyright 2020 Mikhail Lopatkin
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

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Utilities to work with {@link Future}s.
 */
public final class MyFutures {
    private MyFutures() {}

    /**
     * Helper interface for {@code runAsync}.
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * Executes a callable on the provided executor. Result is delivered as a {@link CompletableFuture}. Note that it
     * isn't possible to cancel a running callable with {@link CompletableFuture#cancel(boolean)}, only callback chain
     * is cancelled.
     *
     * @param callable the callable to execute
     * @param executor the executor to run the callable on
     * @param <V> the type of the result
     * @return the completable future that completes with the result of a callable
     */
    public static <V> CompletableFuture<V> runAsync(Callable<V> callable, Executor executor) {
        CompletableFuture<V> future = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                future.complete(callable.call());
            } catch (Throwable e) {  // OK to catch Throwable here
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Executes a runnable on the provided executor. Result signal is delivered as a {@link CompletableFuture}. Note
     * that it isn't possible to cancel a running callable with {@link CompletableFuture#cancel(boolean)}, only callback
     * chain is cancelled.
     *
     * @param runnable the runnable to execute
     * @param executor the executor to run the callable on
     * @return the completable future that completes with the result of a callable
     */
    public static CompletableFuture<Void> runAsync(ThrowingRunnable runnable, Executor executor) {
        return runAsync(() -> {
            runnable.run();
            return null;
        }, executor);
    }
}
