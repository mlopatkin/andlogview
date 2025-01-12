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
import name.mlopatkin.andlogview.base.MyThrowables;
import name.mlopatkin.andlogview.device.AdbDeviceList;
import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.liblogcat.ddmlib.DeviceDisconnectedHandler;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
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
         * Show the user that ADB services aren't ready yet, but they are being loading. Depending on the way the
         * notification is presented, the user may be able to dismiss it. Dismissing the notification may cancel the
         * action that requested the load, so the UI has to accommodate for that.
         *
         * @param isCancellable if the action being run is cancellable
         * @param userHideAction the callback to run if the user hides the notification
         */
        void showAdbLoadingProgress(boolean isCancellable, Runnable userHideAction);

        /**
         * Show the user that ADB services are now ready.
         */
        void hideAdbLoadingProgress();

        /**
         * Show the user that ADB services failed to load. This is only called once per presenter lifetime.
         */
        void showAdbLoadingError(String failureReason, boolean isAutoStart);
    }

    private final AdbServicesBridge bridge;
    private final View view;
    private final AdbConfigurationPref adbConfigurationPref;
    private final Executor uiExecutor;
    private final DeviceDisconnectedHandler deviceDisconnectedHandler;
    // Maintains set of the current requests to show loading progress. Useful, when new show and old hide requests
    // overlap because of the delayed execution of hide callback.
    private final Set<Object> progressTokens = new HashSet<>();

    private boolean hasShownErrorMessage;

    @Inject
    AdbServicesInitializationPresenter(
            View view,
            AdbServicesBridge bridge,
            AdbConfigurationPref adbConfigurationPref,
            @Named(AppExecutors.UI_EXECUTOR) Executor uiExecutor,
            DeviceDisconnectedHandler deviceDisconnectedHandler) {
        this.bridge = bridge;
        this.view = view;
        this.adbConfigurationPref = adbConfigurationPref;
        this.uiExecutor = uiExecutor;
        this.deviceDisconnectedHandler = deviceDisconnectedHandler;
    }

    /**
     * Kicks off initialization of the adb services and returns the device list, without waiting for the
     * initialization to complete. This method should be used when the device list is enough for the task.
     * There is no indication of the loading progress, but ADB initialization
     * <p>
     * The initialization of the adb services triggered by this method cannot be cancelled.
     *
     * @param failureHandler the failure handler to be called if the ADB failed to initialize
     */
    public AdbDeviceList withAdbDeviceList(Consumer<? super Throwable> failureHandler) {
        return bridge.prepareAdbDeviceList(ignoreCancellations(failureHandler));
    }

    /**
     * Initializes the adb services and executes the action. This method should be used when the user triggers the
     * action, it takes care of indicating the pause. The actions are executed on the UI executor. If the initialization
     * fails, the error dialog may be shown and
     * <p>
     * The initialization may be cancelled by using the returned handle. When cancelled, the failure handler is invoked.
     *
     * @param action the action to execute
     * @param failureHandler the failure handler
     * @return cancellable handle to abort initialization
     */
    public Cancellable withAdbServicesInteractive(Consumer<? super AdbServices> action,
            Consumer<? super Throwable> failureHandler) {
        return initAdbServicesInteractive(true /* allowCancellation */, action, failureHandler);
    }

    private Cancellable initAdbServicesInteractive(
            boolean allowCancellation,
            Consumer<? super AdbServices> action,
            Consumer<? super Throwable> failureHandler) {
        var result = getServicesAsync();
        var future = result;
        if (!future.isDone()) {
            // Only bother with setting cursors if the ADB is not yet initialized.
            var token = new Object();
            future = future.whenCompleteAsync(
                    (services, th) -> hideProgressWithToken(token),
                    uiExecutor);
            // Cancellation only affects the view. Non-cancellable requests don't actually care about the returned
            // future, so we can cancel it to hide the progress view. Hiding will also prevent the error dialog from
            // appearing if the ADB initialization eventually fails.
            // TODO(mlopatkin) Should there be a non-invasive UI to show the ADB loading status?
            showProgressWithToken(allowCancellation, token, () -> result.cancel(false));
        }
        future.handleAsync(
                        consumingHandler(action, ignoreCancellations(this::handleAdbError).andThen(failureHandler)),
                        uiExecutor)
                .exceptionally(MyFutures::uncaughtException);
        // TODO(mlopatkin) Should we always show a failure message if the ADB fails/is failed for the interactive
        //  request?
        return MyFutures.toCancellable(result);
    }

    private CompletableFuture<AdbServices> getServicesAsync() {
        return bridge.getAdbServicesAsync();
    }

    private void showProgressWithToken(boolean allowCancellation, Object token, Runnable userHideAction) {
        var isFirstToken = progressTokens.isEmpty();
        progressTokens.add(token);
        if (isFirstToken) {
            view.showAdbLoadingProgress(allowCancellation, userHideAction);
        }
    }

    private void hideProgressWithToken(Object token) {
        progressTokens.remove(token);
        if (progressTokens.isEmpty()) {
            view.hideAdbLoadingProgress();
        }
    }

    /**
     * Shows the ADB failure to the user if appropriate. Consecutive errors are typically not shown.
     *
     * @param failure the failure
     */
    public void handleAdbError(Throwable failure) {
        handleAdbError(failure, false);
    }

    /**
     * Shows the ADB failure to the user if appropriate. Consecutive errors are typically not shown.
     * <p>
     * The error may also be suppressed if it is a result of an automatic connection that happened without explicit
     * ADB service request, e.g. when the user starts the app without a log file provided.
     *
     * @param failure the failure
     * @param isAutoStart if the error is triggered by the automatic connection setup
     */
    public void handleAdbError(Throwable failure, boolean isAutoStart) {
        if (!hasShownErrorMessage && (!isAutoStart || adbConfigurationPref.shouldShowAutostartFailures())) {
            hasShownErrorMessage = true;
            // showAdbLoadingError blocks and opens a nested message pump. It is important to set up the flag before
            // showing the dialog to prevent re-entrance and double dialog.
            if (MyThrowables.unwrapUninteresting(failure) instanceof AdbException adbException) {
                view.showAdbLoadingError(adbException.getMessage(), isAutoStart);
            } else {
                view.showAdbLoadingError("Failed to initialize ADB", isAutoStart);
            }
        }
    }

    /**
     * Restarts ADB services (or starts one if it is not yet running) asynchronously.
     * <p>
     * Shows the progress indication while ADB is initialized. Upon failure, shows the error dialog.
     */
    public void restartAdb() {
        // Stopping ADB trigger disconnect dialogs now
        deviceDisconnectedHandler.suppressDialogs();
        bridge.stopAdb();
        hasShownErrorMessage = false;
        initAdbServicesInteractive(
                false /* allowCancellation */,
                adbServices -> deviceDisconnectedHandler.resumeDialogs(),
                failure -> deviceDisconnectedHandler.resumeDialogs());
    }
}
