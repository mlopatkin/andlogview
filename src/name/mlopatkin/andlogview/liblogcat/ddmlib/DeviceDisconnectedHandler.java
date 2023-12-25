/*
 * Copyright 2022 Mikhail Lopatkin
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
package name.mlopatkin.andlogview.liblogcat.ddmlib;

import name.mlopatkin.andlogview.AppExecutors;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.ui.mainframe.ErrorDialogs;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * This class is responsible for showing notification dialog when device is disconnected.
 */
@MainFrameScoped
public class DeviceDisconnectedHandler implements AdbDataSource.StateObserver {
    public interface DeviceAwaiter {
        void waitForDevice();
    }

    private final DeviceAwaiter deviceAwaiter;
    private final ErrorDialogs errorDialogs;
    private final AdbConfigurationPref adbConfigurationPref;
    private final Executor uiExecutor;

    @Inject
    DeviceDisconnectedHandler(
            DeviceAwaiter deviceAwaiter,
            ErrorDialogs errorDialogs,
            AdbConfigurationPref adbConfigurationPref,
            @Named(AppExecutors.UI_EXECUTOR) Executor uiExecutor
    ) {
        this.deviceAwaiter = deviceAwaiter;
        this.errorDialogs = errorDialogs;
        this.adbConfigurationPref = adbConfigurationPref;
        this.uiExecutor = uiExecutor;
    }

    @Override
    public void onDataSourceInvalidated(AdbDataSource.InvalidationReason reason) {
        switch (reason) {
            case DISCONNECT:
                onDeviceDisconnected("Device is disconnected");
                break;
            case OFFLINE:
                onDeviceDisconnected("Device goes offline");
                break;
            default:
                throw new AssertionError("Unexpected reason " + reason);
        }
    }

    private void onDeviceDisconnected(String message) {
        if (adbConfigurationPref.isAutoReconnectEnabled()) {
            deviceAwaiter.waitForDevice();
        } else {
            // The dialog is modal, and must be shown in async manner. Otherwise, we'll block all other listeners.
            uiExecutor.execute(() -> showNotificationDialog(message));
        }
    }

    private void showNotificationDialog(String message) {
        errorDialogs.showDeviceDisconnectedWarning(message);
    }

    /**
     * Starts tracking the invalidation of the given data source. When the data source becomes invalid a notification is
     * shown or the reconnection attempt is made.
     *
     * @param dataSource the data source to track
     */
    public void startWatching(AdbDataSource dataSource) {
        dataSource.asStateObservable().addObserver(this);
    }
}
