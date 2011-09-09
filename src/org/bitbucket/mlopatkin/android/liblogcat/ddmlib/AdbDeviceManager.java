/*
 * Copyright 2011 Mikhail Lopatkin
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
package org.bitbucket.mlopatkin.android.liblogcat.ddmlib;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;

public class AdbDeviceManager {
    private AdbDeviceManager() {
    }

    public static void addDeviceChangeListener(IDeviceChangeListener listener) {
        AdbConnectionManager.getAdb();
        AndroidDebugBridge.addDeviceChangeListener(listener);
    }

    public static void removeDeviceChangeListener(IDeviceChangeListener listener) {
        AndroidDebugBridge.removeDeviceChangeListener(listener);
    }

    public static List<IDevice> getAvailableDevices() {
        AndroidDebugBridge adb = AdbConnectionManager.getAdb();
        if (adb.hasInitialDeviceList()) {
            return Arrays.asList(adb.getDevices());
        } else {
            return Collections.emptyList();
        }
    }

    public static IDevice getDefaultDevice() {
        AndroidDebugBridge adb = AdbConnectionManager.getAdb();
        if (adb.hasInitialDeviceList() && adb.getDevices().length > 0) {
            return adb.getDevices()[0];
        } else {
            return null;
        }
    }

    public static abstract class AbstractDeviceListener implements IDeviceChangeListener {

        @Override
        public void deviceConnected(IDevice device) {
        }

        @Override
        public void deviceDisconnected(IDevice device) {
        }

        @Override
        public void deviceChanged(IDevice device, int changeMask) {
        }

    }

    private static final String PRODUCT_NAME_PROPERTY = "ro.build.product";

    public static String getDeviceDisplayName(IDevice device) {
        StringBuilder deviceName = new StringBuilder(device.getSerialNumber());
        String productName = null;
        if (device.isEmulator()) {
            productName = device.getAvdName();
        } else {
            productName = device.getProperty(PRODUCT_NAME_PROPERTY);

        }
        if (productName != null) {
            deviceName.append(' ').append(productName);
        }
        return deviceName.toString();
    }
}
