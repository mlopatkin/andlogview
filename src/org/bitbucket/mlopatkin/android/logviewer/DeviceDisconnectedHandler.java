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

import com.android.ddmlib.IDevice;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDeviceManager;
import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;

import java.awt.EventQueue;

import javax.swing.JOptionPane;

/**
 * This class is responsible for showing notification dialog when device is
 * disconnected.
 *
 */
public class DeviceDisconnectedHandler extends AdbDeviceManager.AbstractDeviceListener {
    private static final Logger logger = Logger.getLogger(DeviceDisconnectedHandler.class);
    private final IDevice device;
    private final MainFrame mainFrame;

    private DeviceDisconnectedHandler(MainFrame mainFrame, IDevice device) {
        this.mainFrame = mainFrame;
        this.device = device;
    }

    @Override
    public void deviceDisconnected(IDevice device) {
        if (device == this.device) {
            onDeviceDisconnected(disconnectedInvoker);
            // one-shot
            AdbDeviceManager.removeDeviceChangeListener(this);
        }
    }

    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        if (device == this.device && (changeMask & IDevice.CHANGE_STATE) != 0) {
            if (!device.isOnline()) {
                onDeviceDisconnected(offlineInvoker);
                AdbDeviceManager.removeDeviceChangeListener(this);
            }
        }
    }

    private void onDeviceDisconnected(Runnable notificationInvoker) {
        logger.debug("showNotification");
        if (Configuration.adb.isAutoReconnectEnabled()) {
            mainFrame.waitForDevice();
        } else {
            EventQueue.invokeLater(notificationInvoker);
        }
    }

    private void showNotificationDialog(String message) {
        assert EventQueue.isDispatchThread();
        logger.debug("show notification dialog");
        JOptionPane.showMessageDialog(mainFrame, message, "Warning", JOptionPane.WARNING_MESSAGE);
        logger.debug("close notification dialog");
    }

    private Runnable disconnectedInvoker = new Runnable() {
        @Override
        public void run() {
            showNotificationDialog("Device is disconnected");
        }
    };

    private Runnable offlineInvoker = new Runnable() {
        @Override
        public void run() {
            showNotificationDialog("Device goes offline");
        }
    };

    public static void startWatching(MainFrame mainFrame, IDevice device) {
        AdbDeviceManager.addDeviceChangeListener(new DeviceDisconnectedHandler(mainFrame, device));
    }
}
