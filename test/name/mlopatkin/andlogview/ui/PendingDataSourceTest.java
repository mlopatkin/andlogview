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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.test.ThreadTestUtils;
import name.mlopatkin.andlogview.utils.Cancellable;

import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ExtendWith(MockitoExtension.class)
class PendingDataSourceTest {
    @Mock
    DataSource dataSource;
    @Mock
    Consumer<@Nullable DataSource> dataSourceConsumer;
    @Mock
    Consumer<Throwable> failureConsumer;
    @Mock
    Cancellable cancellable;

    final CompletableFuture<DataSource> future = new CompletableFuture<>();

    private Thread.UncaughtExceptionHandler exceptionHandler;
    private final List<Throwable> unhandledException = new ArrayList<>();

    @BeforeEach
    void setUp() {
        exceptionHandler = Thread.currentThread().getUncaughtExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler((thread, th) -> unhandledException.add(th));
    }

    @AfterEach
    void tearDown() {
        Thread.currentThread().setUncaughtExceptionHandler(exceptionHandler);
        Assertions.setMaxStackTraceElementsDisplayed(25);
        assertThat(unhandledException).isEmpty();
    }

    @Test
    void completedPdsCannotBeCancelled() {
        var pds = PendingDataSource.newCompleted(dataSource);

        assertThat(pds.cancel()).isFalse();
    }

    @Test
    void completedPdsDeliversValue() {
        var pds = PendingDataSource.newCompleted(dataSource);

        pds.whenAvailable(dataSourceConsumer);

        verify(dataSourceConsumer).accept(dataSource);
    }

    @Test
    void completedPdsDeliversNoFailure() {
        var pds = PendingDataSource.newCompleted(dataSource);

        pds.whenFailed(failureConsumer);

        verify(failureConsumer, never()).accept(any());
    }

    @Test
    void emptyPdsCannotBeCancelled() {
        var pds = PendingDataSource.newEmpty();

        assertThat(pds.cancel()).isFalse();
    }

    @Test
    void emptyPdsDeliversNullValue() {
        var pds = PendingDataSource.newEmpty();

        pds.whenAvailable(dataSourceConsumer);

        verify(dataSourceConsumer).accept(isNull());
    }

    @Test
    void emptyPdsDeliversNoFailure() {
        var pds = PendingDataSource.newEmpty();

        pds.whenFailed(failureConsumer);

        verify(failureConsumer, never()).accept(any());
    }

    @Test
    void fromFuturePdsCancelsFuture() {
        var pds = PendingDataSource.fromFuture(future);

        assertThat(pds.cancel()).isTrue();
        assertThat(future).isCancelled();
    }

    @Test
    void fromFuturePdsCannotBeCancelledIfFutureIsCompleted() {
        var future = CompletableFuture.completedFuture(dataSource);
        var pds = PendingDataSource.fromFuture(future);

        assertThat(pds.cancel()).isFalse();
    }

    @Test
    void fromFuturePdsInvokesNoCallbacksBeforeFutureCompletes() {
        var pds = PendingDataSource.fromFuture(future);

        pds.whenAvailable(dataSourceConsumer);
        pds.whenFailed(failureConsumer);

        verify(dataSourceConsumer, never()).accept(any());
        verify(failureConsumer, never()).accept(any());
    }


    @Test
    void fromFuturePdsInvokesOnlyResultCallbacksAfterFutureCompletes() {
        var pds = PendingDataSource.fromFuture(future);
        future.complete(dataSource);

        pds.whenAvailable(dataSourceConsumer);
        pds.whenFailed(failureConsumer);

        verify(dataSourceConsumer).accept(dataSource);
        verify(failureConsumer, never()).accept(any());
    }

    @Test
    void fromFuturePdsInvokesOnlyFailureCallbacksAfterFutureFails() {
        var pds = PendingDataSource.fromFuture(future);
        var ex = new Exception();
        future.completeExceptionally(ex);

        pds.whenAvailable(dataSourceConsumer);
        pds.whenFailed(failureConsumer);

        verify(dataSourceConsumer, never()).accept(any());
        verify(failureConsumer).accept(ex);
    }

    @Test
    void fromFuturePdsInvokesExistingResultCallbacksAfterFutureCompletes() {
        var pds = PendingDataSource.fromFuture(future);

        pds.whenAvailable(dataSourceConsumer);
        pds.whenFailed(failureConsumer);

        future.complete(dataSource);

        verify(dataSourceConsumer).accept(dataSource);
        verify(failureConsumer, never()).accept(any());
    }

    @Test
    void fromFuturePdsInvokesExistingFailureCallbacksAfterFutureFails() {
        var pds = PendingDataSource.fromFuture(future);
        pds.whenAvailable(dataSourceConsumer);
        pds.whenFailed(failureConsumer);

        var ex = new Exception();
        future.completeExceptionally(ex);

        verify(dataSourceConsumer, never()).accept(any());
        verify(failureConsumer).accept(ex);
    }

    @Test
    void fromFuturePdsInvokesNoCallbacksAfterBeingCancelled() {
        var pds = PendingDataSource.fromFuture(future);
        pds.whenAvailable(dataSourceConsumer);
        pds.whenFailed(failureConsumer);

        pds.cancel();

        verify(dataSourceConsumer, never()).accept(any());
        verify(failureConsumer, never()).accept(any());
    }

    @Test
    void completablePdsReturnsStagesFromAddMethod() {
        var pds = new PendingDataSource.CompletablePendingDataSource<>();

        assertThat(pds.addStage(future)).isSameAs(future);
        assertThat(pds.addStage(cancellable)).isSameAs(cancellable);
    }

    @Test
    void completablePdsCancelsStages() {
        var pds = new PendingDataSource.CompletablePendingDataSource<>();

        pds.addStage(cancellable);
        pds.addStage(future);

        assertThat(pds.cancel()).isTrue();

        verify(cancellable).cancel();
        assertThat(future).isCancelled();
    }

    @Test
    void completablePdsCanBeCancelledIfStageIsCompleted() {
        var future = CompletableFuture.completedFuture(dataSource);
        var pds = new PendingDataSource.CompletablePendingDataSource<>();
        pds.addStage(future);

        assertThat(pds.cancel()).isTrue();
    }

    @Test
    void completablePdsInvokesNoCallbacksBeforeCompleting() {
        var pds = new PendingDataSource.CompletablePendingDataSource<>();

        pds.whenAvailable(dataSourceConsumer);
        pds.whenFailed(failureConsumer);

        verify(dataSourceConsumer, never()).accept(any());
        verify(failureConsumer, never()).accept(any());
    }


    @Test
    void completablePdsInvokesOnlyResultCallbacksAfterCompleting() {
        var pds = new PendingDataSource.CompletablePendingDataSource<>();
        pds.complete(dataSource);

        pds.whenAvailable(dataSourceConsumer);
        pds.whenFailed(failureConsumer);

        verify(dataSourceConsumer).accept(dataSource);
        verify(failureConsumer, never()).accept(any());
    }

    @Test
    void completablePdsInvokesOnlyFailureCallbacksAfterFailing() {
        var pds = new PendingDataSource.CompletablePendingDataSource<>();
        var ex = new Exception();
        pds.fail(ex);

        pds.whenAvailable(dataSourceConsumer);
        pds.whenFailed(failureConsumer);

        verify(dataSourceConsumer, never()).accept(any());
        verify(failureConsumer).accept(ex);
    }

    @Test
    void completablePdsInvokesExistingResultCallbacksAfterCompleting() {
        var pds = new PendingDataSource.CompletablePendingDataSource<>();

        pds.whenAvailable(dataSourceConsumer);
        pds.whenFailed(failureConsumer);

        pds.complete(dataSource);

        verify(dataSourceConsumer).accept(dataSource);
        verify(failureConsumer, never()).accept(any());
    }

    @Test
    void completablePdsInvokesExistingFailureCallbacksAfterFailing() {
        var pds = new PendingDataSource.CompletablePendingDataSource<>();
        pds.whenAvailable(dataSourceConsumer);
        pds.whenFailed(failureConsumer);

        var ex = new Exception();
        pds.fail(ex);

        verify(dataSourceConsumer, never()).accept(any());
        verify(failureConsumer).accept(ex);
    }

    @Test
    void failingCompletablePdsDoesNotTriggerUncaughtException() throws Exception {
        var pds = new PendingDataSource.CompletablePendingDataSource<>();

        var handler = mock(Thread.UncaughtExceptionHandler.class);
        ThreadTestUtils.withUncaughtExceptionHandler(handler, () -> pds.fail(new Exception()));

        verify(handler, never()).uncaughtException(any(), any());
    }

    @Test
    void failingCompletablePdsWithStagesPropagatesNoExceptions() throws Exception {
        var handler = ThreadTestUtils.withUncaughtExceptionHandler(mock(), () -> {
            var pds = new PendingDataSource.CompletablePendingDataSource<>();
            pds.addStage(cancellable);
            pds.addStage(future);

            pds.fail(new Exception());
        });

        verify(handler, never()).uncaughtException(any(), any());
    }

    @Test
    void throwingStageCloserPropagatesException() throws Exception {
        var ex = new RuntimeException();
        doThrow(ex).when(cancellable).cancel();
        var handler = ThreadTestUtils.withUncaughtExceptionHandler(mock(), () -> {
            var pds = new PendingDataSource.CompletablePendingDataSource<>();
            pds.addStage(cancellable);

            pds.cancel();
        });

        verify(handler).uncaughtException(any(), eq(ex));
    }
}
