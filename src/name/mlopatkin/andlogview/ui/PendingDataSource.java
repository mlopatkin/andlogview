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

package name.mlopatkin.andlogview.ui;

import static name.mlopatkin.andlogview.utils.MyFutures.errorHandler;
import static name.mlopatkin.andlogview.utils.MyFutures.ignoreCancellations;
import static name.mlopatkin.andlogview.utils.MyFutures.valueHandler;

import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.utils.Cancellable;
import name.mlopatkin.andlogview.utils.MyFutures;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A pending {@link DataSource} being initialized. The client can cancel initialization or can subscribe to the
 * completion of the initialization.
 *
 * @param <T> the concrete subtype of the DataSource
 */
public abstract class PendingDataSource<T extends @Nullable DataSource> implements Cancellable {
    /**
     * Registers a consumer to receive the {@link DataSource} when it is ready. The consumer can be called
     * synchronously if the data source is already available.
     *
     * @param consumer the consumer to process the DataSource
     */
    public abstract void whenAvailable(Consumer<? super T> consumer);

    /**
     * Registers a consumer to receive the failure when it happens. The consumer can be called
     * synchronously if the data source has already failed.
     * <p>
     * Without using this method, all data source failures coming from this class will be lost.
     *
     * @param consumer the consumer to process the failure
     */
    // TODO(mlopatkin) whenFailed is actually inconvenient to use, because it makes it harder to reason if the
    //  exception should be considered unhandled. The typical approach of "add uncaught forwarded at the end of every
    //  chain" doesn't work. It seems that the only way is to provide the handler at creation time.
    //  Otherwise, we're always leaving PDS that weren't obtained to hang in the air with nowhere to log. Maybe nobody
    //  cares, though?
    public abstract void whenFailed(Consumer<? super Throwable> consumer);

    /**
     * Wraps completable future into the {@link PendingDataSource} instance. Cancelling the returned
     * {@link PendingDataSource} cancels the given future.
     *
     * @param future the future that provides the {@link DataSource}
     * @param <T> the concrete subtype of the DataSource
     * @return PendingDataSource that completes with the future
     */
    public static <T extends DataSource> PendingDataSource<T> fromFuture(CompletableFuture<T> future) {
        return new CompletablePendingDataSource<>(future);
    }

    /**
     * Creates an already completed {@link PendingDataSource}. It invokes callbacks immediately, and cannot be
     * cancelled.
     *
     * @param dataSource the data source
     * @param <T> the concrete subtype of the DataSource
     * @return the completed PendingDataSource
     */
    public static <T extends DataSource> PendingDataSource<T> newCompleted(T dataSource) {
        return new PendingDataSource<>() {
            @Override
            public void whenAvailable(Consumer<? super T> consumer) {
                consumer.accept(dataSource);
            }

            @Override
            public void whenFailed(Consumer<? super Throwable> consumer) {
                // Completed DataSource never fail.
            }

            @Override
            public boolean cancel() {
                // Completed DataSource cannot be cancelled.
                return false;
            }
        };
    }

    /**
     * Creates an already completed {@link PendingDataSource}. It invokes callbacks immediately with the {@code null}
     * argument, and cannot be cancelled.
     *
     * @param <T> the concrete subtype of the DataSource
     * @return the completed PendingDataSource
     */
    @SuppressWarnings("NullAway")  // NullAway cannot handle nullable generics (yet?)
    public static <T extends DataSource> PendingDataSource<@Nullable T> newEmpty() {
        return newCompleted(null);
    }

    /**
     * {@link PendingDataSource} implementation that can be configured.
     *
     * @param <T> the concrete subtype of the DataSource
     */
    public static class CompletablePendingDataSource<T extends DataSource> extends PendingDataSource<T> {
        private static final Function<Throwable, Void> EXCEPTION_HANDLER = MyFutures::uncaughtException;

        private final CompletableFuture<T> dataSourceFuture;

        /**
         * Wraps the provided future. Cancelling this {@link PendingDataSource} cancels the future. All callbacks are
         * registered on the provided future. Completing the future completes this source.
         *
         * @param future the future to wrap
         */
        public CompletablePendingDataSource(CompletableFuture<T> future) {
            this.dataSourceFuture = future;
        }

        /**
         * Creates an empty {@link PendingDataSource}.
         */
        public CompletablePendingDataSource() {
            this(new CompletableFuture<>());
        }

        /**
         * Adds {@link Cancellable} subclass as a stage. Cancelling this {@link PendingDataSource} will cancel the
         * provided stage.
         *
         * @param stage the stage to cancel with this DataSource
         * @param <C> the concrete subtype of cancellable
         * @return the same stage, useful for expressions
         */
        @CanIgnoreReturnValue
        public <C extends Cancellable> C addStage(C stage) {
            assert stage != this;
            addCancellationHandler(stage::cancel);
            return stage;
        }

        /**
         * Adds {@link CompletableFuture} subclass as a stage. Cancelling this {@link PendingDataSource} will cancel the
         * provided stage.
         *
         * @param stage the stage to cancel with this DataSource
         * @param <F> the concrete subtype of CompletableFuture
         * @return the same stage, useful for expressions
         */
        @CanIgnoreReturnValue
        public <E, F extends CompletableFuture<E>> F addStage(F stage) {
            assert stage != dataSourceFuture;
            addCancellationHandler(() -> stage.cancel(false));
            return stage;
        }

        @Override
        public void whenAvailable(Consumer<? super T> consumer) {
            dataSourceFuture.handle(valueHandler(consumer))
                    // we're skipping failures coming from the downstream future. Users may handle them with
                    // whenFailure. However, exception from the consumer go into EXCEPTION_HANDLER.
                    .exceptionally(EXCEPTION_HANDLER);
        }

        @Override
        public void whenFailed(Consumer<? super Throwable> consumer) {
            dataSourceFuture.handle(errorHandler(ignoreCancellations(consumer)))
                    // Handle failures coming from the consumer
                    .exceptionally(EXCEPTION_HANDLER);
        }

        @Override
        public boolean cancel() {
            return dataSourceFuture.cancel(false);
        }

        /**
         * Completes the pending job, invoking all registered callbacks.
         *
         * @param dataSource the datasource to complete with
         * @throws IllegalStateException if this instance is already completed or cancelled
         */
        public void complete(T dataSource) {
            if (!dataSourceFuture.complete(dataSource)) {
                // TODO(mlopatkin) this might be too strict.
                throw new IllegalStateException("Cannot complete the future, because it is already completed");
            }
        }

        /**
         * Completes the pending job exceptionally, invoking all registered callbacks,
         *
         * @param exception the exception
         * @throws IllegalStateException if this instance is already completed or cancelled
         */
        public void fail(Throwable exception) {
            if (!dataSourceFuture.completeExceptionally(exception)) {
                // TODO(mlopatkin) this might be too strict.
                throw new IllegalStateException("Cannot complete the future, because it is already completed");
            }
        }

        private void addCancellationHandler(Runnable handler) {
            dataSourceFuture.exceptionally(adaptCancellationHandler(handler));
        }

        private Function<Throwable, @Nullable T> adaptCancellationHandler(Runnable cancellationHandler) {
            return originalThrowable -> {
                try {
                    if (dataSourceFuture.isCancelled()) {
                        cancellationHandler.run();
                    }
                } catch (Throwable th) {
                    // ok to catch Throwable there
                    EXCEPTION_HANDLER.apply(th);
                }
                return null;
            };
        }
    }
}
