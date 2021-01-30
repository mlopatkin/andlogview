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

class AdbDeviceImpl implements AdbDevice {
    final IDevice device;

    public AdbDeviceImpl(IDevice device) {
        this.device = new LoggingDevice(device);
    }

    @Override
    public String getName() {
        return device.getName();
    }

    @Override
    public String getProduct() {
        return device.getProperty("ro.build.product");
    }

    @Override
    public String getBuildFingerprint() {
        return device.getProperty("ro.build.fingerprint");
    }

    @Override
    public String getApiString() {
        String codename = device.getVersion().getCodename();
        if (codename != null) {
            return codename;
        }
        return String.valueOf(device.getVersion().getApiLevel());
    }

    @Override
    public Command command(List<String> commandLine) {
        return new CommandImpl(device, commandLine);
    }
}
