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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Device or emulator that is connected via ADB.
 */
public interface AdbDevice extends ProvisionalAdbDevice {
    /** @return the user-friendly name of the device */
    String getName();

    /** @return the product string of the device ({@code android.os.Build.PRODUCT}) */
    @Nullable
    String getProduct();

    /** @return the build fingerprint string of the device ({@code android.os.Build.FINGERPRINT}) */
    @Nullable
    String getBuildFingerprint();

    /**
     * @return the api version of the device ({@code android.os.Build.VERSION.SDK_INT} or {@code
     *         android.os.Build.VERSION.CODENAME} if this is a pre-release)
     */
    String getApiString();

    /**
     * Creates a {@link Command} to run a command on this device.
     *
     * @param commandLine the command line as a series of arguments
     * @return the command to configure and/or run
     */
    Command command(List<String> commandLine);


    /**
     * @return {@code true} if the device is online and can run commands
     */
    boolean isOnline();

    /**
     * Creates a {@link Command} to run a command on this device.
     *
     * @param commandLine the command line as a series of arguments
     * @return the builder to configure and/or run command
     */
    default Command command(String... commandLine) {
        return command(Arrays.asList(commandLine));
    }

    @Override
    default CompletableFuture<AdbDevice> getProvisionedDevice() {
        return CompletableFuture.completedFuture(this);
    }
}
