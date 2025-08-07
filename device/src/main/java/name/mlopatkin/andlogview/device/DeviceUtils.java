/*
 * Copyright 2021 Mikhail Lopatkin
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

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Collection of utility wrappers to work with exception-rich DDMLIB methods.
 */
final class DeviceUtils {
    private DeviceUtils() {}

    /**
     * Executes a shell command on the provided device without any timeouts. Unlike the method of the {@link IDevice}
     * with the same signature, no default timeouts are applied.
     *
     * @param device the device to run command on
     * @param command the command to run
     * @param receiver the optional receiver for the command output
     * @throws InterruptedException if the thread is interrupted
     * @throws DeviceGoneException if the connection to the device is lost or the device doesn't complete
     *         command fast enough
     */
    public static void executeShellCommand(IDevice device, String command, @Nullable IShellOutputReceiver receiver)
            throws InterruptedException, DeviceGoneException {
        executeShellCommand(device, command, receiver, 0L, TimeUnit.SECONDS);
    }

    /**
     * Executes a shell command on the provided device with a time limit. The method is intended to be used for the fast
     * commands where timeout likely indicates that the device is stuck.
     *
     * @param device the device to run command on
     * @param command the command to run
     * @param receiver the optional receiver for the command output
     * @param executionTimeout the total execution timeout for the command (0 means no limit)
     * @param timeUnit the unit of the executionTimeout (if non-zero)
     * @throws InterruptedException if the thread is interrupted
     * @throws DeviceGoneException if the connection to the device is lost or the device doesn't complete
     *         command fast enough
     */
    public static void executeShellCommand(IDevice device, String command, @Nullable IShellOutputReceiver receiver,
            long executionTimeout, TimeUnit timeUnit) throws InterruptedException, DeviceGoneException {
        try {
            device.executeShellCommand(command, receiver, executionTimeout, 0L, timeUnit);
        } catch (TimeoutException e) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Command '" + command + "' interrupted");
            }
            throw new DeviceGoneException("Command failed to complete in time", e);
        } catch (AdbCommandRejectedException e) {
            throw new DeviceGoneException("Device is gone or ADB host is ill", e);
        } catch (ShellCommandUnresponsiveException e) {
            throw new AssertionError("Unexpected unresponsive exception");
        } catch (IOException e) {
            throw new DeviceGoneException("ADB connection is broken", e);
        }
    }
}
