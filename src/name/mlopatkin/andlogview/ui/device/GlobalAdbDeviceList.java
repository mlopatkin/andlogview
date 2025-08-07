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

import name.mlopatkin.andlogview.AppExecutors;
import name.mlopatkin.andlogview.base.concurrent.SequentialExecutor;
import name.mlopatkin.andlogview.device.AdbDeviceList;
import name.mlopatkin.andlogview.device.AdbServer;
import name.mlopatkin.andlogview.device.Device;
import name.mlopatkin.andlogview.device.DeviceChangeObserver;
import name.mlopatkin.andlogview.device.ProvisionalDevice;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import com.google.common.collect.ImmutableList;

import org.jspecify.annotations.Nullable;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * A holder for per-AdbServer instances of {@link AdbDeviceList}. Changes to the underlying list become opaque to the
 * clients.
 */
class GlobalAdbDeviceList implements AdbDeviceList {
    private final Subject<DeviceChangeObserver> deviceChangeObservers = new Subject<>();
    private final SequentialExecutor uiExecutor;

    private final DeviceChangeObserver listScopedObserver = new DeviceChangeObserver() {
        @Override
        public void onProvisionalDeviceConnected(ProvisionalDevice device) {
            for (var obs : deviceChangeObservers) {
                obs.onProvisionalDeviceConnected(device);
            }
        }

        @Override
        public void onDeviceConnected(Device device) {
            for (var obs : deviceChangeObservers) {
                obs.onDeviceConnected(device);
            }
        }

        @Override
        public void onDeviceDisconnected(ProvisionalDevice device) {
            for (var obs : deviceChangeObservers) {
                obs.onDeviceDisconnected(device);
            }
        }

        @Override
        public void onDeviceChanged(Device device) {
            for (var obs : deviceChangeObservers) {
                obs.onDeviceChanged(device);
            }
        }
    };

    private @Nullable AdbDeviceList serverScopedDeviceList;

    @Inject
    public GlobalAdbDeviceList(@Named(AppExecutors.UI_EXECUTOR) SequentialExecutor uiExecutor) {
        this.uiExecutor = uiExecutor;
    }

    public void setAdbServer(@Nullable AdbServer newServer) {
        var currentList = serverScopedDeviceList;
        serverScopedDeviceList = null;
        if (currentList != null) {
            currentList.asObservable().removeObserver(listScopedObserver);
            currentList.close();
        }
        if (newServer != null) {
            serverScopedDeviceList = newServer.getDeviceList(uiExecutor);
            serverScopedDeviceList.asObservable().addObserver(listScopedObserver);
        }
    }

    @Override
    public List<Device> getDevices() {
        if (serverScopedDeviceList == null) {
            return ImmutableList.of();
        }
        return serverScopedDeviceList.getDevices();
    }

    @Override
    public List<ProvisionalDevice> getAllDevices() {
        if (serverScopedDeviceList == null) {
            return ImmutableList.of();
        }
        return serverScopedDeviceList.getAllDevices();
    }

    @Override
    public Observable<DeviceChangeObserver> asObservable() {
        return deviceChangeObservers.asObservable();
    }

    @Override
    public void close() {
        // Nobody calls this yet.
        setAdbServer(null);
    }
}
