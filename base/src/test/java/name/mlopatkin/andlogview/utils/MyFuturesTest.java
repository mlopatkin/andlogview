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

package name.mlopatkin.andlogview.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.base.MyThrowables;
import name.mlopatkin.andlogview.test.ThreadTestUtils;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.concurrent.CompletableFuture;

class MyFuturesTest {
    @Test
    void cancelByTouchesNoCancellableBeforeCancel() {
        Cancellable cancellable = mock();
        var canceller = new CompletableFuture<Void>();

        MyFutures.cancelBy(cancellable, canceller);

        verify(cancellable, never()).cancel();
    }

    @Test
    void cancelBTouchesNoFutureBeforeCancel() {
        var cancellable = new CompletableFuture<>();
        var canceller = new CompletableFuture<Void>();

        MyFutures.cancelBy(cancellable, canceller);

        assertThat(cancellable).isNotCancelled();
    }

    @Test
    void cancelByCancelsCancellableAfterCancel() {
        Cancellable cancellable = mock();
        var canceller = new CompletableFuture<Void>();

        MyFutures.cancelBy(cancellable, canceller);
        canceller.cancel(false);

        verify(cancellable).cancel();
    }

    @Test
    void cancelByCancelsFutureAfterCancel() {
        var cancellable = new CompletableFuture<>();
        var canceller = new CompletableFuture<Void>();

        MyFutures.cancelBy(cancellable, canceller);
        canceller.cancel(false);

        assertThat(cancellable).isCancelled();
    }

    @Test
    void exceptionsFromCancellableAreForwarded() throws Exception {
        Cancellable cancellable = mock();
        var ex = new RuntimeException("test");
        when(cancellable.cancel()).thenThrow(ex);

        var canceller = new CompletableFuture<Void>();

        MyFutures.cancelBy(cancellable, canceller);

        var handler = ThreadTestUtils.withUncaughtExceptionHandler(mock(), () -> {
            canceller.cancel(false);
        });

        verify(handler).uncaughtException(any(),
                ArgumentMatchers.argThat(argument -> ex.equals(MyThrowables.unwrapUninteresting(argument))));
    }

    @Test
    void cancelByCancelledFutureImmediatelyCancelsCancellable() {
        Cancellable cancellable = mock();
        var future = cancelledFuture();

        MyFutures.cancelBy(cancellable, future);

        verify(cancellable).cancel();
    }

    @Test
    void cancelByCancelledFutureImmediatelyCancelsFuture() {
        var cancellable = new CompletableFuture<>();
        var future = cancelledFuture();

        MyFutures.cancelBy(cancellable, future);

        assertThat(cancellable).isCancelled();
    }

    @Test
    void cancelByFailedFutureDoesNotCancel() {
        Cancellable cancellable1 = mock();
        var cancellable2 = new CompletableFuture<>();
        var future = CompletableFuture.failedFuture(new Exception("already failed"));

        MyFutures.cancelBy(cancellable1, future);
        MyFutures.cancelBy(cancellable2, future);

        verify(cancellable1, never()).cancel();
        assertThat(cancellable2).isNotCancelled();
    }

    @Test
    void attachingCancellableDoesNotPropagateExceptions() throws Exception {
        Cancellable cancellable1 = mock();
        var cancellable2 = new CompletableFuture<>();
        var future = CompletableFuture.failedFuture(new Exception("already failed"));

        var handler = ThreadTestUtils.withUncaughtExceptionHandler(mock(), () -> {
            MyFutures.cancelBy(cancellable1, future);
            MyFutures.cancelBy(cancellable2, future);
        });

        verify(handler, never()).uncaughtException(any(), any());
    }

    @Test
    void indirectCancellationPropagates() {
        Cancellable cancellable1 = mock();
        var cancellable2 = new CompletableFuture<>();
        var future = new CompletableFuture<String>();
        var indirect = future.thenApply(String::trim);

        MyFutures.cancelBy(cancellable1, indirect);
        MyFutures.cancelBy(cancellable2, indirect);

        future.cancel(false);

        verify(cancellable1).cancel();
        assertThat(cancellable2).isCancelled();
    }

    private static CompletableFuture<Void> cancelledFuture() {
        var future = new CompletableFuture<Void>();
        future.cancel(false);
        return future;
    }
}
