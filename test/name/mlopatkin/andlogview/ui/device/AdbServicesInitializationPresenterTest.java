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


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import name.mlopatkin.andlogview.base.MyThrowables;
import name.mlopatkin.andlogview.base.concurrent.TestExecutor;
import name.mlopatkin.andlogview.device.AdbDeviceList;
import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.liblogcat.ddmlib.DeviceDisconnectedHandler;
import name.mlopatkin.andlogview.test.ThreadTestUtils;
import name.mlopatkin.andlogview.utils.Cancellable;
import name.mlopatkin.andlogview.utils.MyFutures;

import com.google.common.util.concurrent.MoreExecutors;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class AdbServicesInitializationPresenterTest {
    CompletableFuture<AdbServices> servicesFuture = new CompletableFuture<>();
    @Mock
    AdbServicesBridge mockBridge;

    FakeView view = new FakeView();

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    Consumer<AdbServices> servicesConsumer;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    Consumer<Throwable> errorConsumer;

    @Mock
    DeviceDisconnectedHandler disconnectedHandler;

    private AdbServicesInitializationPresenter presenter;

    @BeforeEach
    void setUp() {
        presenter = createPresenter();
    }

    @Test
    void interactiveRequestNotCompleteBeforeAdbLoad() {
        whenRequestedAdbInteractive();

        thenRequestNotCompleted();
    }

    @Test
    void interactiveRequestCompletesAfterAdbLoad() {
        whenRequestedAdbInteractive();

        whenAdbInitSucceed();

        thenRequestCompletedSuccessfully();
    }

    @Test
    void interactiveRequestFailsAfterAdbLoadFailure() {
        whenRequestedAdbInteractive();

        whenAdbInitFailed();

        thenRequestFailed();
    }

    @ParameterizedTest
    @MethodSource("failedSucceeded")
    void cancelledInteractiveRequestShowsNoMessagesIfAdbFails(AdbInitRef adbInit) {
        var result = whenRequestedAdbInteractive();

        result.cancel();

        adbInit.tryInitAdb(this);

        thenRequestCancelled();
        thenNoErrorIsShown();
    }

    @ParameterizedTest
    @MethodSource("failedSucceeded")
    void cancelledInteractiveRequestPropagatesNoExceptions(AdbInitRef adbInit) throws Exception {
        var result = whenRequestedAdbInteractive();

        var handler = ThreadTestUtils.withUncaughtExceptionHandler(mock(), () -> {
            result.cancel();

            adbInit.tryInitAdb(this);
        });

        verify(handler, never()).uncaughtException(any(), any());
    }

    @Test
    void nonInteractiveRequestShowsAndHidesNoProgress() {
        whenRequestedAdbDeviceList();

        thenNoProgressIsShown();
        thenNoProgressIsHidden();
    }

    @ParameterizedTest
    @MethodSource("allInteractiveRequests")
    void interactiveRequestShowsProgress(ServiceRequest request) {
        whenRequestedAdbWith(request);

        thenProgressIsShown();
    }

    @ParameterizedTest
    @MethodSource("allInteractiveRequests")
    void failedInteractiveRequestHidesProgress(ServiceRequest request) {
        whenRequestedAdbWith(request);
        whenAdbInitFailed();

        thenProgressIsHidden();
    }

    @Test
    void cancelledInteractiveRequestHidesProgress() {
        var result = whenRequestedAdbInteractive();
        result.cancel();

        thenProgressIsHidden();
    }

    @ParameterizedTest
    @MethodSource("allInteractiveRequests")
    void completedInteractiveRequestHidesProgress(ServiceRequest request) {
        whenRequestedAdbWith(request);
        whenAdbInitSucceed();

        thenProgressIsHidden();
    }

    @Test
    void requestAfterLoadingCompletes() {
        whenAdbInitSucceed();
        whenRequestedAdbInteractive();

        thenRequestCompletedSuccessfully();
    }

    @Test
    void requestAfterLoadingFailureFails() {
        whenAdbInitFailed();
        whenRequestedAdbInteractive();

        thenRequestFailed();
    }

    @ParameterizedTest
    @MethodSource("allInteractiveRequests")
    void noProgressShownForInteractiveRequestsAfterLoading(ServiceRequest request) {
        whenAdbInitSucceed();
        whenRequestedAdbWith(request);

        thenNoProgressIsShown();
        thenNoProgressIsHidden();
    }

    @Test
    void progressHiddenIfTheConsumerThrows() throws Exception {
        givenServiceConsumerThrows();

        ThreadTestUtils.withEmptyUncaughtExceptionHandler(() -> {
            whenRequestedAdbInteractive();
            whenAdbInitSucceed();
        });

        thenProgressIsHidden();
    }

    @Test
    void consumerExceptionPropagatesToDefaultHandler() throws Exception {
        givenServiceConsumerThrows();

        var handler = ThreadTestUtils.withUncaughtExceptionHandler(mock(), () -> {
            whenRequestedAdbInteractive();
            whenAdbInitSucceed();
        });

        verify(handler).uncaughtException(any(), any());
    }

    @Test
    void cancellationOfInteractiveRequestDoesNotPropagateToExceptionHandler() throws Exception {
        var handler = ThreadTestUtils.withUncaughtExceptionHandler(mock(),
                () -> whenRequestedAdbInteractive().cancel());

        verify(handler, never()).uncaughtException(any(), any());
    }

    @ParameterizedTest
    @MethodSource("allAdbRequests")
    void loadingFailuresDoNotPropagateToExceptionHandler(ServiceRequest request) throws Exception {
        var handler = ThreadTestUtils.withUncaughtExceptionHandler(mock(), () -> {
            whenRequestedAdbWith(request);
            whenAdbInitFailed();
        });

        verify(handler, never()).uncaughtException(any(), any());
    }

    @ParameterizedTest
    @MethodSource("allAdbRequests")
    void loadingFailureIsShown(ServiceRequest request) {
        whenRequestedAdbWith(request);

        whenAdbInitFailed();

        thenErrorIsShown();
    }

    @ParameterizedTest
    @MethodSource("nonRestartRequests")
    void multipleRequestsCauseNoAdditionalErrorsToShow(ServiceRequest request) {
        givenInitialState(() -> {
            whenRequestedAdbWith(request);
            whenAdbInitFailed();
        });

        whenRequestedAdbWith(request);

        thenNoErrorIsShown();
    }

    @ParameterizedTest
    @MethodSource("allAdbRequests")
    void restartShowsAdditionalError(ServiceRequest initialRequest) {
        givenInitialState(() -> {
            whenRequestedAdbWith(initialRequest);
            whenAdbInitFailed();
        });

        whenRequestedAdbWith(restartRequest());

        thenErrorIsShown();
    }

    @ParameterizedTest
    @MethodSource("allAdbRequests")
    void restartShowsNoErrorIfSucceedsAfterInitialFailure(ServiceRequest initialRequest) {
        givenInitialState(() -> {
            whenRequestedAdbWith(initialRequest);
            whenAdbInitFailed();
        });

        withNewResultAfterReload();
        whenRequestedAdbWith(restartRequest());
        whenAdbInitSucceed();

        thenNoErrorIsShown();
        thenProgressIsHidden();
    }

    @Test
    void requestAfterCancelledInteractiveCanSucceed() {
        whenRequestedAdbInteractive().cancel();
        Mockito.<Object>reset(servicesConsumer, errorConsumer);

        whenRequestedAdbInteractive();

        whenAdbInitSucceed();

        thenRequestCompletedSuccessfully();
    }

    @ParameterizedTest
    @MethodSource("allInteractiveRequests")
    void requestAfterCancelledStillShowsProgress(ServiceRequest request) {
        // Cancellation may hide progress asynchronously, so a proper executor is necessary.
        var testExecutor = new TestExecutor();
        presenter = createPresenter(testExecutor);

        var step1 = whenRequestedAdbInteractive();
        testExecutor.flush();

        step1.cancel();
        whenRequestedAdbWith(request);

        testExecutor.flush();
        thenProgressIsShown();
    }

    @ParameterizedTest
    @MethodSource("allInteractiveRequests")
    void interactiveRequestCanSucceedIfViewOpensModalDialog(ServiceRequest request) {
        view.withModalLoop(cancellationAction -> {
            whenAdbInitSucceed();

            thenProgressIsHidden();
        });

        whenRequestedAdbWith(request);

        thenProgressIsHidden();
    }

    @ParameterizedTest
    @MethodSource("allInteractiveRequests")
    void interactiveRequestCanFailIfViewOpensModalDialog(ServiceRequest request) {
        view.withModalLoop(cancellationAction -> {
            whenAdbInitFailed();

            thenProgressIsHidden();
        });

        whenRequestedAdbWith(request);

        thenProgressIsHidden();
        thenErrorIsShown();
    }

    @ParameterizedTest
    @MethodSource("allCancellableInteractiveRequests")
    void interactiveRequestCanBeCancelledByDialog(ServiceRequest request) {
        view.withModalLoop(cancellationAction -> {
            cancellationAction.run();

            thenProgressIsHidden();
        });

        whenRequestedAdbWith(request);

        thenProgressIsHidden();
        thenNoErrorIsShown();
    }

    @Test
    void afterCancellingProgressDialogRequestCancelled() {
        view.withModalLoop(cancellationAction -> {
            cancellationAction.run();

            thenProgressIsHidden();
        });

        whenRequestedAdbInteractive();

        thenRequestCancelled();
    }

    @ParameterizedTest
    @MethodSource("allCancellableInteractiveRequests")
    void afterCancellingProgressNoErrorIsShown(ServiceRequest request) {
        view.withModalLoop(cancellationAction -> {
            cancellationAction.run();

            whenAdbInitFailed();

            thenProgressIsHidden();
        });

        whenRequestedAdbWith(request);

        thenProgressIsHidden();
        thenNoErrorIsShown();
    }

    @Test
    void afterCancellingProgressDialogNewRequestCanComplete() {
        givenInitialState(() -> {
            view.withModalLoop(cancellationAction -> {
                cancellationAction.run();

                whenAdbInitSucceed();
            });
            whenRequestedAdbInteractive();
        });

        whenRequestedAdbInteractive();

        thenRequestCompletedSuccessfully();
    }

    @Test
    void afterCancellingProgressDialogNewRequestCanFail() {
        givenInitialState(() -> {
            view.withModalLoop(cancellationAction -> {
                cancellationAction.run();

                whenAdbInitFailed();
            });
            whenRequestedAdbInteractive();
        });

        whenRequestedAdbInteractive();

        thenRequestFailed();
        thenErrorIsShown();
    }

    @Test
    void afterCompletingRestartWithModalDialogNewRestartShowsDialogAgain() {
        givenInitialState(() -> {
            view.withModalLoop(cancellationAction -> whenAdbInitSucceed());
            whenRequestedAdbWith(restartRequest());
        });
        withNewResultAfterReload();

        view.withModalLoop(cancellationAction -> whenAdbInitSucceed());
        whenRequestedAdbWith(restartRequest());

        thenProgressIsHidden();
    }

    @Test
    void stopsBridgeWithDisconnectMessagesSuppressed() {
        whenRequestedAdbWith(restartRequest());

        var inOrder = inOrder(mockBridge, disconnectedHandler);
        inOrder.verify(disconnectedHandler).suppressDialogs();
        inOrder.verify(disconnectedHandler, never()).resumeDialogs();
        inOrder.verify(mockBridge).stopAdb();
    }

    @Test
    void restartShowsNonCancellableProgress() {
        whenRequestedAdbWith(restartRequest());

        thenNonCancellableProgressIsShown();
    }

    @Test
    void interactiveRequestShowsCancellableProgress() {
        whenRequestedAdbWith(interactiveRequest());

        thenCancellableProgressIsShown();
    }

    @Test
    @SuppressWarnings("Convert2MethodRef")
    void interactiveRequestAfterHiddenRestartShowsProgressDialogAndCanComplete() {
        givenInitialState(() -> {
            view.withModalLoop(userHideAction -> userHideAction.run());
            whenRequestedAdbWith(restartRequest());
        });

        whenRequestedAdbWith(interactiveRequest());
        whenAdbInitSucceed();

        thenCancellableProgressIsShown();
        thenRequestCompletedSuccessfully();
    }

    @Test
    @SuppressWarnings("Convert2MethodRef")
    void hiddenRestartShowsProgressDialogAndCanComplete() {
        givenInitialState(() -> {
            view.withModalLoop(userHideAction -> userHideAction.run());
            whenRequestedAdbWith(restartRequest());
        });

        whenRequestedAdbWith(interactiveRequest());
        whenAdbInitSucceed();

        thenCancellableProgressIsShown();
        thenRequestCompletedSuccessfully();
    }

    private AdbServicesInitializationPresenter createPresenter() {
        return createPresenter(MoreExecutors.directExecutor());
    }

    private AdbServicesInitializationPresenter createPresenter(Executor uiExecutor) {
        lenient().when(mockBridge.getAdbServicesAsync())
                .thenAnswer(invocation -> servicesFuture.thenApply(Function.identity()));
        lenient().when(mockBridge.prepareAdbDeviceList(any()))
                .thenAnswer(invocation -> {
                    Consumer<? super Throwable> failureHandler = invocation.getArgument(0);
                    servicesFuture
                            .handle(MyFutures.errorHandler(failureHandler))
                            .exceptionally(MyFutures::uncaughtException);
                    return mock(AdbDeviceList.class);
                });
        return new AdbServicesInitializationPresenter(view, mockBridge, uiExecutor, disconnectedHandler);
    }

    private void withNewResultAfterReload() {
        lenient().doAnswer(invocation -> {
            servicesFuture = new CompletableFuture<>();
            return null;
        }).when(mockBridge).stopAdb();
    }

    private void whenAdbInitFailed() {
        servicesFuture.completeExceptionally(new AdbException("Failed"));
    }

    private void whenAdbInitSucceed() {
        servicesFuture.complete(mock(AdbServices.class));
    }

    private void whenRequestedAdbWith(ServiceRequest request) {
        request.whenRunWithPresenter(presenter, servicesConsumer, errorConsumer);
    }

    private Cancellable whenRequestedAdbInteractive() {
        return whenRequestedAdbInteractive(servicesConsumer);
    }

    private Cancellable whenRequestedAdbInteractive(Consumer<? super AdbServices> servicesConsumer) {
        return presenter.withAdbServicesInteractive(servicesConsumer, errorConsumer);
    }

    private void whenRequestedAdbDeviceList() {
        presenter.withAdbDeviceList();
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

    private void thenRequestCancelled() {
        verify(servicesConsumer, never()).accept(any());
        verify(errorConsumer).accept(ArgumentMatchers.argThat(
                failure -> MyThrowables.unwrapUninteresting(failure) instanceof CancellationException));
    }

    private void thenNoProgressIsShown() {
        view.assertNoProgressAppeared();
    }

    private void thenNoProgressIsHidden() {
        view.assertNoProgressAppeared();
    }

    private void thenProgressIsShown() {
        view.assertShowsProgress();
    }

    private void thenCancellableProgressIsShown() {
        view.assertProgressAppeared();
        view.assertProgressIsCancellable();
    }

    private void thenNonCancellableProgressIsShown() {
        view.assertProgressAppeared();
        view.assertProgressIsNotCancellable();
    }

    private void thenProgressIsHidden() {
        view.assertProgressAppeared();
        view.assertShowsNoProgress();
    }

    private void thenErrorIsShown() {
        view.assertShowsError();
    }

    private void thenNoErrorIsShown() {
        view.assertShowsNoError();
    }

    @FunctionalInterface
    interface ServiceRequest {
        void whenRunWithPresenter(
                AdbServicesInitializationPresenter presenter,
                Consumer<? super AdbServices> servicesConsumer,
                Consumer<? super Throwable> errorConsumer);
    }

    static ServiceRequest interactiveRequest() {
        return testConsumer(AdbServicesInitializationPresenter::withAdbServicesInteractive,
                "withAdbServicesInteractive");
    }

    private static ServiceRequest restartRequest() {
        return testConsumer((presenter, servicesConsumer, errorConsumer) -> presenter.restartAdb(), "restartAdb");
    }

    private static ServiceRequest deviceListRequest() {
        return testConsumer((presenter, servicesConsumer, errorConsumer) -> presenter.withAdbDeviceList(),
                "withAdbDeviceList");
    }

    static Stream<ServiceRequest> allInteractiveRequests() {
        return Stream.of(
                interactiveRequest(),
                restartRequest()
        );
    }

    static Stream<ServiceRequest> allCancellableInteractiveRequests() {
        return Stream.of(interactiveRequest());
    }

    static Stream<ServiceRequest> nonRestartRequests() {
        return Stream.of(
                interactiveRequest(),
                deviceListRequest()
        );
    }

    static Stream<ServiceRequest> allAdbRequests() {
        return Stream.concat(nonRestartRequests(), Stream.of(
                restartRequest()));
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

    @FunctionalInterface
    interface AdbInitRef {
        void tryInitAdb(AdbServicesInitializationPresenterTest test);
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

    private static class FakeView implements AdbServicesInitializationPresenter.View {
        private boolean progressAppeared;
        private boolean progressCancellable;
        private boolean showsProgress;
        private @Nullable String showsError;
        private Consumer<? super Runnable> loop = r -> {};

        @Override
        public void showAdbLoadingProgress(boolean isCancellable, Runnable userHideAction) {
            progressAppeared = true;
            showsProgress = true;
            progressCancellable = isCancellable;
            loop.accept(userHideAction);
            loop = r -> {};
        }

        @Override
        public void hideAdbLoadingProgress() {
            showsProgress = false;
        }

        @Override
        public void showAdbLoadingError(String failureReason) {
            showsError = Objects.requireNonNull(failureReason);
        }

        public void assertShowsProgress() {
            assertThat(showsProgress).as("shows progress").isTrue();
        }

        public void assertProgressAppeared() {
            assertThat(progressAppeared).as("progress appeared").isTrue();
        }

        public void assertNoProgressAppeared() {
            assertThat(progressAppeared).as("no progress appeared").isFalse();
        }

        public void assertShowsNoProgress() {
            assertThat(showsProgress).as("shows no progress").isFalse();
        }

        public void assertShowsError() {
            assertThat(showsError).as("shows error").isNotNull();
        }

        public void assertShowsNoError() {
            assertThat(showsError).as("shows no error").isNull();
        }

        public void assertProgressIsCancellable() {
            assertThat(progressCancellable).isTrue();
        }

        public void assertProgressIsNotCancellable() {
            assertThat(progressCancellable).isFalse();
        }

        public void reset() {
            showsError = null;
            showsProgress = false;
            progressAppeared = false;
            progressCancellable = false;
        }

        public void withModalLoop(Consumer<? super Runnable> action) {
            loop = action;
        }
    }

    private void givenInitialState(Runnable action) {
        action.run();
        view.reset();
        Mockito.<Object>reset(servicesConsumer, errorConsumer);
    }
}
