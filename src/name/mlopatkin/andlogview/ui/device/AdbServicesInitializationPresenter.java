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
import static name.mlopatkin.andlogview.utils.MyFutures.ignoreCancellations;

import name.mlopatkin.andlogview.AppExecutors;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.utils.Cancellable;
import name.mlopatkin.andlogview.utils.MyFutures;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * The presenter handles ADB initialization and shows progress and errors to the user.
 */
@MainFrameScoped
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

    private final AdbServicesBridge bridge;
    private final View view;
    private final Executor uiExecutor;
    // Maintains set of the current requests to show loading progress. Useful, when new show and old hide requests
    // overlap because of the delayed execution of hide callback.
    private final Set<Object> progressTokens = new HashSet<>();

    private boolean hasShownErrorMessage;

    @Inject
    AdbServicesInitializationPresenter(AdbServicesBridge bridge, View view,
            @Named(AppExecutors.UI_EXECUTOR) Executor uiExecutor) {
        this.bridge = bridge;
        this.view = view;
        this.uiExecutor = uiExecutor;
    }

    /**
     * Initializes the adb services and executes the action. This method should be used when the user doesn't directly
     * trigger the action, so no progress indication is needed. The actions are executed on the UI executor.
     * <p>
     * The initialization may be cancelled by using the returned handle. When cancelled, no callbacks are invoked.
     *
     * @param action the action to execute
     * @param failureHandler the failure handler
     * @return cancellable handle to abort initialization
     */
    public Cancellable withAdbServices(Consumer<? super AdbServices> action,
            Consumer<? super Throwable> failureHandler) {
        var result = getServicesAsync();
        result.handleAsync(
                        consumingHandler(action, ignoreCancellations(adbErrorHandler().andThen(failureHandler))),
                        uiExecutor)
                .exceptionally(MyFutures::uncaughtException);
        return MyFutures.toCancellable(result);
    }

    /**
     * Initializes the adb services and executes the action. This method should be used when the user triggers the
     * action, it takes care of indicating the pause. The actions are executed on the UI executor.
     * <p>
     * The initialization may be cancelled by using the returned handle. When cancelled, no callbacks are invoked.
     *
     * @param action the action to execute
     * @param failureHandler the failure handler
     * @return cancellable handle to abort initialization
     */
    public Cancellable withAdbServicesInteractive(Consumer<? super AdbServices> action,
            Consumer<? super Throwable> failureHandler) {
        var result = getServicesAsync();
        var future = result;
        if (!future.isDone()) {
            // Only bother with setting cursors if the ADB is not yet initialized.
            var token = showProgressWithToken();
            future = future.whenCompleteAsync(
                    (services, th) -> hideProgressWithToken(token),
                    uiExecutor);
        }
        future.handleAsync(
                        consumingHandler(action, ignoreCancellations(adbErrorHandler().andThen(failureHandler))),
                        uiExecutor)
                .exceptionally(MyFutures::uncaughtException);
        // TODO(mlopatkin) Should we always show a failure message if the ADB fails/is failed for the interactive
        //  request?
        return MyFutures.toCancellable(result);
    }

    private CompletableFuture<AdbServices> getServicesAsync() {
        return bridge.getAdbServicesAsync();
    }

    private Object showProgressWithToken() {
        if (progressTokens.isEmpty()) {
            view.showAdbLoadingProgress();
        }
        var token = new Object();
        progressTokens.add(token);
        return token;
    }

    private void hideProgressWithToken(Object token) {
        progressTokens.remove(token);
        if (progressTokens.isEmpty()) {
            view.hideAdbLoadingProgress();
        }
    }

    private Consumer<Throwable> adbErrorHandler() {
        return ignored -> {
            if (!hasShownErrorMessage) {
                view.showAdbLoadingError();
                hasShownErrorMessage = true;
            }
        };
    }
}