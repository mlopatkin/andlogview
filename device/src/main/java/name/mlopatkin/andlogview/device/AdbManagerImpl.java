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

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.Log;
import com.google.errorprone.annotations.concurrent.GuardedBy;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

class AdbManagerImpl implements AdbManager {
    // AdbManagerImpl is responsible for the state of the DDMLIB as a whole. The manager lazily initializes the library
    // when the AdbServer is first requested.
    private static final Logger logger = Logger.getLogger(AdbManagerImpl.class);

    private final AtExitManager atExitManager;

    private final Object lock = new Object();

    @GuardedBy("lock")
    private boolean initialized;
    @Nullable
    private volatile AdbServerImpl server;

    @GuardedBy("lock")
    private AdbLocation adbLocation;

    AdbManagerImpl(AtExitManager atExitManager, AdbLocation initialAdbLocation) {
        this.atExitManager = atExitManager;
        adbLocation = initialAdbLocation;
    }

    @Override
    public void setAdbLocation(AdbLocation adbLocation) throws AdbException {
        synchronized (lock) {
            this.adbLocation = adbLocation;
            AdbServerImpl theServer = server;
            if (theServer != null) {
                theServer.updateConnection(adbLocation);
            }
        }
    }

    @Override
    public AdbServer startServer() throws AdbException {
        synchronized (lock) {
            AdbServerImpl theServer = server;
            if (theServer != null) {
                return theServer;
            }
            return server = createServerLocked();
        }
    }

    @Override
    public Optional<AdbServer> getRunningServer() {
        return Optional.ofNullable(server);
    }

    @GuardedBy("lock")
    private AdbServerImpl createServerLocked() throws AdbException {
        initIfNeededLocked();
        return new AdbServerImpl(adbLocation);
    }

    @GuardedBy("lock")
    private void initIfNeededLocked() {
        if (initialized) {
            return;
        }
        logger.debug("Initializing DDMLIB");
        atExitManager.registerExitAction(this::terminate);
        initializeDdmlibLoggingLocked();
        AndroidDebugBridge.initIfNeeded(/* clientSupport */ false);
        logger.debug("DDMLIB initialized");
        initialized = true;
    }

    @GuardedBy("lock")
    private void initializeDdmlibLoggingLocked() {
        Log.addLogger(new DdmlibToLog4jWrapper());
        if (System.getProperty("logview.debug.ddmlib") != null) {
            DdmPreferences.setLogLevel("debug");
        }
    }

    private void terminate() {
        synchronized (lock) {
            logger.info("Tear down DDMLIB");
            AndroidDebugBridge.terminate();
        }
    }
}
