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

import name.mlopatkin.andlogview.DeviceDisconnectedHandler;
import name.mlopatkin.andlogview.MainFrame;
import name.mlopatkin.andlogview.device.AdbDevice;
import name.mlopatkin.andlogview.device.AdbDeviceList;
import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbDataSource;
import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbDeviceManager;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;

import java.util.function.Consumer;

import javax.inject.Inject;

public class AdbDataSourceFactory {
    // TODO(mlopatkin) get rid of MainFrame injection here.
    private final MainFrame mainFrame;
    private final SelectDeviceDialog.Factory selectDeviceDialogFactory;
    private final AdbDeviceList adbDeviceList;
    // TODO(mlopatkin) get rid of AdbDeviceManager injection here.
    private final AdbDeviceManager adbDeviceManager;
    private final AdbConfigurationPref adbConfigurationPref;

    @Inject
    AdbDataSourceFactory(MainFrame mainFrame, SelectDeviceDialog.Factory selectDeviceDialogFactory,
            AdbDeviceList adbDeviceList, AdbDeviceManager adbDeviceManager, AdbConfigurationPref adbConfigurationPref) {
        this.mainFrame = mainFrame;
        this.selectDeviceDialogFactory = selectDeviceDialogFactory;
        this.adbDeviceList = adbDeviceList;
        this.adbDeviceManager = adbDeviceManager;
        this.adbConfigurationPref = adbConfigurationPref;
    }

    public void selectDeviceAndOpenAsDataSource(Consumer<AdbDataSource> callback) {
        selectDeviceDialogFactory.show((dialog, selectedDevice) -> {
            if (selectedDevice != null) {
                openDeviceAsDataSource(selectedDevice, callback);
            }
        });
    }

    public void openDeviceAsDataSource(AdbDevice device, Consumer<AdbDataSource> callback) {
        DeviceDisconnectedHandler.startWatching(mainFrame, adbConfigurationPref, adbDeviceManager, device);
        callback.accept(new AdbDataSource(device, adbDeviceList));
    }
}
