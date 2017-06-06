/*
 * Copyright 2017 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.device;

import com.android.ddmlib.IDevice;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.Timer;

/**
 * Dialog that allows the user to select a connected device and retrieve live logs from it. This dialog is modal.
 */
public class SelectDeviceDialog extends SelectDeviceDialogUi {
    private static final int UPDATE_DELAY = 500;
    private final ResultReceiver receiver;
    private final DeviceListModel deviceListModel;
    private final Timer updater = new Timer(UPDATE_DELAY, e -> {
        if (isVisible()) {
            deviceList.repaint();
        }
    });

    private boolean resultDelivered = false;

    private SelectDeviceDialog(JFrame owner, ResultReceiver receiver) {
        super(owner);
        this.receiver = receiver;
        deviceListModel = DeviceListModel.create();

        deviceList.setModel(deviceListModel);
        deviceList.setCellRenderer(new DeviceListCellRenderer());

        int firstOnlineIndex = deviceListModel.getFirstOnlineDeviceIndex();
        if (firstOnlineIndex >= 0) {
            deviceList.setSelectedIndex(firstOnlineIndex);
        }

        okButton.addActionListener(e -> onPositiveResult());
        cancelButton.addActionListener(e -> onNegativeResult());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                onDialogClosed();
            }
        });

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        updater.start();
    }

    /**
     * Shows a modal device selection dialog. The adb bridge should be initialized before calling this method.
     *
     * @param owner the parent frame to show dialog in
     * @param resultReceiver the callback to get a selected device
     */
    public static void showDialog(JFrame owner, ResultReceiver resultReceiver) {
        SelectDeviceDialog dialog = new SelectDeviceDialog(owner, resultReceiver);
        dialog.setVisible(true);
    }

    private void onPositiveResult() {
        deliverResult(deviceList.getSelectedValue());
        dispose();
    }

    private void onNegativeResult() {
        deliverResult(null);
        dispose();
    }

    private void deliverResult(IDevice selectedDevice) {
        assert !resultDelivered;
        resultDelivered = true;
        receiver.onDialogResult(this, selectedDevice);
    }

    private void onDialogClosed() {
        if (!resultDelivered) {
            deliverResult(null);
        }
        updater.stop();
        deviceListModel.unsubscribe();
    }
}
