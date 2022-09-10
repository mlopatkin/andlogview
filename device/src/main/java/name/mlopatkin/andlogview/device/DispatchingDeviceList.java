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

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import com.google.errorprone.annotations.concurrent.GuardedBy;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This is the primary dispatcher for AdbDeviceList implementations.
 */
class DispatchingDeviceList implements Observable<DeviceChangeObserver> {
    private static final Logger logger = Logger.getLogger(DispatchingDeviceList.class);

    private final Object deviceLock = new Object();
    private final Object observerLock = new Object();

    @GuardedBy("deviceLock")
    private final LinkedHashMap<IDevice, AdbDeviceImpl> devices;

    @GuardedBy("observerLock")
    private final ArrayList<DeviceChangeObserver> deviceChangeObservers = new ArrayList<>();

    private final AndroidDebugBridge.IDeviceChangeListener deviceChangeListener =
            new AndroidDebugBridge.IDeviceChangeListener() {
                @Override
                public void deviceConnected(IDevice device) {
                    logger.debug(formatDeviceLog(device, "connected"));
                    AdbDeviceImpl prevDevice;
                    AdbDeviceImpl newDevice = new AdbDeviceImpl(device);
                    synchronized (deviceLock) {
                        prevDevice = devices.putIfAbsent(device, newDevice);
                    }
                    if (prevDevice == null) {
                        notifyProvisionalDeviceConnected(newDevice);
                        notifyDeviceConnected(newDevice);
                    } else {
                        // We already have this device in our list, a disconnect was probably missed.
                        notifyDeviceChanged(prevDevice);
                    }
                }

                @Override
                public void deviceDisconnected(IDevice device) {
                    logger.debug(formatDeviceLog(device, "disconnected"));
                    AdbDevice adbDevice;
                    synchronized (deviceLock) {
                        adbDevice = devices.remove(device);
                    }
                    if (adbDevice != null) {
                        notifyDeviceDisconnected(adbDevice);
                    }
                }

                @Override
                public void deviceChanged(IDevice device, int changeMask) {
                    if (logger.isDebugEnabled()) {
                        List<String> changes = new ArrayList<>(3);
                        if ((changeMask & IDevice.CHANGE_BUILD_INFO) != 0) {
                            changes.add("CHANGE_BUILD_INFO");
                        }
                        if ((changeMask & IDevice.CHANGE_CLIENT_LIST) != 0) {
                            changes.add("CHANGE_CLIENT_LIST");
                        }
                        if ((changeMask & IDevice.CHANGE_STATE) != 0) {
                            changes.add("CHANGE_STATE");
                        }
                        logger.debug(formatDeviceLog(device, "state changed {%s}", Joiner.on(" | ").join(changes)));
                    }

                    if (!isRelevantChange(changeMask)) {
                        return;
                    }

                    AdbDeviceImpl adbDevice;
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

    public static DispatchingDeviceList create(AdbFacade adb) {
        DispatchingDeviceList result = new DispatchingDeviceList();
        result.init(adb);
        return result;
    }

    private DispatchingDeviceList() {
        devices = new LinkedHashMap<>();
    }

    private void init(AdbFacade adb) {
        adb.addDeviceChangeListener(deviceChangeListener);
        IDevice[] knownDevices = adb.getDevices();
        synchronized (deviceLock) {
            for (IDevice device : knownDevices) {
                this.devices.putIfAbsent(device, new AdbDeviceImpl(device));
            }
        }
    }

    /**
     * Returns the list of all connected devices, both provisioned and not.
     *
     * @return the list of all connected devices
     */
    public ImmutableList<ProvisionalAdbDevice> getAllDevices() {
        // This isn't really a copy, just a typecast.
        return ImmutableList.copyOf(getDevices());
    }

    /**
     * Returns the list of already provisioned devices.
     *
     * @return the list of provisioned devices
     */
    public ImmutableList<AdbDevice> getDevices() {
        synchronized (deviceLock) {
            return ImmutableList.copyOf(devices.values());
        }
    }

    private List<DeviceChangeObserver> getObservers() {
        synchronized (observerLock) {
            return ImmutableList.copyOf(deviceChangeObservers);
        }
    }

    private void notifyProvisionalDeviceConnected(ProvisionalAdbDevice provisionalDevice) {
        logger.debug(formatDeviceLog(provisionalDevice, "notifyDeviceConnected"));
        for (DeviceChangeObserver obs : getObservers()) {
            obs.onProvisionalDeviceConnected(provisionalDevice);
        }
    }
    private void notifyDeviceConnected(AdbDevice device) {
        logger.debug(formatDeviceLog(device, "notifyDeviceConnected"));
        for (DeviceChangeObserver obs : getObservers()) {
            obs.onDeviceConnected(device);
        }
    }

    private void notifyDeviceDisconnected(AdbDevice device) {
        logger.debug(formatDeviceLog(device, "notifyDeviceDisconnected"));
        for (DeviceChangeObserver obs : getObservers()) {
            obs.onDeviceDisconnected(device);
        }
    }

    private void notifyDeviceChanged(AdbDevice device) {
        logger.debug(formatDeviceLog(device, "notifyDeviceChanged (online=%s)", device.isOnline()));
        for (DeviceChangeObserver obs : getObservers()) {
            obs.onDeviceChanged(device);
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

    @FormatMethod
    private static String formatDeviceLog(ProvisionalAdbDevice device, @FormatString String format, Object... args) {
        return ("[" + device.getSerialNumber() + "]: ") + String.format(format, args);
    }

    @FormatMethod
    private static String formatDeviceLog(IDevice device, @FormatString String format, Object... args) {
        return ("[" + device.getSerialNumber() + "]: ") + String.format(format, args);
    }
}
