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

import name.mlopatkin.andlogview.base.concurrent.SequentialExecutor;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

class AdbServerImpl implements AdbServer {
    // AdbServerImpl wraps the current connection to the adb server. The connection cannot be replaced within the same
    // server instance, a new server instance should be created instead.
    private static final Logger logger = LoggerFactory.getLogger(AdbServerImpl.class);
    private final AdbFacade adbFacade;
    private final DeviceProvisioner deviceProvisioner;

    public AdbServerImpl(File adbExecutable, Executor ioExecutor) throws AdbException {
        adbFacade = new AdbFacadeImpl(createBridge(adbExecutable));
        deviceProvisioner = new DeviceProvisionerImpl(adbFacade, ioExecutor);
    }

    @Override
    public AdbDeviceList getDeviceList(SequentialExecutor listenerExecutor) {
        return DispatchingDeviceList.create(adbFacade, deviceProvisioner, listenerExecutor);
    }

    private static AndroidDebugBridge createBridge(File adbExecutable) throws AdbException {
        logger.info("Starting ADB server");
        logger.debug("ADB server executable: {}", adbExecutable.getAbsolutePath());

        final AndroidDebugBridge bridge;
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

    /**
     * Removes all registered listeners. Mostly useful when tearing down the whole ADB infrastructure.
     */
    public void discardListeners() {
        adbFacade.discardListeners();
    }

    public void stop() {
        deviceProvisioner.close();
        Preconditions.checkState(!adbFacade.hasRegisteredListeners(), "There are leftover listeners");
        AndroidDebugBridge.disconnectBridge();
    }

    private static class AdbFacadeImpl implements AdbFacade {
        private final AndroidDebugBridge bridge;
        private final Set<AndroidDebugBridge.IDeviceChangeListener> listeners = new CopyOnWriteArraySet<>();

        private AdbFacadeImpl(AndroidDebugBridge bridge) {
            this.bridge = bridge;
        }

        @Override
        public IDevice[] getDevices() {
            return bridge.getDevices();
        }

        @Override
        public void addDeviceChangeListener(AndroidDebugBridge.IDeviceChangeListener deviceChangeListener) {
            listeners.add(deviceChangeListener);
            AndroidDebugBridge.addDeviceChangeListener(deviceChangeListener);
        }

        @Override
        public void removeDeviceChangeListener(AndroidDebugBridge.IDeviceChangeListener deviceChangeListener) {
            listeners.remove(deviceChangeListener);
            AndroidDebugBridge.removeDeviceChangeListener(deviceChangeListener);
        }

        @Override
        public boolean hasRegisteredListeners() {
            return !listeners.isEmpty();
        }

        @Override
        public void discardListeners() {
            listeners.forEach(this::removeDeviceChangeListener);
        }
    }
}
