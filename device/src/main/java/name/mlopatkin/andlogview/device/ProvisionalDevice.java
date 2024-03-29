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

/**
 * Provisional device represents device that is connected to the host, but cannot be interacted with. It might be
 * offline from the start or some important information that is available through {@link Device} (like product name
 * or build fingerprint) might be missing. The user can use this class to obtain a corresponding {@code AdbDevice}
 * instance, but this involves waiting on a {@code Future}.
 * <p>
 * Note that the {@code AdbDevice} can become offline as well, it wouldn't be converted to
 * the {@code ProvisionalAdbDevice}.
 */
public interface ProvisionalDevice {
    /**
     * @return the key of the device that doesn't change during the device lifetime
     */
    DeviceKey getDeviceKey();

    /**
     * @return the serial number of the connected device
     */
    String getSerialNumber();

    /**
     * @return the user-friendly name of the device with unique id
     */
    String getDisplayName();

    /**
     * @return {@code true} if the device is online and can run commands
     */
    boolean isOnline();
}
