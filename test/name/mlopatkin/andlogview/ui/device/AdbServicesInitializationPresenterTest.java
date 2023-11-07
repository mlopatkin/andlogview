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
import static org.mockito.Mockito.inOrder;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class AdbServicesInitializationPresenterTest {
    final CompletableFuture<AdbServices> servicesFuture = new CompletableFuture<>();
    @Mock
    AdbServicesInitializationPresenter.View view;
    @Mock
    Consumer<AdbServices> servicesConsumer;
    @Mock
    Consumer<Throwable> errorConsumer;


    @ParameterizedTest
    @MethodSource("interactiveAndNonInteractive")
    void servicesRequestNotCompleteBeforeAdbLoad(ServiceRequest request) {
        whenRunWithPresenter(request);

        thenRequestNotCompleted();
    }

    @ParameterizedTest
    @MethodSource("interactiveAndNonInteractive")
    void servicesRequestCompletesAfterAdbLoad(ServiceRequest request) {
        whenRunWithPresenter(request);

        whenAdbInitSucceed();

        thenRequestCompletedSuccessfully();
    }

    @ParameterizedTest
    @MethodSource("interactiveAndNonInteractive")
    void servicesRequestFailsAfterAdbLoadFailure(ServiceRequest request) {
        whenRunWithPresenter(request);

        whenAdbInitFailed();

        thenRequestFailed();
    }

    @Test
    void nonInteractiveRequestShowsAndHidesNoProgress() {
        whenNonInteractiveRunWithPresenter();

        thenNoProgressIsShown();
        thenNoProgressIsHidden();
    }

    @Test
    void interactiveRequestShowsProgress() {
        whenInteractiveRunWithPresenter();

        thenProgressIsShown();
    }

    @Test
    void failedInteractiveRequestHidesProgress() {
        whenInteractiveRunWithPresenter();
        whenAdbInitFailed();

        thenProgressIsHidden();
    }

    @Test
    void completedInteractiveRequestHidesProgress() {
        whenInteractiveRunWithPresenter();
        whenAdbInitSucceed();

        thenProgressIsHidden();
    }

    @Test
    void consequentInteractiveRequestCancelsUnfinished() {
        Consumer<AdbServices> initialServicesConsumer = mockConsumer();

        var presenter = createPresenter();

        presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
        presenter.withAdbServicesInteractive(initialServicesConsumer, errorConsumer);

        whenAdbInitSucceed();
        verify(servicesConsumer, never()).accept(any());
        verify(initialServicesConsumer).accept(any());
    }

    @Test
    void consequentInteractiveRequestDoNotAffectFinished() {
        Consumer<AdbServices> otherServicesConsumer = mockConsumer();

        var presenter = createPresenter();

        presenter.withAdbServicesInteractive(otherServicesConsumer, errorConsumer);
        whenAdbInitSucceed();

        presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
        verify(servicesConsumer).accept(any());
    }

    @ParameterizedTest
    @MethodSource("interactiveAndNonInteractive")
    void requestAfterLoadingCompletes(ServiceRequest request) {
        whenAdbInitSucceed();
        whenRunWithPresenter(request);

        thenRequestCompletedSuccessfully();
    }

    @ParameterizedTest
    @MethodSource("interactiveAndNonInteractive")
    void requestAfterLoadingFailureFails(ServiceRequest request) {
        whenAdbInitFailed();
        whenRunWithPresenter(request);

        thenRequestFailed();
    }

    @Test
    void noProgressShownForInteractiveRequestsAfterLoading() {
        whenAdbInitSucceed();
        whenInteractiveRunWithPresenter();

        thenNoProgressIsShown();
        thenNoProgressIsHidden();
    }

    @Test
    void progressHiddenIfTheConsumerThrows() throws Exception {
        givenServiceConsumerThrows();

        ThreadTestUtils.withEmptyUncaughtExceptionHandler(() -> {
            whenInteractiveRunWithPresenter();
            whenAdbInitSucceed();
        });

        thenProgressIsHidden();
    }

    @ParameterizedTest
    @MethodSource("interactiveAndNonInteractive")
    void consumerExceptionPropagatesToDefaultHandler(ServiceRequest request) throws Exception {
        givenServiceConsumerThrows();

        Thread.UncaughtExceptionHandler handler = mock();
        ThreadTestUtils.withUncaughtExceptionHandler(handler, () -> {
            whenRunWithPresenter(request);
            whenAdbInitSucceed();
        });

        verify(handler).uncaughtException(any(), any());
    }

    @Test
    void cancellationOfInteractiveRequestDoesNotPropagateToExceptionHandler() throws Exception {
        Thread.UncaughtExceptionHandler handler = mock();
        var presenter = createPresenter();

        ThreadTestUtils.withUncaughtExceptionHandler(handler, () -> {
            presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
            presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
            whenAdbInitSucceed();
        });

        verify(handler, never()).uncaughtException(any(), any());
    }

    @ParameterizedTest
    @MethodSource("interactiveAndNonInteractive")
    void loadingFailuresDoNotPropagateToExceptionHandler(ServiceRequest request) throws Exception {
        Thread.UncaughtExceptionHandler handler = mock();
        ThreadTestUtils.withUncaughtExceptionHandler(handler, () -> {
            whenRunWithPresenter(request);
            whenAdbInitFailed();
        });

        verify(handler, never()).uncaughtException(any(), any());
    }

    @ParameterizedTest
    @MethodSource("interactiveAndNonInteractive")
    void loadingFailureIsShown(ServiceRequest request) {
        whenRunWithPresenter(request);

        whenAdbInitFailed();

        thenErrorIsShown();
    }

    @Test
    void multipleRequestsCauseNoAdditionalErrorsToShow() {
        var presenter = createPresenter();

        presenter.withAdbServices(servicesConsumer, errorConsumer);
        whenAdbInitFailed();
        reset(view);

        presenter.withAdbServices(servicesConsumer, errorConsumer);

        verify(view, never()).showAdbLoadingError();
    }

    private AdbServicesInitializationPresenter createPresenter() {
        var mockBridge = mock(AdbServicesBridge.class);
        when(mockBridge.getAdbServicesAsync()).thenReturn(servicesFuture);
        return new AdbServicesInitializationPresenter(mockBridge, view, MoreExecutors.directExecutor());
    }

    private void whenAdbInitFailed() {
        servicesFuture.completeExceptionally(new AdbException("Failed"));
    }

    private void whenAdbInitSucceed() {
        servicesFuture.complete(mock(AdbServices.class));
    }

    private void whenRunWithPresenter(ServiceRequest request) {
        request.whenRunWithPresenter(createPresenter(), servicesConsumer, errorConsumer);
    }

    private void whenInteractiveRunWithPresenter() {
        whenRunWithPresenter(AdbServicesInitializationPresenter::withAdbServicesInteractive);
    }

    private void whenNonInteractiveRunWithPresenter() {
        whenRunWithPresenter(AdbServicesInitializationPresenter::withAdbServices);
    }

    private void givenServiceConsumerThrows() {
        doThrow(RuntimeException.class).when(servicesConsumer).accept(any());
    }

    private void thenRequestNotCompleted() {
        verify(servicesConsumer, never()).accept(any());
        verify(errorConsumer, never()).accept(any());
    }

    private void thenRequestCompletedSuccessfully() {
        verify(servicesConsumer).accept(any());
        verify(errorConsumer, never()).accept(any());
    }

    private void thenRequestFailed() {
        verify(servicesConsumer, never()).accept(any());
        verify(errorConsumer).accept(any());
    }

    private void thenNoProgressIsShown() {
        verify(view, never()).showAdbLoadingProgress();
    }

    private void thenNoProgressIsHidden() {
        verify(view, never()).showAdbLoadingProgress();
    }

    private void thenProgressIsShown() {
        verify(view).showAdbLoadingProgress();
        verify(view, never()).hideAdbLoadingProgress();
    }

    private void thenProgressIsHidden() {
        var inOrder = inOrder(view);
        inOrder.verify(view).hideAdbLoadingProgress();
        inOrder.verify(view, never()).showAdbLoadingProgress();
    }

    private void thenErrorIsShown() {
        verify(view).showAdbLoadingError();
    }

    @FunctionalInterface
    interface ServiceRequest {
        void whenRunWithPresenter(
                AdbServicesInitializationPresenter presenter,
                Consumer<? super AdbServices> servicesConsumer,
                Consumer<? super Throwable> errorConsumer);
    }


    static Stream<ServiceRequest> interactiveAndNonInteractive() {
        return Stream.of(
                testConsumer(
                        AdbServicesInitializationPresenter::withAdbServices,
                        "withAdbServices"),
                testConsumer(AdbServicesInitializationPresenter::withAdbServicesInteractive,
                        "withAdbServicesInteractive")
        );
    }

    private static ServiceRequest testConsumer(ServiceRequest methodRef, String name) {
        return new ServiceRequest() {
            @Override
            public void whenRunWithPresenter(AdbServicesInitializationPresenter presenter,
                    Consumer<? super AdbServices> servicesConsumer, Consumer<? super Throwable> errorConsumer) {
                methodRef.whenRunWithPresenter(presenter, servicesConsumer, errorConsumer);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T> Consumer<T> mockConsumer() {
        return Mockito.mock();
    }
}
