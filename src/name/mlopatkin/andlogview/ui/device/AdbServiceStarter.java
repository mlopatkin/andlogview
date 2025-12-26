/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.device;

import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.device.AdbManager;
import name.mlopatkin.andlogview.device.AdbServer;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Starts the ADB service.
 * <p>
 * This class is thread-safe.
 */
class AdbServiceStarter {
    private static final Logger log = LoggerFactory.getLogger(AdbServiceStarter.class);

    private final AdbManager adbManager;
    private final AdbConfigurationPref adbPref;
    private final Supplier<Stream<File>> commonAdbLocations;

    public AdbServiceStarter(
            AdbManager adbManager,
            AdbConfigurationPref adbPref,
            Supplier<Stream<File>> commonAdbLocations
    ) {
        this.adbManager = adbManager;
        this.adbPref = adbPref;
        this.commonAdbLocations = commonAdbLocations;
    }

    /**
     * Returns the full path of the ADB executable to use. Throws an exception if the currently set up location is
     * invalid, i.e. doesn't represent an executable file.
     *
     * @return the absolute path to the executable
     * @throws AdbException if the executable cannot be found
     */
    private File resolveAdbExecutable() throws AdbException {
        if (!adbPref.hasValidAdbLocation() && adbPref.isAdbAutoDiscoveryAllowed()) {
            var iter = commonAdbLocations.get().iterator();
            // Stop early if we get an explicit location from somewhere.
            while (iter.hasNext() && adbPref.isAdbAutoDiscoveryAllowed()) {
                var candidate = iter.next().getAbsolutePath();
                log.debug("Trying to resolve ADB in {}", candidate);
                if (adbPref.trySetAutoDiscoveredLocation(candidate)) {
                    log.info("Automatically discovered ADB in {}", candidate);
                    break;
                }
            }
        }

        return adbPref.getExecutable().orElseThrow(() ->
                new AdbException("Provided ADB location '" + adbPref.getAdbLocation() + "' is invalid")
        );
    }

    /**
     * Starts the ADB server based on location currently set in preferences. This method may take non-trivial time.
     *
     * @return the started ADB server
     * @throws AdbException if the ADB server cannot start
     */
    public AdbServer startAdb() throws AdbException {
        return adbManager.startServer(resolveAdbExecutable());
    }
}
