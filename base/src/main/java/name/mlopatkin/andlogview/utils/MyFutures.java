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

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import org.jspecify.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utilities to work with {@link Future}s.
 */
public final class MyFutures {
    private MyFutures() {}

    /**
     * Converts the future into the {@link Cancellable}. CompletableFuture cannot be interrupted so the Cancellable
     * makes no attempts to do so.
     *
     * @param future the future to convert
     * @return the cancellable that forwards cancel to the future
     */
    public static Cancellable toCancellable(CompletableFuture<?> future) {
        return () -> future.cancel(false);
    }

    /**
     * Connects output of the {@code source} to the {@code target}. The value or exception produced by the source are
     * set to target.
     *
     * @param source the source future to produce the result
     * @param target the target future to receive the result
     * @param <T> the type of the future
     */
    public static <T> void connect(CompletionStage<T> source, CompletableFuture<? super T> target) {
        source.whenComplete((v, th) -> {
            if (th != null) {
                if (isCancellation(th)) {
                    target.cancel(false);
                } else {
                    target.completeExceptionally(th);
                }
            } else {
                target.complete(v);
            }
        });
    }

    /**
     * Wraps failure handler into cancellation-ignoring adapter. The adapter can unwrap cancellation exceptions. It is
     * intended for {@link #consumingHandler(Consumer, Consumer)} and {@link #exceptionHandler(Consumer)}
     * implementations.
     *
     * @param failureHandler the failure handler to wrap
     * @return the cancellation-tolerant handler
     */
    public static Consumer<Throwable> ignoreCancellations(Consumer<? super Throwable> failureHandler) {
        return cancellationHandler(() -> {}, failureHandler);
    }

    /**
     * Builds a failure handler with a special handling of cancellations. It can unwrap cancellation exceptions.
     *
     * @param cancellationHandler the cancellation handler
     * @param failureHandler the failure handler
     * @return the combined handler
     */
    public static Consumer<Throwable> cancellationHandler(Runnable cancellationHandler,
            Consumer<? super Throwable> failureHandler) {
        return th -> {
            if (isCancellation(th)) {
                cancellationHandler.run();
            } else {
                failureHandler.accept(th);
            }
        };
    }

    /**
     * Builds a cancellation handler that rethrows other exceptions. It can unwrap cancellation exceptions.
     *
     * @param cancellationHandler the cancellation handler
     * @return the combined handler
     */
    public static Consumer<Throwable> cancellationHandler(Runnable cancellationHandler) {
        return cancellationHandler(cancellationHandler, MyThrowables::sneakyRethrow);
    }

    /**
     * Builds a failure handler with a special handling of cancellations. It can unwrap cancellation exceptions.
     *
     * @param cancellationHandler the cancellation handler
     * @param failureHandler the failure handler
     * @return the combined handler
     */
    public static <T> Function<Throwable, T> cancellationTransformer(Supplier<? extends T> cancellationHandler,
            Function<? super Throwable, ? extends T> failureHandler) {
        return th -> {
            if (isCancellation(th)) {
                return cancellationHandler.get();
            } else {
                return failureHandler.apply(th);
            }
        };
    }

    /**
     * Builds a failure handler with a special handling of cancellations. It can unwrap cancellation exceptions. Unlike
     * the two-argument version, it just rethrows other exceptions without handling them.
     *
     * @param cancellationHandler the cancellation handler
     * @return the combined handler
     */
    public static <T> Function<Throwable, T> cancellationTransformer(Supplier<? extends T> cancellationHandler) {
        return cancellationTransformer(cancellationHandler, th -> {
            throw MyThrowables.sneakyRethrow(th);
        });
    }

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
    public static Function<Throwable, @Nullable Void> exceptionHandler(Consumer<? super Throwable> consumer) {
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
    public static Function<Throwable, @Nullable Void> exceptionHandler(Runnable consumer) {
        return t -> {
            consumer.run();
            return null;
        };
    }

    /**
     * Adapter for the {@link CompletableFuture#handle(BiFunction)} that allows using consumers there.
     *
     * @param consumer the bi-consumer
     * @param <T> the type of the value produced by the future
     * @return the function that adapts the consumer to Void returning type
     */
    public static <T> BiFunction<T, @Nullable Throwable, @Nullable Void> consumingHandler(
            BiConsumer<? super @Nullable T, ? super @Nullable Throwable> consumer) {
        return (r, th) -> {
            consumer.accept(r, th);
            return null;
        };
    }

    /**
     * Adapter for the {@link CompletableFuture#handle(BiFunction)} that allows using consumers there.
     *
     * @param valueConsumer the consumer for the value
     * @param errorConsumer the consumer for the error
     * @param <T> the type of the value produced by the future
     * @return the function that adapts the consumers to Void returning type
     */
    public static <T> BiFunction<T, @Nullable Throwable, @Nullable Void> consumingHandler(
            Consumer<? super T> valueConsumer, Consumer<? super Throwable> errorConsumer) {
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
     * Adapter for the {@link CompletableFuture#handle(BiFunction)} that only cares about values and ignores errors.
     * Unlike {@link CompletableFuture#thenAccept(Consumer)}, the exception from upstream doesn't propagate further.
     *
     * @param valueConsumer the consumer for the value
     * @param <T> the type of the value produced by the future
     * @return the function that adapts the consumers to Void returning type
     */
    public static <T> BiFunction<T, @Nullable Throwable, @Nullable Void> valueHandler(
            Consumer<? super T> valueConsumer) {
        return consumingHandler(valueConsumer, ignored -> {});
    }

    /**
     * Adapter for the {@link CompletableFuture#handle(BiFunction)} that only cares about errors and ignores value.
     * Unlike {@link CompletableFuture#exceptionally(Function)}}, this method changes the outgoing type to
     * {@code Void}.
     *
     * @param errorConsumer the consumer for the error
     * @param <T> the type of the value produced by the future
     * @return the function that adapts the consumers to Void returning type
     */
    public static <T> BiFunction<T, @Nullable Throwable, @Nullable Void> errorHandler(
            Consumer<? super Throwable> errorConsumer) {
        return consumingHandler(ignored -> {}, errorConsumer);
    }

    /**
     * Arranges the futures in such a way that cancellation of the {@code canceller} cancels the provided
     * {@code future}. This method returns the given {@code future} to allow for simpler expressions. Cancellations of
     * canceller's upstream stages also cancel the {@code future}, if they cause the canceller to complete.
     *
     * @param future the future to be cancelled
     * @param canceller the stage, cancellation of which cancels the {@code future}
     * @param <F> the exact type of the given {@code future}
     * @return the {@code future}
     */
    @CanIgnoreReturnValue
    public static <F extends CompletableFuture<?>> F cancelBy(F future, CompletionStage<?> canceller) {
        canceller.exceptionally(th -> {
            if (isCancellation(th)) {
                future.cancel(false);
            }
            return null;
        }).exceptionally(MyFutures::uncaughtException);
        return future;
    }

    /**
     * Arranges the futures in such a way that cancellation of the {@code canceller} cancels the provided
     * {@code cancellable}. This method returns the given {@code cancellable} to allow for simpler expressions.
     * Cancellations of canceller's upstream stages also cancel the {@code cancellable}, if they cause the canceller to
     * complete.
     *
     * @param cancellable the cancellable to be cancelled
     * @param canceller the stage, cancellation of which cancels the {@code cancellable}
     * @param <T> the exact type of the given {@code cancellable}
     * @return the {@code cancellable}
     */
    @CanIgnoreReturnValue
    public static <T extends Cancellable> T cancelBy(T cancellable, CompletionStage<?> canceller) {
        canceller.exceptionally(th -> {
            if (isCancellation(th)) {
                cancellable.cancel();
            }
            return null;
        }).exceptionally(MyFutures::uncaughtException);
        return cancellable;
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
        throw MyThrowables.sneakyRethrow(th);
    }

    private static boolean isCancellation(Throwable th) {
        return (MyThrowables.unwrapUninteresting(th) instanceof CancellationException);
    }
}
