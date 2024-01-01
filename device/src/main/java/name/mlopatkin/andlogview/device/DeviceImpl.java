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

import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import java.util.List;

class DeviceImpl implements DeviceInternal {
    private final DeviceKey deviceKey;
    private final LoggingDevice device;
    private final DeviceProperties deviceProperties;
    private final Subject<DeviceChangeObserver> observers = new Subject<>();

    public DeviceImpl(DeviceKey deviceKey, LoggingDevice device, DeviceProperties deviceProperties) {
        this.deviceKey = deviceKey;
        this.device = device;
        this.deviceProperties = deviceProperties;
    }

    @Override
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
    public int getApiLevel() {
        return deviceProperties.getApiLevel();
    }

    @Override
    public boolean isOnline() {
        return device.isOnline();
    }

    @Override
    public Command command(List<String> commandLine) {
        return new CommandImpl(device, commandLine);
    }

    @Override
    public Observable<DeviceChangeObserver> asObservable() {
        return observers.asObservable();
    }

    @Override
    public void notifyProvisioned() {
        for (var obs : observers) {
            obs.onDeviceConnected(this);
        }
    }

    @Override
    public void notifyChanged() {
        for (var obs : observers) {
            obs.onDeviceChanged(this);
        }
    }

    @Override
    public void notifyConnected() {
        for (var obs : observers) {
            obs.onProvisionalDeviceConnected(this);
        }
    }

    @Override
    public void notifyDisconnected() {
        for (var obs : observers) {
            obs.onDeviceDisconnected(this);
        }
    }
}
