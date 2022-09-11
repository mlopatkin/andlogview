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

import name.mlopatkin.andlogview.utils.MyFutures;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class is responsible for creating an async chain to wait for device to come online and collect required
 * properties, creating an instance of {@link AdbDeviceImpl} at the end.
 */
class DeviceProvisionerImpl implements DeviceProvisioner {
    // 100ms might be too little. I'm leaving an escape hatch in case some devices would be too slow to respond.
    // Don't think it is worth an ui-exposed preference though.
    private static final long PROPERTY_TIMEOUT_MS =
            Long.getLong("andlogview.adb.provision.timeoutMs", 500L);

    private final AdbFacade adb;
    private final Executor provisionalWorker;

    public DeviceProvisionerImpl(AdbFacade adb, Executor provisionalWorker) {
        this.adb = adb;
        this.provisionalWorker = provisionalWorker;
    }

    @Override
    public CompletableFuture<AdbDeviceImpl> provisionDevice(ProvisionalAdbDeviceImpl provisionalDevice) {
        return waitForDeviceToComeOnline(provisionalDevice)
                .thenCompose(onlineDevice ->
                        MyFutures.runAsync(() -> getRequiredProperties(onlineDevice), provisionalWorker))
                .thenApply(deviceProperties -> new AdbDeviceImpl(provisionalDevice.getDeviceKey(),
                        provisionalDevice.getIDevice(), deviceProperties));
    }


    private CompletableFuture<LoggingDevice> waitForDeviceToComeOnline(ProvisionalAdbDeviceImpl provisionalDevice) {
        // ProvisionalAdbDeviceImpl.getIDevice may return a wrapper. We should use it for doing the actual work.
        LoggingDevice ddmlibDevice = provisionalDevice.getIDevice();
        // The device key corresponds to the IDevice from the DDMLIB, we should use it to check if the update is for
        // our device.
        DeviceKey deviceKey = provisionalDevice.getDeviceKey();
        CompletableFuture<LoggingDevice> onlineDevice = new CompletableFuture<>();
        AndroidDebugBridge.IDeviceChangeListener listener = new AndroidDebugBridge.IDeviceChangeListener() {
            @Override
            public void deviceConnected(IDevice device) {
                if (deviceKey.matchesDevice(device)) {
                    if (ddmlibDevice.isOnline()) {
                        onlineDevice.complete(ddmlibDevice);
                    }
                }
            }

            @Override
            public void deviceDisconnected(IDevice device) {
                if (deviceKey.matchesDevice(device)) {
                    onlineDevice.completeExceptionally(new DeviceGoneException());
                }
            }

            @Override
            public void deviceChanged(IDevice device, int changeMask) {
                if (deviceKey.matchesDevice(device) && device.isOnline()) {
                    onlineDevice.complete(ddmlibDevice);
                }
            }
        };
        adb.addDeviceChangeListener(listener);
        if (ddmlibDevice.isOnline()) {
            onlineDevice.complete(ddmlibDevice);
        }
        return onlineDevice.whenComplete((d, th) -> adb.removeDeviceChangeListener(listener));
    }

    private DeviceProperties getRequiredProperties(IDevice ddmlibDevice)
            throws InterruptedException, DeviceGoneException {
        Future<String> product = ddmlibDevice.getSystemProperty(DeviceProperties.PROP_BUILD_PRODUCT);
        Future<String> apiLevel = ddmlibDevice.getSystemProperty(DeviceProperties.PROP_BUILD_API_LEVEL);
        Future<String> codename = ddmlibDevice.getSystemProperty(DeviceProperties.PROP_BUILD_CODENAME);
        Future<String> fingerprint = ddmlibDevice.getSystemProperty(DeviceProperties.PROP_BUILD_FINGERPRINT);

        try {
            return new DeviceProperties(
                    product.get(PROPERTY_TIMEOUT_MS, TimeUnit.MILLISECONDS),
                    apiLevel.get(PROPERTY_TIMEOUT_MS, TimeUnit.MILLISECONDS),
                    codename.get(PROPERTY_TIMEOUT_MS, TimeUnit.MILLISECONDS),
                    fingerprint.get(PROPERTY_TIMEOUT_MS, TimeUnit.MILLISECONDS));
        } catch (ExecutionException e) {
            throw new DeviceGoneException("Failed to obtain device properties", e.getCause());
        } catch (TimeoutException e) {
            throw new DeviceGoneException("Timed out while obtaining device properties", e);
        }
    }
}
