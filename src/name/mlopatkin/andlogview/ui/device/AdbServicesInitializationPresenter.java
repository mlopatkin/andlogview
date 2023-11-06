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

public class AdbServicesInitializationPresenter {
    public interface View {
        void showAdbLoadingProgress();

        void hideAdbLoadingProgress();
    }

    private final View view;
    private final Executor uiExecutor;

    private final Lazy<CompletableFuture<AdbServices>> services;
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
        services.get().handleAsync(
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

        var future = services.get();
        if (!future.isDone()) {
            // Only bother with setting cursors if the ADB is not yet initialized.
            view.showAdbLoadingProgress();
            future = future.whenCompleteAsync(
                    (services, th) -> view.hideAdbLoadingProgress(),
                    uiExecutor);
        }
        var cancellableStep = future.handleAsync(consumingHandler(action, failureHandler), uiExecutor);
        // Make sure that all runtime errors are not ignored, except cancellations.
        cancellableStep
                .exceptionally(skipCancellations())
                .exceptionally(MyFutures::uncaughtException);
        currentInteractiveRequest = cancellableStep;
    }

    private static CompletableFuture<AdbServices> initServicesAsync(AdbServicesBridge bridge) {
        return bridge.getAdbServicesAsync();
    }
}
