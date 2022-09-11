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

import com.android.ddmlib.IDevice;

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
    // TODO(mlopatkin) serial numbers aren't unique in reality, especially in case of the low-end phones with
    //  low-quality firmware. It is better to use something like usb address but DDMLIB doesn't expose it.
    /**
     * @return the serial number of the connected device
     */
    String getSerialNumber();

    /**
     * Do not use in new code. This method is intended to be used during transitional period.
     *
     * @return the DDMLIB's IDevice instance
     */
    IDevice getIDevice();

    /**
     * @return the user-friendly name of the device with unique id
     */
    String getDisplayName();
}
