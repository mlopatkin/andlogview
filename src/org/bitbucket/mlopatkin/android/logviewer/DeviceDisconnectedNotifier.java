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
package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.EventQueue;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDeviceManager;

import com.android.ddmlib.IDevice;

/**
 * This class is responsible for showing notification dialog when device is
 * disconnected.
 * 
 */
public class DeviceDisconnectedNotifier extends AdbDeviceManager.AbstractDeviceListener {
    private static final Logger logger = Logger.getLogger(DeviceDisconnectedNotifier.class);
    private IDevice device;

    private DeviceDisconnectedNotifier(IDevice device) {
        this.device = device;
    }

    @Override
    public void deviceDisconnected(IDevice device) {
        if (device == this.device) {
            showNotification();
            // one-shot
            AdbDeviceManager.removeDeviceChangeListener(this);
        }
    }

    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        if (device == this.device && (changeMask & IDevice.CHANGE_STATE) != 0) {
            if (!device.isOnline()) {
                showNotification();
                AdbDeviceManager.removeDeviceChangeListener(this);
            }
        }
    }

    private void showNotification() {
        logger.debug("showNotification");
        EventQueue.invokeLater(notificationInvoker);
    }

    private Runnable notificationInvoker = new Runnable() {
        @Override
        public void run() {
            logger.debug("show notification dialog");
            JOptionPane.showMessageDialog(null, "Device is disconnected", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            logger.debug("close notification dialog");
        }
    };

    public static void startWatching(IDevice device) {
        AdbDeviceManager.addDeviceChangeListener(new DeviceDisconnectedNotifier(device));
    }
}
