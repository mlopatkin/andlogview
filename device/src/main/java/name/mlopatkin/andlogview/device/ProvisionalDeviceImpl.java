/*
 * Copyright 2022 the Andlogview authors
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

class ProvisionalDeviceImpl implements ProvisionalDeviceInternal {
    private final DeviceKey deviceKey;
    private final LoggingDevice device;

    public ProvisionalDeviceImpl(DeviceKey deviceKey, IDevice device) {
        this.deviceKey = deviceKey;
        this.device = new LoggingDevice(device);
    }

    @Override
    public DeviceKey getDeviceKey() {
        return deviceKey;
    }

    @Override
    public String getSerialNumber() {
        return device.getSerialNumber();
    }

    public LoggingDevice getIDevice() {
        return device;
    }

    @Override
    public String getDisplayName() {
        return device.getSerialNumber();
    }

    @Override
    public boolean isOnline() {
        return device.isOnline();
    }

    @Override
    public void notifyConnected() {
        // do nothing, provisional devices cannot be subscribed to (yet)
    }

    @Override
    public void notifyDisconnected() {
        // do nothing, provisional devices cannot be subscribed to (yet)
    }
}
