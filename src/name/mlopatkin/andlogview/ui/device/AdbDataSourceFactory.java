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

package name.mlopatkin.andlogview.ui.device;

import name.mlopatkin.andlogview.device.Device;
import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbDataSource;
import name.mlopatkin.andlogview.liblogcat.ddmlib.DeviceDisconnectedHandler;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

import javax.inject.Inject;

public class AdbDataSourceFactory {
    private final DeviceDisconnectedHandler deviceDisconnectedHandler;

    @Inject
    AdbDataSourceFactory(DeviceDisconnectedHandler deviceDisconnectedHandler) {
        this.deviceDisconnectedHandler = deviceDisconnectedHandler;
    }

    public void selectDeviceAndOpenAsDataSource(
            SelectDeviceDialog.Factory selectDeviceDialogFactory,
            Consumer<? super @Nullable AdbDataSource> callback) {
        selectDeviceDialogFactory.show((dialog, selectedDevice) -> {
            if (selectedDevice != null) {
                openDeviceAsDataSource(selectedDevice, callback);
            } else {
                callback.accept(null);
            }
        });
    }

    public void openDeviceAsDataSource(Device device, Consumer<? super AdbDataSource> callback) {
        AdbDataSource dataSource = new AdbDataSource(device);
        deviceDisconnectedHandler.startWatching(dataSource);
        callback.accept(dataSource);
    }
}
