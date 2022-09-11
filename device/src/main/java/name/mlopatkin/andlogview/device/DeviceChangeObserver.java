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

import java.util.Objects;

/**
 * The device change observer.
 */
public interface DeviceChangeObserver {
    /**
     * Called when the new device is connected, but before it was provisioned
     *
     * @param device the connected device
     */
    default void onProvisionalDeviceConnected(ProvisionalDevice device) {}

    /**
     * Called when the new device is connected and provisioned.
     *
     * @param device the connected device
     */
    default void onDeviceConnected(Device device) {}

    /**
     * Called when the device is disconnected
     *
     * @param device the disconnected device
     */
    default void onDeviceDisconnected(Device device) {}

    /**
     * Called when the device status is changed.
     *
     * @param device the changed device
     */
    default void onDeviceChanged(Device device) {}

    /**
     * Creates a new observer that forwards events from only the given device to this observer.
     *
     * @param trackedDevice the device to get events from
     * @return the new observer that only forwards events for the given device
     */
    default DeviceChangeObserver scopeToSingleDevice(ProvisionalDevice trackedDevice) {
        DeviceChangeObserver parent = this;
        return new DeviceChangeObserver() {
            @Override
            public void onProvisionalDeviceConnected(ProvisionalDevice device) {
                if (isTrackedDevice(device)) {
                    parent.onProvisionalDeviceConnected(device);
                }
            }

            @Override
            public void onDeviceConnected(Device device) {
                if (isTrackedDevice(device)) {
                    parent.onDeviceConnected(device);
                }
            }

            @Override
            public void onDeviceDisconnected(Device device) {
                if (isTrackedDevice(device)) {
                    parent.onDeviceDisconnected(device);
                }
            }

            @Override
            public void onDeviceChanged(Device device) {
                if (isTrackedDevice(device)) {
                    parent.onDeviceChanged(device);
                }
            }

            @Override
            public DeviceChangeObserver scopeToSingleDevice(ProvisionalDevice anotherTrackedDevice) {
                if (!isTrackedDevice(anotherTrackedDevice)) {
                    throw new IllegalArgumentException("Can't scope an already scoped observer");
                }
                return this;
            }

            private boolean isTrackedDevice(ProvisionalDevice device) {
                return Objects.equals(trackedDevice.getSerialNumber(), device.getSerialNumber());
            }
        };
    }
}
