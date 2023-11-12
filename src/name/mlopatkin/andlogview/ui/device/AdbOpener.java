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
import name.mlopatkin.andlogview.utils.Cancellable;
import name.mlopatkin.andlogview.utils.MyFutures;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

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
     * @param consumer the handler to process resulting data source
     * @param failureHandler the handler to process adb initialization failure if any
     * @return the cancellable handle to stop initialization
     */
    public PendingDataSource selectAndOpenDevice(Consumer<? super @Nullable AdbDataSource> consumer,
            Consumer<? super Throwable> failureHandler) {
        return PendingDataSource.fromCancellable(presenter.withAdbServicesInteractive(
                adb -> adb.getDataSourceFactory().selectDeviceAndOpenAsDataSource(consumer),
                failureHandler));
    }

    /**
     * Initializes ADB if necessary, waits for a first device to come online and builds the ADB data source out of it.
     * <p>
     * The returned handle must be used on UI thread only.
     *
     * @param consumer the handler to process resulting data source
     * @param failureHandler the handler to process adb initialization failure if any
     * @return the cancellable handle to stop initialization
     */
    public PendingDataSource awaitDevice(Consumer<? super AdbDataSource> consumer,
            Consumer<? super Throwable> failureHandler) {
        var result = new MultiStagePendingDataSource();
        result.addStage(presenter.withAdbServices(
                adb -> result.addStage(awaitDevice(adb))
                        .thenAccept(
                                device -> adb.getDataSourceFactory().openDeviceAsDataSource(device, consumer))
                        .exceptionally(exceptionHandler(ignoreCancellations(failureHandler))),
                failureHandler));
        return result;
    }

    private CompletableFuture<Device> awaitDevice(AdbServices adbServices) {
        var deviceList = adbServices.getDeviceList();
        return getFirstOnlineDevice(deviceList).map(CompletableFuture::completedFuture).orElseGet(() -> {
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
            return future;
        });
    }

    private Optional<Device> getFirstOnlineDevice(AdbDeviceList deviceList) {
        return deviceList.getDevices().stream().filter(Device::isOnline).findFirst();
    }

    private static class MultiStagePendingDataSource implements PendingDataSource {
        private final List<Cancellable> cancellable = new ArrayList<>();

        public <T extends Cancellable> T addStage(T cancellable) {
            this.cancellable.add(cancellable);
            return cancellable;
        }

        public <T> CompletableFuture<T> addStage(CompletableFuture<T> future) {
            cancellable.add(MyFutures.toCancellable(future));
            return future;
        }

        @Override
        public boolean cancel() {
            var result = cancellable.isEmpty();
            for (Cancellable c : cancellable) {
                result |= c.cancel();
            }
            return result;
        }
    }
}
