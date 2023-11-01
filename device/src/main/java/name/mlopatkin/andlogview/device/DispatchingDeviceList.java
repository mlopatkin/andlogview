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

import static name.mlopatkin.andlogview.utils.MyFutures.exceptionHandler;

import name.mlopatkin.andlogview.utils.events.Observable;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
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

    private final DeviceProvisioner deviceProvisioner;

    @GuardedBy("deviceLock")
    private final LinkedHashMap<DeviceKey, ProvisionalDevice> devices = new LinkedHashMap<>();

    @GuardedBy("observerLock")
    private final ArrayList<DeviceChangeObserver> deviceChangeObservers = new ArrayList<>();

    private final AndroidDebugBridge.IDeviceChangeListener deviceChangeListener =
            new AndroidDebugBridge.IDeviceChangeListener() {
                @Override
                public void deviceConnected(IDevice device) {
                    logger.debug(formatDeviceLog(device, "connected"));
                    ProvisionalDevice prevDevice;
                    ProvisionalDeviceImpl newDevice = new ProvisionalDeviceImpl(DeviceKey.of(device), device);
                    synchronized (deviceLock) {
                        prevDevice = devices.putIfAbsent(newDevice.getDeviceKey(), newDevice);
                    }
                    if (prevDevice == null) {
                        notifyProvisionalDeviceConnected(newDevice);
                        startProvisioning(newDevice);
                    } else {
                        logger.debug(formatDeviceLog(device, "device is already known as %s", prevDevice));
                        // We already have this device in our list, maybe some races between listener and initializing
                        // the initial list. The device might be being provisioned, though.
                        if (prevDevice instanceof Device) {
                            notifyDeviceChanged((Device) prevDevice);
                        }
                    }
                }

                @Override
                public void deviceDisconnected(IDevice device) {
                    logger.debug(formatDeviceLog(device, "disconnected"));
                    ProvisionalDevice adbDevice;
                    synchronized (deviceLock) {
                        adbDevice = devices.remove(DeviceKey.of(device));
                    }
                    if (adbDevice != null) {
                        notifyDeviceDisconnected(adbDevice);
                    }
                    // The pending provision job, if any, will complete itself abnormally.
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

                    ProvisionalDevice adbDevice;
                    synchronized (deviceLock) {
                        adbDevice = devices.get(DeviceKey.of(device));
                    }
                    if (adbDevice != null) {
                        if (adbDevice instanceof Device) {
                            notifyDeviceChanged((Device) adbDevice);
                        }
                    } else {
                        // We don't know about this device yet.
                        deviceConnected(device);
                    }
                }
            };

    private boolean isRelevantChange(int changeMask) {
        return (changeMask & (IDevice.CHANGE_STATE | IDevice.CHANGE_BUILD_INFO)) != 0;
    }

    public static DispatchingDeviceList create(AdbFacade adb, DeviceProvisioner deviceProvisioner) {
        DispatchingDeviceList result = new DispatchingDeviceList(deviceProvisioner);
        result.init(adb);
        return result;
    }

    private DispatchingDeviceList(DeviceProvisioner deviceProvisioner) {
        this.deviceProvisioner = deviceProvisioner;
    }

    private void init(AdbFacade adb) {
        adb.addDeviceChangeListener(deviceChangeListener);
        IDevice[] knownDevices = adb.getDevices();
        ArrayList<ProvisionalDeviceImpl> devicesToProvision = new ArrayList<>(knownDevices.length);
        synchronized (deviceLock) {
            for (IDevice device : knownDevices) {
                DeviceKey key = DeviceKey.of(device);
                ProvisionalDeviceImpl newDevice = new ProvisionalDeviceImpl(key, device);
                ProvisionalDevice prevDevice = devices.putIfAbsent(key, newDevice);
                if (prevDevice == null) {
                    // The device listener might have added some devices already and started provisioning.
                    devicesToProvision.add(newDevice);
                }
            }
        }
        // No need to notify listeners as this is a part of the initialization.
        for (ProvisionalDeviceImpl device : devicesToProvision) {
            startProvisioning(device);
        }
    }

    /**
     * Returns the list of all connected devices, both provisioned and not.
     *
     * @return the list of all connected devices
     */
    public ImmutableList<ProvisionalDevice> getAllDevices() {
        synchronized (deviceLock) {
            return ImmutableList.copyOf(devices.values());
        }
    }

    /**
     * Returns the list of already provisioned devices.
     *
     * @return the list of provisioned devices
     */
    @SuppressWarnings("StaticPseudoFunctionalStyleMethod")
    public ImmutableList<Device> getDevices() {
        synchronized (deviceLock) {
            return ImmutableList.copyOf(Iterables.filter(devices.values(), Device.class));
        }
    }

    private List<DeviceChangeObserver> getObservers() {
        synchronized (observerLock) {
            return ImmutableList.copyOf(deviceChangeObservers);
        }
    }

    private void notifyProvisionalDeviceConnected(ProvisionalDevice provisionalDevice) {
        logger.debug(formatDeviceLog(provisionalDevice, "notifyProvisionalDeviceConnected"));
        for (DeviceChangeObserver obs : getObservers()) {
            obs.onProvisionalDeviceConnected(provisionalDevice);
        }
    }

    private void notifyDeviceConnected(Device device) {
        logger.debug(formatDeviceLog(device, "notifyDeviceConnected"));
        for (DeviceChangeObserver obs : getObservers()) {
            obs.onDeviceConnected(device);
        }
    }

    private void notifyDeviceDisconnected(ProvisionalDevice device) {
        logger.debug(formatDeviceLog(device, "notifyDeviceDisconnected"));
        for (DeviceChangeObserver obs : getObservers()) {
            obs.onDeviceDisconnected(device);
        }
    }

    private void notifyDeviceChanged(Device device) {
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
    private static String formatDeviceLog(ProvisionalDevice device, @FormatString String format,
            @Nullable Object... args) {
        return ("[" + device.getSerialNumber() + "]: ") + String.format(format, args);
    }

    @FormatMethod
    private static String formatDeviceLog(IDevice device, @FormatString String format, @Nullable Object... args) {
        return ("[" + device.getSerialNumber() + "]: ") + String.format(format, args);
    }

    private void startProvisioning(ProvisionalDeviceImpl device) {
        logger.debug(formatDeviceLog(device, "Started provisioning"));
        deviceProvisioner.provisionDevice(device)
                .thenAccept(
                        provisionedDevice -> finishProvisioning(device, provisionedDevice))
                .exceptionally(exceptionHandler(th -> abandonProvisioning(device, th)));
    }

    private void abandonProvisioning(ProvisionalDeviceImpl device, Throwable exception) {
        logger.debug(formatDeviceLog(device, "Failed to complete device provisioning"), exception);
        synchronized (deviceLock) {
            devices.remove(device.getDeviceKey(), device);
        }
    }

    private void finishProvisioning(ProvisionalDeviceImpl provisionalDevice, DeviceImpl provisionedDevice) {
        logger.debug(formatDeviceLog(provisionalDevice, "Completed provisioning"));
        synchronized (deviceLock) {
            if (devices.replace(provisionalDevice.getDeviceKey(), provisionalDevice, provisionedDevice)) {
                notifyDeviceConnected(provisionedDevice);
            } else {
                assert false :
                        "Provisioned device was replaced before, got " + devices.get(provisionedDevice.getDeviceKey());
            }
        }
    }
}
