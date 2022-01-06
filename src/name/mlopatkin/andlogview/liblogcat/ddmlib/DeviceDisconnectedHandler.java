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
package name.mlopatkin.andlogview.liblogcat.ddmlib;

import name.mlopatkin.andlogview.MainFrame;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;

import java.awt.EventQueue;

import javax.swing.JOptionPane;

/**
 * This class is responsible for showing notification dialog when device is disconnected.
 */
public class DeviceDisconnectedHandler implements AdbDataSource.StateObserver {
    private final MainFrame mainFrame;
    private final AdbConfigurationPref adbConfigurationPref;

    private DeviceDisconnectedHandler(MainFrame mainFrame, AdbConfigurationPref adbConfigurationPref) {
        this.mainFrame = mainFrame;
        this.adbConfigurationPref = adbConfigurationPref;
    }

    @Override
    public void onDataSourceInvalidated(AdbDataSource.InvalidationReason reason) {
        switch (reason) {
            case DISCONNECT:
                onDeviceDisconnected("Device is disconnected");
                break;
            case OFFLINE:
                onDeviceDisconnected("Device goes offline");
                break;
            default:
                throw new AssertionError("Unexpected reason " + reason);
        }
    }

    private void onDeviceDisconnected(String message) {
        if (adbConfigurationPref.isAutoReconnectEnabled()) {
            mainFrame.waitForDevice();
        } else {
            EventQueue.invokeLater(() -> showNotificationDialog(message));
        }
    }

    private void showNotificationDialog(String message) {
        assert EventQueue.isDispatchThread();
        JOptionPane.showMessageDialog(mainFrame, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static void startWatching(AdbDataSource dataSource, MainFrame mainFrame,
            AdbConfigurationPref adbConfigurationPref) {
        dataSource.asStateObservable().addObserver(new DeviceDisconnectedHandler(mainFrame, adbConfigurationPref));
    }
}
