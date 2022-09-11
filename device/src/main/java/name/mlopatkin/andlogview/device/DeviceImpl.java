/*
 * Copyright 2020 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.device;

import com.android.ddmlib.IDevice;

import java.util.List;

class DeviceImpl implements Device {
    private final DeviceKey deviceKey;
    private final LoggingDevice device;
    private final DeviceProperties deviceProperties;

    public DeviceImpl(DeviceKey deviceKey, LoggingDevice device, DeviceProperties deviceProperties) {
        this.deviceKey = deviceKey;
        this.device = device;
        this.deviceProperties = deviceProperties;
    }

    public DeviceKey getDeviceKey() {
        return deviceKey;
    }

    @Override
    public String getSerialNumber() {
        return device.getSerialNumber();
    }

    @Override
    public String getName() {
        return device.getName();
    }

    @Override
    public String getDisplayName() {
        String serial = device.getSerialNumber();
        String productName = device.isEmulator() ? device.getAvdName() : getProduct();
        return productName + " (" + serial + ")";
    }

    @Override
    public String getProduct() {
        return deviceProperties.getProduct();
    }

    @Override
    public String getBuildFingerprint() {
        return deviceProperties.getBuildFingerprint();
    }

    @Override
    public String getApiString() {
        return deviceProperties.getApiVersionString();
    }

    @Override
    public IDevice getIDevice() {
        return device;
    }

    @Override
    public boolean isOnline() {
        return device.isOnline();
    }

    @Override
    public Command command(List<String> commandLine) {
        return new CommandImpl(device, commandLine);
    }
}
