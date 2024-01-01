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

import static name.mlopatkin.andlogview.utils.MyFutures.errorHandler;

import name.mlopatkin.andlogview.base.concurrent.SequentialExecutor;
import name.mlopatkin.andlogview.utils.MyFutures;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This is the primary dispatcher for AdbDeviceList implementations.
 */
class DispatchingDeviceList implements AdbDeviceList {
    private static final Logger logger = Logger.getLogger(DispatchingDeviceList.class);

    private final AdbFacade adb;
    private final DeviceProvisioner deviceProvisioner;
    private final SequentialExecutor listExecutor;

    private final LinkedHashMap<DeviceKey, ProvisionalDeviceInternal> devices = new LinkedHashMap<>();
    private final Subject<DeviceChangeObserver> deviceChangeObservers = new Subject<>();

    private final AndroidDebugBridge.IDeviceChangeListener deviceChangeListener =
            new AndroidDebugBridge.IDeviceChangeListener() {
                @Override
                public void deviceConnected(IDevice device) {
                    listExecutor.execute(() -> handleDeviceConnected(device));
                }

                @Override
                public void deviceDisconnected(IDevice device) {
                    listExecutor.execute(() -> handleDeviceDisconnected(device));
                }

                @Override
                public void deviceChanged(IDevice device, int changeMask) {
                    listExecutor.execute(() -> handleDeviceChanged(device, changeMask));
                }
            };

    private void handleDeviceChanged(IDevice device, int changeMask) {
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

        var adbDevice = devices.get(DeviceKey.of(device));
        if (adbDevice != null) {
            if (adbDevice instanceof DeviceInternal adbDeviceInternal) {
                notifyDeviceChanged(adbDeviceInternal);
            }
        } else {
            // We don't know about this device yet.
            handleDeviceConnected(device);
        }
    }

    private void handleDeviceDisconnected(IDevice device) {
        logger.debug(formatDeviceLog(device, "disconnected"));
        var adbDevice = devices.remove(DeviceKey.of(device));
        if (adbDevice != null) {
            notifyDeviceDisconnected(adbDevice);
        }
        // The pending provision job, if any, will complete itself abnormally.
    }

    private void handleDeviceConnected(IDevice device) {
        logger.debug(formatDeviceLog(device, "connected"));
        var newDevice = new ProvisionalDeviceImpl(DeviceKey.of(device), device);
        var prevDevice = devices.putIfAbsent(newDevice.getDeviceKey(), newDevice);
        if (prevDevice == null) {
            notifyProvisionalDeviceConnected(newDevice);
            startProvisioning(newDevice);
        } else {
            logger.debug(formatDeviceLog(device, "device is already known as %s", prevDevice));
            // We already have this device in our list, maybe some races between listener and initializing
            // the initial list. The device might be being provisioned, though.
            if (prevDevice instanceof DeviceInternal prevDeviceInternal) {
                notifyDeviceChanged(prevDeviceInternal);
            }
        }
    }

    private boolean isRelevantChange(int changeMask) {
        return (changeMask & (IDevice.CHANGE_STATE | IDevice.CHANGE_BUILD_INFO)) != 0;
    }

    public static DispatchingDeviceList create(
            AdbFacade adb,
            DeviceProvisioner deviceProvisioner,
            SequentialExecutor listExecutor) {
        listExecutor.checkSequence();
        DispatchingDeviceList result = new DispatchingDeviceList(adb, deviceProvisioner, listExecutor);
        result.init();
        return result;
    }

    private DispatchingDeviceList(AdbFacade adb, DeviceProvisioner deviceProvisioner, SequentialExecutor listExecutor) {
        this.adb = adb;
        this.deviceProvisioner = deviceProvisioner;
        this.listExecutor = listExecutor;
    }

    private void init() {
        adb.addDeviceChangeListener(deviceChangeListener);

        IDevice[] knownDevices = adb.getDevices();
        ArrayList<ProvisionalDeviceImpl> devicesToProvision = new ArrayList<>(knownDevices.length);
        for (IDevice device : knownDevices) {
            DeviceKey key = DeviceKey.of(device);
            ProvisionalDeviceImpl newDevice = new ProvisionalDeviceImpl(key, device);
            ProvisionalDevice prevDevice = devices.putIfAbsent(key, newDevice);
            if (prevDevice == null) {
                // The device listener might have added some devices already and started provisioning.
                devicesToProvision.add(newDevice);
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
    @Override
    public ImmutableList<ProvisionalDevice> getAllDevices() {
        listExecutor.checkSequence();
        return ImmutableList.copyOf(devices.values());
    }

    /**
     * Returns the list of already provisioned devices.
     *
     * @return the list of provisioned devices
     */
    @SuppressWarnings("StaticPseudoFunctionalStyleMethod")
    @Override
    public ImmutableList<Device> getDevices() {
        listExecutor.checkSequence();
        return ImmutableList.copyOf(Iterables.filter(devices.values(), Device.class));
    }

    private void notifyProvisionalDeviceConnected(ProvisionalDeviceInternal provisionalDevice) {
        logger.debug(formatDeviceLog(provisionalDevice, "notifyProvisionalDeviceConnected"));
        for (DeviceChangeObserver obs : deviceChangeObservers) {
            obs.onProvisionalDeviceConnected(provisionalDevice);
        }
        provisionalDevice.notifyConnected();
    }

    private void notifyDeviceConnected(DeviceInternal device) {
        logger.debug(formatDeviceLog(device, "notifyDeviceConnected"));
        for (DeviceChangeObserver obs : deviceChangeObservers) {
            obs.onDeviceConnected(device);
        }
        device.notifyProvisioned();
    }

    private void notifyDeviceDisconnected(ProvisionalDeviceInternal device) {
        logger.debug(formatDeviceLog(device, "notifyDeviceDisconnected"));
        for (DeviceChangeObserver obs : deviceChangeObservers) {
            obs.onDeviceDisconnected(device);
        }
        device.notifyDisconnected();
    }

    private void notifyDeviceChanged(DeviceInternal device) {
        logger.debug(formatDeviceLog(device, "notifyDeviceChanged (online=%s)", device.isOnline()));
        for (DeviceChangeObserver obs : deviceChangeObservers) {
            obs.onDeviceChanged(device);
        }
        device.notifyChanged();
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
                .thenAcceptAsync(
                        provisionedDevice -> finishProvisioning(device, provisionedDevice),
                        listExecutor)
                .handleAsync(errorHandler(th -> abandonProvisioning(device, th)), listExecutor)
                .exceptionally(MyFutures::uncaughtException);
    }

    private void abandonProvisioning(ProvisionalDeviceImpl device, Throwable exception) {
        listExecutor.checkSequence();
        logger.debug(formatDeviceLog(device, "Failed to complete device provisioning"), exception);
        devices.remove(device.getDeviceKey(), device);
        // TODO(mlopatkin) We don't notify clients about the failed provisioning. Is it a problem besides having a
        //  disabled entry in the device list?
    }

    private void finishProvisioning(ProvisionalDeviceImpl provisionalDevice, DeviceImpl provisionedDevice) {
        logger.debug(formatDeviceLog(provisionalDevice, "Completed provisioning"));
        if (devices.replace(provisionalDevice.getDeviceKey(), provisionalDevice, provisionedDevice)) {
            // The device list might be already closed.
            notifyDeviceConnected(provisionedDevice);
        }
    }

    @Override
    public void close() {
        listExecutor.checkSequence();
        adb.removeDeviceChangeListener(deviceChangeListener);

        var currentDevices = ImmutableList.copyOf(devices.values());
        devices.clear();

        for (var device : currentDevices) {
            notifyDeviceDisconnected(device);
        }

        deviceChangeObservers.clear();
    }

    @Override
    public Observable<DeviceChangeObserver> asObservable() {
        listExecutor.checkSequence();
        return deviceChangeObservers.asObservable();
    }
}
