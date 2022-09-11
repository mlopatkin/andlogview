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
import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * A holder for the {@link IDevice} instance that adds equality check for them.
 * It should only be created from the original IDevice implementations provided by the DDMLIB, not the wrappers.
 */
public final class DeviceKey {
    private final IDevice device;

    private DeviceKey(IDevice device) {
        Preconditions.checkArgument(!(device instanceof DelegatingDevice),
                "Do not create keys out of delegating devices, got %s", device);
        this.device = device;
    }

    /**
     * Checks if this key corresponds to {@code otherDevice}. The {@code otherDevice} instance should be provided by
     * DDMLIB.
     * <p>
     * This is an equivalent of {@code equals(DeviceKey.of(otherDevice)}, but saves an allocation.
     *
     * @param otherDevice the device to check
     * @return {@code true} if the {@code otherDevice} matches this key
     * @throws IllegalArgumentException if the {@code otherDevice} is of unsupported type, e.g. a wrapper
     */
    public boolean matchesDevice(IDevice otherDevice) {
        Preconditions.checkArgument(device.getClass() == otherDevice.getClass(),
                "Unexpected device implementation for check, expected %s, got %s", device.getClass(),
                otherDevice.getClass());
        return Objects.equals(device, otherDevice);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceKey deviceKey = (DeviceKey) o;
        return Objects.equals(device, deviceKey.device);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device);
    }

    /**
     * Creates a device key of the given device. The device instance should be provided by DDMLIB.
     *
     * @param device the device provided by the DDMLIB to create key of
     * @return the key of the device
     * @throws IllegalArgumentException if the device is of unsupported type, e.g. a wrapper
     */
    static DeviceKey of(IDevice device) {
        return new DeviceKey(device);
    }
}
