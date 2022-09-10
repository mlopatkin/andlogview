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

import name.mlopatkin.andlogview.utils.events.Subject;

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class FakeAdbFacade implements AdbFacade {
    private final Subject<IDeviceChangeListener> deviceChangeListeners = new Subject<>();

    private final List<IDevice> connectedDevices = new ArrayList<>();

    public IDevice connectDevice(IDevice device) {
        Preconditions.checkArgument(connectedDevices.add(device), "The device %s is already connected",
                device.getSerialNumber());

        for (IDeviceChangeListener deviceChangeListener : deviceChangeListeners) {
            deviceChangeListener.deviceConnected(device);
        }

        return device;
    }

    public void changeDevice(IDevice device, int changeMask) {
        Preconditions.checkArgument(connectedDevices.contains(device), "The device %s is not connected",
                device.getSerialNumber());

        for (IDeviceChangeListener deviceChangeListener : deviceChangeListeners) {
            deviceChangeListener.deviceChanged(device, changeMask);
        }
    }

    public void changeNotConnectedDevice(IDevice device, int changeMask) {
        Preconditions.checkArgument(!connectedDevices.contains(device), "The device %s is connected",
                device.getSerialNumber());

        for (IDeviceChangeListener deviceChangeListener : deviceChangeListeners) {
            deviceChangeListener.deviceChanged(device, changeMask);
        }
    }

    public void disconnectDevice(IDevice device) {
        Preconditions.checkArgument(connectedDevices.remove(device), "The device %s is not connected",
                device.getSerialNumber());

        for (IDeviceChangeListener deviceChangeListener : deviceChangeListeners) {
            deviceChangeListener.deviceDisconnected(device);
        }
    }

    public void disconnectUnconnectedDevice(IDevice device) {
        Preconditions.checkArgument(!connectedDevices.remove(device), "The device %s was connected",
                device.getSerialNumber());

        for (IDeviceChangeListener deviceChangeListener : deviceChangeListeners) {
            deviceChangeListener.deviceDisconnected(device);
        }
    }

    @Override
    public IDevice[] getDevices() {
        return connectedDevices.toArray(new IDevice[0]);
    }

    @Override
    public void addDeviceChangeListener(IDeviceChangeListener deviceChangeListener) {
        deviceChangeListeners.asObservable().addObserver(deviceChangeListener);
    }
}
