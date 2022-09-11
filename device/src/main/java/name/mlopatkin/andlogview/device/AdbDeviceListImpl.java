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

package name.mlopatkin.andlogview.device;

import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.concurrent.Executor;

class AdbDeviceListImpl
        implements AdbDeviceList, DeviceChangeObserver, Observable<DeviceChangeObserver> {
    private final DispatchingDeviceList dispatcher;
    private final Executor listenerExecutor;
    private final Subject<DeviceChangeObserver> deviceChangeObservers = new Subject<>();


    AdbDeviceListImpl(DispatchingDeviceList dispatcher, Executor listenerExecutor) {
        this.dispatcher = dispatcher;
        this.listenerExecutor = listenerExecutor;
    }

    @Override
    public void onDeviceConnected(Device device) {
        listenerExecutor.execute(() -> {
            for (DeviceChangeObserver obs : deviceChangeObservers) {
                obs.onDeviceConnected(device);
            }
        });
    }

    @Override
    public void onDeviceDisconnected(Device device) {
        listenerExecutor.execute(() -> {
            for (DeviceChangeObserver obs : deviceChangeObservers) {
                obs.onDeviceDisconnected(device);
            }
        });
    }

    @Override
    public void onDeviceChanged(Device device) {
        listenerExecutor.execute(() -> {
            for (DeviceChangeObserver obs : deviceChangeObservers) {
                obs.onDeviceChanged(device);
            }
        });
    }

    @Override
    public List<Device> getDevices() {
        return dispatcher.getDevices();
    }

    @Override
    public List<ProvisionalDevice> getAllDevices() {
        return dispatcher.getAllDevices();
    }

    @Override
    public void addObserver(DeviceChangeObserver observer) {
        if (deviceChangeObservers.isEmpty()) {
            dispatcher.addObserver(this);
        }
        deviceChangeObservers.asObservable().addObserver(observer);
    }

    @Override
    public void removeObserver(@Nullable DeviceChangeObserver observer) {
        deviceChangeObservers.asObservable().removeObserver(observer);
        if (deviceChangeObservers.isEmpty()) {
            dispatcher.removeObserver(this);
        }
    }

    @Override
    public Observable<DeviceChangeObserver> asObservable() {
        return this;
    }
}
