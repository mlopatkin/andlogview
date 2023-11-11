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


import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import name.mlopatkin.andlogview.base.concurrent.TestExecutor;
import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.test.ThreadTestUtils;
import name.mlopatkin.andlogview.utils.Cancellable;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class AdbServicesInitializationPresenterTest {
    final CompletableFuture<AdbServices> servicesFuture = new CompletableFuture<>();
    @Mock
    AdbServicesInitializationPresenter.View view;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    Consumer<AdbServices> servicesConsumer;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    Consumer<Throwable> errorConsumer;

    private AdbServicesInitializationPresenter presenter;

    @BeforeEach
    void setUp() {
        presenter = createPresenter();
    }

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

    @ParameterizedTest
    @MethodSource("interactiveNonInteractiveFailedSucceed")
    void cancelledRequestShowsNoMessagesIfAdbFails(ServiceRequest request, AdbInitRef adbInit) {
        var result = whenRunWithPresenter(request);

        result.cancel();

        adbInit.tryInitAdb(this);

        thenRequestNotCompleted();
        thenNoErrorIsShown();
    }

    @ParameterizedTest
    @MethodSource("interactiveNonInteractiveFailedSucceed")
    void cancelledRequestPropagatesNoExceptions(ServiceRequest request, AdbInitRef adbInit) throws Exception {
        var result = whenRunWithPresenter(request);

        Thread.UncaughtExceptionHandler handler = mock();
        ThreadTestUtils.withUncaughtExceptionHandler(handler, () -> {
            result.cancel();

            adbInit.tryInitAdb(this);
        });

        verify(handler, never()).uncaughtException(any(), any());
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
    void cancelledInteractiveRequestHidesProgress() {
        var result = whenInteractiveRunWithPresenter();
        result.cancel();

        thenProgressIsHidden();
    }

    @Test
    void completedInteractiveRequestHidesProgress() {
        whenInteractiveRunWithPresenter();
        whenAdbInitSucceed();

        thenProgressIsHidden();
    }

    @Test
    void consequentInteractiveRequestDoNotAffectFinished() {
        Consumer<AdbServices> otherServicesConsumer = mockConsumer();

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
        presenter.withAdbServices(servicesConsumer, errorConsumer);
        whenAdbInitFailed();
        reset(view);

        presenter.withAdbServices(servicesConsumer, errorConsumer);

        verify(view, never()).showAdbLoadingError();
    }

    @ParameterizedTest
    @MethodSource("interactiveAndNonInteractive")
    void requestAfterCancelledOneSucceed(ServiceRequest request) {
        whenRunWithPresenter(request).cancel();
        reset(view, servicesConsumer, errorConsumer);

        whenRunWithPresenter(request);

        whenAdbInitSucceed();

        thenRequestCompletedSuccessfully();
    }

    @Test
    void requestAfterCancelledStillShowsProgress() {
        // Cancellation may hide progress asynchronously, so a proper executor is necessary.
        var testExecutor = new TestExecutor();
        presenter = createPresenter(testExecutor);

        var step1 = whenInteractiveRunWithPresenter();
        testExecutor.flush();

        step1.cancel();
        whenInteractiveRunWithPresenter();

        testExecutor.flush();
        thenProgressIsShown();
    }

    private AdbServicesInitializationPresenter createPresenter() {
        return createPresenter(MoreExecutors.directExecutor());
    }

    private AdbServicesInitializationPresenter createPresenter(Executor uiExecutor) {
        var mockBridge = mock(AdbServicesBridge.class);
        lenient().when(mockBridge.getAdbServicesAsync())
                .thenAnswer(invocation -> servicesFuture.thenApply(Function.identity()));
        return new AdbServicesInitializationPresenter(mockBridge, view, uiExecutor);
    }

    private void whenAdbInitFailed() {
        servicesFuture.completeExceptionally(new AdbException("Failed"));
    }

    private void whenAdbInitSucceed() {
        servicesFuture.complete(mock(AdbServices.class));
    }

    private Cancellable whenRunWithPresenter(ServiceRequest request) {
        return request.whenRunWithPresenter(presenter, servicesConsumer, errorConsumer);
    }

    private Cancellable whenInteractiveRunWithPresenter() {
        return whenRunWithPresenter(AdbServicesInitializationPresenter::withAdbServicesInteractive);
    }

    private Cancellable whenNonInteractiveRunWithPresenter() {
        return whenRunWithPresenter(AdbServicesInitializationPresenter::withAdbServices);
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
        var order = inOrder(view);
        order.verify(view).showAdbLoadingProgress();
        order.verify(view, never()).hideAdbLoadingProgress();
    }

    private void thenProgressIsHidden() {
        var inOrder = inOrder(view);
        inOrder.verify(view).hideAdbLoadingProgress();
        inOrder.verify(view, never()).showAdbLoadingProgress();
    }

    private void thenErrorIsShown() {
        verify(view).showAdbLoadingError();
    }

    private void thenNoErrorIsShown() {
        verify(view, never()).showAdbLoadingError();
    }

    @FunctionalInterface
    interface ServiceRequest {
        Cancellable whenRunWithPresenter(
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

    static Stream<AdbInitRef> failedSucceeded() {
        return Stream.of(
                testConsumer(
                        AdbServicesInitializationPresenterTest::whenAdbInitSucceed,
                        "whenAdbInitSucceed"),
                testConsumer(AdbServicesInitializationPresenterTest::whenAdbInitFailed,
                        "whenAdbInitFailed")
        );
    }

    static Stream<Arguments> interactiveNonInteractiveFailedSucceed() {
        return Lists.cartesianProduct(
                interactiveAndNonInteractive().collect(Collectors.toList()),
                failedSucceeded().collect(Collectors.toList())).stream().map(list -> arguments(list.toArray()));
    }

    private static ServiceRequest testConsumer(ServiceRequest methodRef, String name) {
        return new ServiceRequest() {
            @Override
            public Cancellable whenRunWithPresenter(AdbServicesInitializationPresenter presenter,
                    Consumer<? super AdbServices> servicesConsumer, Consumer<? super Throwable> errorConsumer) {
                return methodRef.whenRunWithPresenter(presenter, servicesConsumer, errorConsumer);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    @FunctionalInterface
    interface AdbInitRef  {
        void tryInitAdb(AdbServicesInitializationPresenterTest test);
    }

    private static AdbInitRef testConsumer(AdbInitRef runnable, String name) {
        return new AdbInitRef() {

            @Override
            public void tryInitAdb(AdbServicesInitializationPresenterTest test) {
                runnable.tryInitAdb(test);
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
