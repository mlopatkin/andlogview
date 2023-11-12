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

import static name.mlopatkin.andlogview.utils.MyFutures.exceptionHandler;
import static name.mlopatkin.andlogview.utils.MyFutures.ignoreCancellations;

import name.mlopatkin.andlogview.AppExecutors;
import name.mlopatkin.andlogview.device.AdbDeviceList;
import name.mlopatkin.andlogview.device.Device;
import name.mlopatkin.andlogview.device.DeviceChangeObserver;
import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbDataSource;
import name.mlopatkin.andlogview.ui.PendingDataSource;
import name.mlopatkin.andlogview.utils.MyFutures;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Opens {@link AdbDataSource}. May show error dialogs if the process doesn't go well.
 */
public class AdbOpener {
    private final AdbServicesInitializationPresenter presenter;
    private final Executor uiExecutor;

    @Inject
    AdbOpener(AdbServicesInitializationPresenter presenter,
            @Named(AppExecutors.UI_EXECUTOR) Executor uiExecutor) {
        this.presenter = presenter;
        this.uiExecutor = uiExecutor;
    }

    /**
     * Initializes ADB if necessary, opens device selection dialog and builds the ADB data source out of the
     * user-selected device. If the user decides to cancel the dialog, then consumer receives {@code null}.
     * <p>
     * The returned handle must be used on UI thread only.
     *
     * @return the cancellable handle to stop initialization
     */
    public PendingDataSource<@Nullable AdbDataSource> selectAndOpenDevice() {
        var result = new PendingDataSource.CompletablePendingDataSource<AdbDataSource>();
        result.addStage(presenter.withAdbServicesInteractive(
                adb -> adb.getDataSourceFactory().selectDeviceAndOpenAsDataSource(result::complete),
                result::fail));
        return result;
    }

    /**
     * Initializes ADB if necessary, waits for a first device to come online and builds the ADB data source out of it.
     * <p>
     * The returned handle must be used on UI thread only.
     *
     * @return the cancellable handle to stop initialization
     */
    public PendingDataSource<AdbDataSource> awaitDevice() {
        var result = new PendingDataSource.CompletablePendingDataSource<AdbDataSource>();
        result.addStage(presenter.withAdbServices(
                adb -> result.addStage(awaitDevice(adb))
                        .thenAccept(
                                device -> adb.getDataSourceFactory().openDeviceAsDataSource(device, result::complete))
                        .exceptionally(exceptionHandler(ignoreCancellations(result::fail))),
                result::fail));
        return result;
    }

    private CompletableFuture<Device> awaitDevice(AdbServices adbServices) {
        var deviceList = adbServices.getDeviceList();
        var future = new CompletableFuture<Device>();
        var deviceObserver = new DeviceChangeObserver() {
            @Override
            public void onDeviceConnected(Device device) {
                tryConnect(device);
            }

            @Override
            public void onDeviceChanged(Device device) {
                tryConnect(device);
            }

            private void tryConnect(Device device) {
                if (device.isOnline()) {
                    future.complete(device);
                }
            }
        };
        deviceList.asObservable().addObserver(deviceObserver);
        // Spawn a sibling cleanup branch that triggers when the future is completed or cancelled.
        future.handleAsync(
                        (r, th) -> {
                            // An upstream exception may come here as `th`, but it is swallowed. This branch only
                            // cares about cleaning up.
                            deviceList.asObservable().removeObserver(deviceObserver);
                            return null;
                        },
                        uiExecutor)
                // Exceptions form handleAsync above are forwarded, though.
                .exceptionally(MyFutures::uncaughtException);
        // Device list set up is racy, but now we'll receive all updates. To make sure we've considered the state prior
        // to subscribing, let's seed the future with the current device list, if any.
        getFirstOnlineDevice(deviceList).ifPresent(future::complete);
        return future;
    }

    private Optional<Device> getFirstOnlineDevice(AdbDeviceList deviceList) {
        return deviceList.getDevices().stream().filter(Device::isOnline).findFirst();
    }
}
