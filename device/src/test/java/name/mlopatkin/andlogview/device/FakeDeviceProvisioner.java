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

package name.mlopatkin.andlogview.device;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

class FakeDeviceProvisioner implements DeviceProvisioner {
    private final Map<String, CompletableFuture<?>> provisioningDevices = new ConcurrentHashMap<>();

    public void completeDeviceProvisioning(String deviceSerial) {
        var deviceFuture = provisioningDevices.remove(deviceSerial);
        if (deviceFuture == null) {
            throw new IllegalArgumentException("Device with serial " + deviceSerial + " is not being provisioned");
        }
        deviceFuture.complete(null);
    }

    public void failDeviceProvisioning(String deviceSerial) {
        var deviceFuture = provisioningDevices.remove(deviceSerial);
        if (deviceFuture == null) {
            throw new IllegalArgumentException("Device with serial " + deviceSerial + " is not being provisioned");
        }
        deviceFuture.completeExceptionally(new DeviceGoneException("Device " + deviceSerial + "is disconnected"));
    }

    @Override
    public CompletableFuture<DeviceImpl> provisionDevice(ProvisionalDeviceImpl provisionalDevice) {
        CompletableFuture<?> provision = new CompletableFuture<>();
        if (provisioningDevices.putIfAbsent(provisionalDevice.getSerialNumber(), provision) != null) {
            throw new IllegalArgumentException(
                    "Device " + provisionalDevice.getSerialNumber() + " is already being provisioned");
        }
        return provision.thenApply(ignored ->
                new DeviceImpl(
                        provisionalDevice.getDeviceKey(),
                        provisionalDevice.getIDevice(),
                        new DeviceProperties(
                                "product", "30", null, "fingerprint"))
        );
    }
}
