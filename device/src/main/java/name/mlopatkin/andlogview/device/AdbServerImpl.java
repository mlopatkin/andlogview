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
import com.android.ddmlib.IDevice;
import com.google.common.base.MoreObjects;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

class AdbServerImpl implements AdbServer, AdbFacade {
    // AdbServerImpl wraps the current connection to the adb server. The connection cannot be replaced within the same
    // server instance, a new server instance should be created instead.
    private static final Logger logger = Logger.getLogger(AdbServerImpl.class);

    private final AndroidDebugBridge currentBridge;
    private final LazyInstance<DispatchingDeviceList> dispatchingDeviceList;
    private final List<AdbBridgeObserver> bridgeObservers = new CopyOnWriteArrayList<>();

    public AdbServerImpl(AdbLocation adbLocation, Executor ioExecutor) throws AdbException {
        currentBridge = createBridge(adbLocation);
        dispatchingDeviceList =
                lazy(() -> DispatchingDeviceList.create(this, new DeviceProvisionerImpl(this, ioExecutor)));
    }

    @Override
    public AdbDeviceList getDeviceList(Executor listenerExecutor) {
        return new AdbDeviceListImpl(dispatchingDeviceList.get(), listenerExecutor);
    }

    private static AndroidDebugBridge createBridge(AdbLocation adbLocation) throws AdbException {
        logger.info("Starting ADB server");
        File adbExecutable = adbLocation.getExecutable()
                .orElseThrow(
                        () -> new AdbException("ADB location '" + adbLocation.getExecutableString() + "' is invalid"));
        logger.debug("ADB server executable: " + adbExecutable.getAbsolutePath());
        final @Nullable AndroidDebugBridge bridge;
        try {
            bridge = AndroidDebugBridge.createBridge(adbExecutable.getAbsolutePath(), false);
        } catch (IllegalArgumentException e) {
            // Error handling in DDMLIB is a mess.
            if (e.getCause() instanceof IOException ioException) {
                throw new AdbException(
                        String.format(
                                "Cannot start ADB at '%s':\n%s",
                                adbExecutable.getAbsolutePath(),
                                MoreObjects.firstNonNull(ioException.getMessage(), "I/O error")),
                        ioException
                );
            }
            throw new AdbException(
                    String.format(
                            "Failed to initialize ADB at '%s':\n%s",
                            adbExecutable.getAbsolutePath(),
                            MoreObjects.firstNonNull(e.getMessage(), "unknown error, see logs")
                    ),
                    e
            );
        }
        if (bridge == null) {
            throw new AdbException("Failed to create the bridge at " + adbExecutable);
        }
        if (!isBridgeReady(bridge)) {
            throw new AdbException("Bridge is not ready");
        }
        return bridge;
    }

    private static boolean isBridgeReady(AndroidDebugBridge adb) {
        // hack below - there is no explicit way to check if the bridge was created successfully. I don't remember why
        // bridge creation can fail though.
        try {
            Field started = AndroidDebugBridge.class.getDeclaredField("mStarted");
            started.setAccessible(true);
            return started.getBoolean(adb);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new AssertionError("This DDMLIB is not supported");
        }
    }

    private AndroidDebugBridge getBridge() {
        return currentBridge;
    }

    @Override
    public IDevice[] getDevices() {
        return getBridge().getDevices();
    }

    @Override
    public void addDeviceChangeListener(AndroidDebugBridge.IDeviceChangeListener deviceChangeListener) {
        AndroidDebugBridge.addDeviceChangeListener(deviceChangeListener);
    }

    @Override
    public void removeDeviceChangeListener(AndroidDebugBridge.IDeviceChangeListener deviceChangeListener) {
        AndroidDebugBridge.removeDeviceChangeListener(deviceChangeListener);
    }

    @Override
    public void addBridgeObserver(AdbBridgeObserver observer) {
        bridgeObservers.add(observer);
    }

    @Override
    public void removeBridgeObserver(AdbBridgeObserver observer) {
        bridgeObservers.remove(observer);
    }

    public void stop() {
        for (AdbBridgeObserver observer : bridgeObservers) {
            observer.onAdbBridgeClosed();
        }
        AndroidDebugBridge.disconnectBridge();
    }
}
