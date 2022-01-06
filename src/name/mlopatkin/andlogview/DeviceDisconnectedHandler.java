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
package name.mlopatkin.andlogview;

import name.mlopatkin.andlogview.device.AdbDevice;
import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbDeviceManager;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;

import com.android.ddmlib.IDevice;

import org.apache.log4j.Logger;

import java.awt.EventQueue;
import java.util.Objects;

import javax.swing.JOptionPane;

/**
 * This class is responsible for showing notification dialog when device is
 * disconnected.
 */
public class DeviceDisconnectedHandler extends AdbDeviceManager.AbstractDeviceListener {
    private static final Logger logger = Logger.getLogger(DeviceDisconnectedHandler.class);
    private final MainFrame mainFrame;
    private final AdbConfigurationPref adbConfigurationPref;
    private final AdbDeviceManager deviceManager;
    private final IDevice device;

    private DeviceDisconnectedHandler(MainFrame mainFrame, AdbConfigurationPref adbConfigurationPref,
            AdbDeviceManager deviceManager, IDevice device) {
        this.mainFrame = mainFrame;
        this.adbConfigurationPref = adbConfigurationPref;
        this.deviceManager = deviceManager;
        this.device = device;
    }

    private boolean isTrackedDevice(IDevice device) {
        return Objects.equals(device.getSerialNumber(), this.device.getSerialNumber());
    }
    @Override
    public void deviceDisconnected(IDevice device) {
        if (isTrackedDevice(device)) {
            onDeviceDisconnected(disconnectedInvoker);
            // one-shot
            deviceManager.removeDeviceChangeListener(this);
        }
    }

    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        if (isTrackedDevice(device) && (changeMask & IDevice.CHANGE_STATE) != 0) {
            if (!device.isOnline()) {
                onDeviceDisconnected(offlineInvoker);
                deviceManager.removeDeviceChangeListener(this);
            }
        }
    }

    private void onDeviceDisconnected(Runnable notificationInvoker) {
        logger.debug("showNotification");
        if (adbConfigurationPref.isAutoReconnectEnabled()) {
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

    private Runnable disconnectedInvoker = () -> showNotificationDialog("Device is disconnected");

    private Runnable offlineInvoker = () -> showNotificationDialog("Device goes offline");

    public static void startWatching(MainFrame mainFrame, AdbConfigurationPref adbConfigurationPref,
            AdbDeviceManager deviceManager, AdbDevice device) {
        deviceManager.addDeviceChangeListener(
                new DeviceDisconnectedHandler(mainFrame, adbConfigurationPref, deviceManager, device.getIDevice()));
    }
}
