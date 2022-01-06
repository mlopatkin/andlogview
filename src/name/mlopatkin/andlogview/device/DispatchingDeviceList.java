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

import name.mlopatkin.andlogview.device.AdbDeviceList.DeviceChangeObserver;
import name.mlopatkin.andlogview.utils.events.Observable;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.concurrent.GuardedBy;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is the primary dispatcher for AdbDeviceList implementations.
 */
class DispatchingDeviceList implements Observable<DeviceChangeObserver> {
    private final Object deviceLock = new Object();
    private final Object observerLock = new Object();

    @GuardedBy("deviceLock")
    private final LinkedHashMap<IDevice, AdbDevice> devices;

    @GuardedBy("observerLock")
    private final ArrayList<DeviceChangeObserver> deviceChangeObservers = new ArrayList<>();

    private final AndroidDebugBridge.IDeviceChangeListener deviceChangeListener =
            new AndroidDebugBridge.IDeviceChangeListener() {
                @Override
                public void deviceConnected(IDevice device) {
                    AdbDevice prevDevice;
                    AdbDevice newDevice = new AdbDeviceImpl(device);
                    synchronized (deviceLock) {
                        prevDevice = devices.putIfAbsent(device, newDevice);
                    }
                    if (prevDevice == null) {
                        notifyDeviceAdded(newDevice);
                    } else {
                        // We already have this device in our list, a disconnect was probably missed.
                        notifyDeviceChanged(prevDevice);
                    }
                }

                @Override
                public void deviceDisconnected(IDevice device) {
                    AdbDevice adbDevice;
                    synchronized (deviceLock) {
                        adbDevice = devices.remove(device);
                    }
                    if (adbDevice != null) {
                        notifyDeviceRemoved(adbDevice);
                    }
                }

                @Override
                public void deviceChanged(IDevice device, int changeMask) {
                    if (isRelevantChange(changeMask)) {
                        return;
                    }

                    AdbDevice adbDevice;
                    synchronized (deviceLock) {
                        adbDevice = devices.get(device);
                    }
                    if (adbDevice != null) {
                        notifyDeviceChanged(adbDevice);
                    } else {
                        // We don't know about this device yet.
                        deviceConnected(device);
                    }
                }
            };

    private boolean isRelevantChange(int changeMask) {
        return (changeMask & (IDevice.CHANGE_STATE | IDevice.CHANGE_BUILD_INFO)) != 0;
    }

    public static DispatchingDeviceList create(AdbServerImpl server) {
        DispatchingDeviceList result = new DispatchingDeviceList(server);
        AndroidDebugBridge.addDeviceChangeListener(result.deviceChangeListener);
        return result;
    }

    private DispatchingDeviceList(AdbServerImpl server) {
        synchronized (deviceLock) {
            // Seed devices with whatever bridge knows about.
            devices = Arrays.stream(server.getBridge().getDevices())
                    .collect(
                            Collectors.toMap(
                                    Function.identity(),
                                    AdbDeviceImpl::new,
                                    (a, b) -> {
                                        throw new IllegalArgumentException("Duplicate devices");
                                    },
                                    LinkedHashMap::new));
        }
    }

    public ImmutableList<AdbDevice> getDevices() {
        final ImmutableList.Builder<AdbDevice> snapshot;
        synchronized (deviceLock) {
            snapshot = ImmutableList.builderWithExpectedSize(devices.size());
            snapshot.addAll(devices.values());
        }
        return snapshot.build();
    }

    private List<DeviceChangeObserver> getObservers() {
        synchronized (observerLock) {
            return ImmutableList.copyOf(deviceChangeObservers);
        }
    }

    private void notifyDeviceAdded(AdbDevice newDevice) {
        for (DeviceChangeObserver obs : getObservers()) {
            obs.onDeviceConnected(newDevice);
        }
    }

    private void notifyDeviceRemoved(AdbDevice removedDevice) {
        for (DeviceChangeObserver obs : getObservers()) {
            obs.onDeviceDisconnected(removedDevice);
        }
    }

    private void notifyDeviceChanged(AdbDevice changedDevice) {
        for (DeviceChangeObserver obs : getObservers()) {
            obs.onDeviceChanged(changedDevice);
        }
    }

    @Override
    public void addObserver(DeviceChangeObserver observer) {
        synchronized (observerLock) {
            deviceChangeObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(@Nullable DeviceChangeObserver observer) {
        synchronized (observerLock) {
            deviceChangeObservers.remove(observer);
        }
    }
}
