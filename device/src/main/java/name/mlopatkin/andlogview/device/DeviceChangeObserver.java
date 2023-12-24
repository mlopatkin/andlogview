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
    default void onDeviceDisconnected(ProvisionalDevice device) {}

    /**
     * Called when the device status is changed.
     *
     * @param device the changed device
     */
    default void onDeviceChanged(Device device) {}
}
