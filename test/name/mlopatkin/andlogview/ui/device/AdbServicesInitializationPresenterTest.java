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

package name.mlopatkin.andlogview.ui.device;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.test.ThreadTestUtils;

import com.google.common.util.concurrent.MoreExecutors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ExtendWith(MockitoExtension.class)
class AdbServicesInitializationPresenterTest {

    final CompletableFuture<AdbServices> servicesFuture = new CompletableFuture<>();

    @Mock
    AdbServicesInitializationPresenter.View view;

    @Mock
    Consumer<AdbServices> servicesConsumer;
    @Mock
    Consumer<Throwable> errorConsumer;

    @Test
    void nonInteractiveRequestCompletes() {
        var presenter = createPresenter();

        presenter.withAdbServices(servicesConsumer, errorConsumer);

        whenAdbInitSucceed();

        verify(servicesConsumer).accept(any());
        verify(errorConsumer, never()).accept(any());
    }

    @Test
    void nonInteractiveRequestHandlesErrors() {
        var presenter = createPresenter();

        presenter.withAdbServices(servicesConsumer, errorConsumer);

        whenAdbInitFailed();

        verify(servicesConsumer, never()).accept(any());
        verify(errorConsumer).accept(any());
    }

    @Test
    void interactiveRequestCompletes() {
        var presenter = createPresenter();

        presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);

        whenAdbInitSucceed();

        verify(servicesConsumer).accept(any());
        verify(errorConsumer, never()).accept(any());
    }

    @Test
    void interactiveRequestHandlesErrors() {
        var presenter = createPresenter();

        presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);

        whenAdbInitFailed();

        verify(servicesConsumer, never()).accept(any());
        verify(errorConsumer).accept(any());
    }

    @Test
    void nonInteractiveRequestTouchesNoView() {
        var presenter = createPresenter();

        presenter.withAdbServices(servicesConsumer, errorConsumer);

        verify(view, never()).showAdbLoadingProgress();
        verify(view, never()).hideAdbLoadingProgress();
    }

    @Test
    void interactiveRequestShowsProgress() {
        var presenter = createPresenter();

        presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);

        verify(view).showAdbLoadingProgress();
        verify(view, never()).hideAdbLoadingProgress();
    }

    @Test
    void failedInteractiveRequestHidesProgress() {
        var presenter = createPresenter();

        presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
        reset(view);

        whenAdbInitFailed();
        verify(view).hideAdbLoadingProgress();
    }

    @Test
    void completedInteractiveRequestHidesProgress() {
        var presenter = createPresenter();

        presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
        reset(view);

        whenAdbInitSucceed();
        verify(view).hideAdbLoadingProgress();
    }

    @Test
    void consequentInteractiveRequestCancelsUnfinished() {
        @SuppressWarnings("unchecked")
        Consumer<AdbServices> otherServicesConsumer = Mockito.mock();

        var presenter = createPresenter();

        presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
        presenter.withAdbServicesInteractive(otherServicesConsumer, errorConsumer);

        whenAdbInitSucceed();
        verify(servicesConsumer, never()).accept(any());
        verify(otherServicesConsumer).accept(any());
    }

    @Test
    void consequentInteractiveRequestDoNotAffectFinished() {
        @SuppressWarnings("unchecked")
        Consumer<AdbServices> otherServicesConsumer = Mockito.mock();

        var presenter = createPresenter();

        presenter.withAdbServicesInteractive(otherServicesConsumer, errorConsumer);
        whenAdbInitSucceed();

        presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
        verify(servicesConsumer).accept(any());
    }

    @Test
    void interactiveRequestCompletesAfterLoading() {
        var presenter = createPresenter();

        whenAdbInitSucceed();
        presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);

        verify(servicesConsumer).accept(any());
        verify(errorConsumer, never()).accept(any());
    }

    @Test
    void nonInteractiveRequestCompletesAfterLoading() {
        var presenter = createPresenter();

        whenAdbInitSucceed();
        presenter.withAdbServices(servicesConsumer, errorConsumer);

        verify(servicesConsumer).accept(any());
        verify(errorConsumer, never()).accept(any());
    }


    @Test
    void interactiveRequestHandlesFailureAfterLoading() {
        var presenter = createPresenter();

        whenAdbInitFailed();
        presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);

        verify(servicesConsumer, never()).accept(any());
        verify(errorConsumer).accept(any());
    }

    @Test
    void nonInteractiveRequestHandlesFailureAfterLoading() {
        var presenter = createPresenter();

        whenAdbInitFailed();
        presenter.withAdbServices(servicesConsumer, errorConsumer);

        verify(servicesConsumer, never()).accept(any());
        verify(errorConsumer).accept(any());
    }

    @Test
    void noProgressWhenRequestingInteractiveAfterLoading() {
        var presenter = createPresenter();

        whenAdbInitSucceed();
        presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);

        verify(view, never()).showAdbLoadingProgress();
        verify(view, never()).hideAdbLoadingProgress();
    }

    @Test
    void progressHidesIfTheConsumerThrows() throws Exception {
        var presenter = createPresenter();

        doThrow(RuntimeException.class).when(servicesConsumer).accept(any());

        ThreadTestUtils.withEmptyUncaughtExceptionHandler(() -> {
            presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
            whenAdbInitSucceed();
        });

        verify(view).hideAdbLoadingProgress();
    }

    @Test
    void consumerExceptionPropagatesToDefaultHandler() throws Exception {
        Thread.UncaughtExceptionHandler handler = mock();
        var presenter = createPresenter();

        doThrow(RuntimeException.class).when(servicesConsumer).accept(any());

        ThreadTestUtils.withUncaughtExceptionHandler(handler, () -> {
            presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
            whenAdbInitSucceed();
        });

        verify(handler).uncaughtException(any(), any());
    }

    @Test
    void cancellationDoesNotPropagateToExceptionHandler() throws Exception {
        Thread.UncaughtExceptionHandler handler = mock();
        var presenter = createPresenter();

        ThreadTestUtils.withUncaughtExceptionHandler(handler, () -> {
            presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
            presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
            whenAdbInitSucceed();
        });

        verify(handler, never()).uncaughtException(any(), any());
    }

    @Test
    void loadingFailuresDoNotPropagateToExceptionHandler() throws Exception {
        Thread.UncaughtExceptionHandler handler = mock();
        var presenter = createPresenter();

        ThreadTestUtils.withUncaughtExceptionHandler(handler, () -> {
            presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
            whenAdbInitFailed();
        });

        verify(handler, never()).uncaughtException(any(), any());
    }

    private void whenAdbInitFailed() {
        servicesFuture.completeExceptionally(new AdbException("Failed"));
    }

    private void whenAdbInitSucceed() {
        servicesFuture.complete(mock(AdbServices.class));
    }

    private AdbServicesInitializationPresenter createPresenter() {
        var mockBridge = mock(AdbServicesBridge.class);
        when(mockBridge.getAdbServicesAsync()).thenReturn(servicesFuture);
        return new AdbServicesInitializationPresenter(mockBridge, view, MoreExecutors.directExecutor());
    }
}
