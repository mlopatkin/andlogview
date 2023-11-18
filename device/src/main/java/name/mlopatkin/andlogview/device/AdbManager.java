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

import java.util.concurrent.Executor;

/**
 * This class represents the global state of the DDMLIB library. The library is initialized lazily, so one could safely
 * obtain an instance of the manager without worrying about bringing up ADB.
 * <p>
 * The implementation is thread-safe.
 */
public interface AdbManager {
    /**
     * Ensures that the ADB server is running and sets up the connection. If the server isn't running then it is
     * started, otherwise the running server is stopped and a new one starts. This method may block for a significant
     * period of time.
     *
     * @param adbLocation the ADB location to use when starting server
     * @return the server
     * @throws AdbException if the server cannot be started
     */
    AdbServer startServer(AdbLocation adbLocation) throws AdbException;

    /**
     * Creates an instance of the AdbManager but doesn't initialize the DDMLIB yet.
     *
     * @param atExitManager the {@link AtExitManager} to register cleanup hooks
     * @param ioExecutor the executor for non-CPU intensive blocking background work
     * @return the AdbManager
     */
    static AdbManager create(AtExitManager atExitManager, Executor ioExecutor) {
        return new AdbManagerImpl(atExitManager, ioExecutor);
    }
}
