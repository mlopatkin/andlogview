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

import name.mlopatkin.andlogview.base.AtExitManager;

import java.util.Optional;

/**
 * This class represents the global state of the DDMLIB library. The library is initialized lazily, so one could safely
 * obtain an instance of the manager without worrying about bringing up ADB.
 * <p>
 * The implementation is thread-safe.
 */
public interface AdbManager {

    /**
     * Updates the current location of the ADB executable. If there is an active {@link AdbServer} then it is
     * restarted. This method may block for a significant period of time.
     *
     * @param adbLocation the new location of the ADB executable
     * @throws AdbException if the server cannot be started in the new location
     */
    void setAdbLocation(AdbLocation adbLocation) throws AdbException;

    /**
     * Ensures that the ADB server is running and sets up the connection. If the server isn't running then it is
     * started. If the running server doesn't match the configured executable then it is killed and a new matching one
     * os started. This method may block for a significant period of time.
     *
     * @return the server
     * @throws AdbException if the server cannot be started
     */
    AdbServer startServer() throws AdbException;

    /**
     * Returns the current running AdbServer if any. Doesn't start the server if it isn't started already. This method
     * doesn't block.
     *
     * @return the current started server or empty Optional if it isn't running
     */
    Optional<AdbServer> getRunningServer();

    /**
     * Creates an instance of the AdbManager but doesn't initialize the DDMLIB yet.
     *
     * @param atExitManager the {@link AtExitManager} to register cleanup hooks
     * @param initialAdbLocation the ADB location to use when starting server
     *
     * @return the AdbManager
     */
    static AdbManager create(AtExitManager atExitManager, AdbLocation initialAdbLocation) {
        return new AdbManagerImpl(atExitManager, initialAdbLocation);
    }
}
