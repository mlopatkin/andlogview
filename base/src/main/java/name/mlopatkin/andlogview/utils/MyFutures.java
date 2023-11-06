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

import name.mlopatkin.andlogview.base.MyThrowables;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

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

    /**
     * Backport of the {@code CompletableFuture::failedFuture} for Java 8.
     *
     * @param th the failure reason
     * @param <T> the type of the future
     * @return the completed completion stage that has failed with the provided failure
     */
    public static <T> CompletableFuture<T> failedFuture(Throwable th) {
        var future = new CompletableFuture<T>();
        future.completeExceptionally(th);
        return future;
    }

    /**
     * Adapter for void-returning methods to be used in {@link CompletableFuture#exceptionally(Function)}.
     *
     * @param consumer the consumer of the exception
     * @return the consumer adapted to a function
     */
    public static Function<Throwable, Void> exceptionHandler(Consumer<Throwable> consumer) {
        return t -> {
            consumer.accept(t);
            return null;
        };
    }

    /**
     * Adapter for void-returning methods to be used in {@link CompletableFuture#exceptionally(Function)}.
     *
     * @param consumer the failure handler
     * @return the consumer adapted to a function
     */
    public static Function<Throwable, Void> exceptionHandler(Runnable consumer) {
        return t -> {
            consumer.run();
            return null;
        };
    }

    /**
     * Adapter for the {@link CompletableFuture#exceptionally(Function)} that suppresses failures because of cancelled
     * chain. The downstream chain still executes in this case.
     *
     * @return the function that consumes cancellations but rethrows everything else
     */
    public static Function<Throwable, Void> skipCancellations() {
        return th -> {
            if (MyThrowables.unwrapUninteresting(th) instanceof CancellationException) {
                return null;
            }
            throw sneakyRethrow(th);
        };
    }

    /**
     * Adapter for the {@link CompletableFuture#handle(BiFunction)} that allows using consumers there.
     *
     * @param consumer the bi-consumer
     * @param <T> the type of the value produced by the future
     * @return the function that adapts the consumer to Void returning type
     */
    public static <T> BiFunction<T, @Nullable Throwable, Void> consumingHandler(
            BiConsumer<? super T, @Nullable ? super Throwable> consumer) {
        return (r, th) -> {
            consumer.accept(r, th);
            return null;
        };
    }

    /**
     * Adapter for the {@link CompletableFuture#handle(BiFunction)} that allows using consumers there.
     *
     * @param valueConsumer the consumer for the value
     * @param errorConsumer the consumer for the value
     * @param <T> the type of the value produced by the future
     * @return the function that adapts the consumers to Void returning type
     */
    public static <T> BiFunction<T, @Nullable Throwable, Void> consumingHandler(
            Consumer<? super T> valueConsumer, Consumer<@Nullable ? super Throwable> errorConsumer) {
        return (r, th) -> {
            if (th != null) {
                errorConsumer.accept(th);
            } else {
                valueConsumer.accept(r);
            }
            return null;
        };
    }

    /**
     * Helper for {@link CompletableFuture#exceptionally(Function)} that forwards exception to thread's default
     * exception handler. The failure is rethrown, so the downstream chain still fails.
     *
     * @param th the throwable to forward
     * @return {@code null}
     */
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public static <T> @Nullable T uncaughtException(Throwable th) {
        Thread thread = Thread.currentThread();
        thread.getUncaughtExceptionHandler().uncaughtException(thread, th);
        // Rethrow the exception, so the downstream chain is still unsuccessful.
        throw sneakyRethrow(th);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> RuntimeException sneakyRethrow(Throwable th) throws E {
        throw (E) th;
    }
}
