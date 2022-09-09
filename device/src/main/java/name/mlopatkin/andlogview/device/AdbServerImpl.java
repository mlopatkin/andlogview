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

import static name.mlopatkin.andlogview.utils.LazyInstance.lazy;

import name.mlopatkin.andlogview.utils.LazyInstance;

import com.android.ddmlib.AndroidDebugBridge;
import com.google.errorprone.annotations.concurrent.GuardedBy;

import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.Executor;

class AdbServerImpl implements AdbServer, Closeable {
    private static final Logger logger = Logger.getLogger(AdbServerImpl.class);

    private final Object lock = new Object();

    @GuardedBy("lock")
    private AdbConnectionImpl adbConnection;
    private final LazyInstance<DispatchingDeviceList> dispatchingDeviceList =
            lazy(() -> DispatchingDeviceList.create(this));

    public AdbServerImpl(AdbLocation adbLocation) throws AdbException {
        synchronized (lock) {
            adbConnection = createConnectionLocked(adbLocation);
        }
    }

    @Override
    public AdbDeviceList getDeviceList(Executor listenerExecutor) {
        return new AdbDeviceListImpl(dispatchingDeviceList.get(), listenerExecutor);
    }

    @Override
    public AdbConnection getConnection() {
        synchronized (lock) {
            return adbConnection;
        }
    }

    public void updateConnection(AdbLocation adbLocation) throws AdbException {
        synchronized (lock) {
            adbConnection.close();
            adbConnection = createConnectionLocked(adbLocation);
        }
    }

    @GuardedBy("lock")
    private AdbConnectionImpl createConnectionLocked(AdbLocation adbLocation) throws AdbException {
        logger.info("Starting ADB server");
        File adbExecutable = adbLocation.getExecutable().orElseThrow(() -> new AdbException("ADB location is invalid"));
        logger.debug("ADB server executable: " + adbExecutable.getAbsolutePath());
        AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(adbExecutable.getAbsolutePath(), false);
        if (!isBridgeReady(bridge)) {
            throw new AdbException("Bridge is not ready");
        }
        return new AdbConnectionImpl(bridge);
    }

    private static boolean isBridgeReady(AndroidDebugBridge adb) {
        // hack below - there is no explicit way to check if the bridge was created successfully. I don't remember why
        // bridge creation can fail though.
        try {
            Field started = AndroidDebugBridge.class.getDeclaredField("mStarted");
            started.setAccessible(true);
            return started.getBoolean(adb);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new AssertionError("DDMLIB is not supported");
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            adbConnection.close();
        }
    }

    @SuppressWarnings("deprecation")
    public AndroidDebugBridge getBridge() {
        synchronized (lock) {
            return adbConnection.getBridge();
        }
    }
}
