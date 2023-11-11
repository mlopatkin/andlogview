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

import static name.mlopatkin.andlogview.utils.MyFutures.consumingHandler;
import static name.mlopatkin.andlogview.utils.MyFutures.skipCancellations;

import name.mlopatkin.andlogview.AppExecutors;
import name.mlopatkin.andlogview.utils.LazyInstance;
import name.mlopatkin.andlogview.utils.MyFutures;

import dagger.Lazy;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * The presenter handles ADB initialization and shows progress and errors to the user.
 */
public class AdbServicesInitializationPresenter {
    /**
     * View interface to show ADB initialization to the user.
     */
    public interface View {
        /**
         * Show the user that ADB services aren't ready yet, but they are being loading.
         */
        void showAdbLoadingProgress();

        /**
         * Show the user that ADB services are now ready.
         */
        void hideAdbLoadingProgress();

        /**
         * Show the user that ADB services failed to load. This is only called once per presenter lifetime.
         */
        void showAdbLoadingError();
    }

    private final View view;
    private final Executor uiExecutor;

    private final Lazy<CompletableFuture<AdbServices>> services;
    private boolean hasShownErrorMessage;

    private @Nullable CompletableFuture<?> currentInteractiveRequest;

    @Inject
    AdbServicesInitializationPresenter(AdbServicesBridge bridge, View view,
            @Named(AppExecutors.UI_EXECUTOR) Executor uiExecutor) {
        this.view = view;
        this.uiExecutor = uiExecutor;
        this.services = LazyInstance.lazy(() -> initServicesAsync(bridge));
    }

    /**
     * Initializes the adb services and executes the action. This method should be used when the user doesn't directly
     * trigger the action, so no progress indication is needed. The actions are executed on the UI executor.
     *
     * @param action the action to execute
     * @param failureHandler the failure handler
     */
    public void withAdbServices(Consumer<? super AdbServices> action, Consumer<? super Throwable> failureHandler) {
        getServicesAsync().handleAsync(
                        consumingHandler(action, failureHandler),
                        uiExecutor)
                .exceptionally(MyFutures::uncaughtException);
    }

    /**
     * Initializes the adb services and executes the action. This method should be used when the user triggers the
     * action so the delay should be indicated somehow. The actions are executed on the UI executor.
     *
     * @param action the action to execute
     * @param failureHandler the failure handler
     */
    public void withAdbServicesInteractive(Consumer<? super AdbServices> action,
            Consumer<? super Throwable> failureHandler) {
        if (currentInteractiveRequest != null) {
            // Something else tried to open ADB-dependent window before us. We should cancel that action, because the
            // user changed their mind.
            currentInteractiveRequest.cancel(false);
            currentInteractiveRequest = null;
        }

        var future = getServicesAsync();
        if (!future.isDone()) {
            // Only bother with setting cursors if the ADB is not yet initialized.
            view.showAdbLoadingProgress();
            future = future.whenCompleteAsync(
                    (services, th) -> view.hideAdbLoadingProgress(),
                    uiExecutor);
        }
        // TODO(mlopatkin) Should we always show a failure message if the ADB fails/is failed for the interactive
        //  request?
        var cancellableStep = future.handleAsync(consumingHandler(action, failureHandler), uiExecutor);
        // Make sure that all runtime errors are not ignored, except cancellations.
        cancellableStep
                .exceptionally(skipCancellations())
                .exceptionally(MyFutures::uncaughtException);
        currentInteractiveRequest = cancellableStep;
    }

    private CompletableFuture<AdbServices> getServicesAsync() {
        var servicesFuture = services.get();
        if (isCompletedSuccessfully(servicesFuture) || hasShownErrorMessage) {
            return servicesFuture;
        }
        servicesFuture = servicesFuture.whenCompleteAsync(this::handleAdbErrorIfNeeded, uiExecutor);
        return servicesFuture;
    }

    private void handleAdbErrorIfNeeded(@Nullable AdbServices ignored,
            @Nullable Throwable maybeFailure) {
        if (maybeFailure == null) {
            return;
        }

        if (!hasShownErrorMessage) {
            view.showAdbLoadingError();
            hasShownErrorMessage = true;
        }
    }

    private static CompletableFuture<AdbServices> initServicesAsync(AdbServicesBridge bridge) {
        return bridge.getAdbServicesAsync();
    }

    private static boolean isCompletedSuccessfully(CompletableFuture<?> future) {
        return future.isDone() && !future.isCompletedExceptionally();
    }
}
