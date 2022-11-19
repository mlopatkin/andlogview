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
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;

import java.awt.EventQueue;

import javax.inject.Inject;
import javax.swing.JOptionPane;

/**
 * This class is responsible for showing notification dialog when device is disconnected.
 */
public class DeviceDisconnectedHandler implements AdbDataSource.StateObserver {
    // TODO(mlopatkin) Replace MainFrame with something more specific. DialogFactory covers one use case but the call
    //  to waitForDevice is still problematic
    private final MainFrame mainFrame;
    private final DialogFactory dialogFactory;
    private final AdbConfigurationPref adbConfigurationPref;

    @Inject
    DeviceDisconnectedHandler(MainFrame mainFrame, DialogFactory dialogFactory,
            AdbConfigurationPref adbConfigurationPref) {
        this.mainFrame = mainFrame;
        this.dialogFactory = dialogFactory;
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
        JOptionPane.showMessageDialog(dialogFactory.getOwner(), message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Starts tracking the invalidation of the given data source. When the data source becomes invalid a notification is
     * shown or the reconnection attempt is made.
     *
     * @param dataSource the data source to track
     */
    public void startWatching(AdbDataSource dataSource) {
        dataSource.asStateObservable().addObserver(this);
    }
}
