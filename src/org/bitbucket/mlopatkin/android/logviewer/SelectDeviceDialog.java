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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDeviceManager;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper.DoubleClickListener;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;

public class SelectDeviceDialog extends JDialog {

    public interface DialogResultReceiver {
        void onDialogResult(SelectDeviceDialog dialog, IDevice selectedDevice);
    }

    public static void showSelectDeviceDialog(DialogResultReceiver receiver) {
        if (dialog == null) {
            dialog = new SelectDeviceDialog();
        }
        dialog.receiver = receiver;
        dialog.setVisible(true);
    }

    private static final Logger logger = Logger.getLogger(SelectDeviceDialog.class);

    private static SelectDeviceDialog dialog;

    private JList deviceList;
    private DialogResultReceiver receiver;

    private DeviceListModel devices = new DeviceListModel();

    private SelectDeviceDialog() {
        setModal(true);
        setTitle("Select device");
        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            deviceList = new JList();
            deviceList.setModel(devices);
            contentPanel.add(deviceList);

            UiHelper.addDoubleClickListener(deviceList, new DoubleClickListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onPositiveResult();
                }
            });
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onPositiveResult();
                    }
                });

                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onNegativeResult();
                    }
                });
                buttonPane.add(cancelButton);
            }
        }
    }

    private void onPositiveResult() {
        assert receiver != null;
        receiver.onDialogResult(this, getSelectedDevice());
        setVisible(false);
    }

    private void onNegativeResult() {
        assert receiver != null;
        receiver.onDialogResult(this, null);
        setVisible(false);
    }

    private IDevice getSelectedDevice() {
        int selected = deviceList.getSelectedIndex();
        if (selected >= 0) {
            return devices.getDevice(selected);
        } else {
            return null;
        }
    }

    private static class DeviceListModel extends AbstractListModel implements IDeviceChangeListener {

        private List<IDevice> devices;
        private static final String PRODUCT_NAME_PROPERTY = "ro.build.product";

        public DeviceListModel() {
            devices = new ArrayList<IDevice>(AdbDeviceManager.getAvailableDevices());
            AdbDeviceManager.addDeviceChangeListener(this);
        }

        @Override
        public int getSize() {
            return devices.size();
        }

        @Override
        public Object getElementAt(int index) {
            IDevice device = getDevice(index);
            StringBuilder deviceName = new StringBuilder(device.getSerialNumber());
            if (device.isEmulator()) {
                deviceName.append(' ').append(device.getAvdName());
            } else {
                String productName = device.getProperty(PRODUCT_NAME_PROPERTY);
                if (productName != null) {
                    deviceName.append(' ').append(productName);
                }
            }
            return deviceName.toString();
        }

        public IDevice getDevice(int index) {
            return devices.get(index);
        }

        private void addDevice(IDevice device) {
            logger.debug("device added " + device);
            devices.add(device);
            fireIntervalAdded(this, devices.size() - 1, devices.size() - 1);
        }

        private void removeDevice(IDevice device) {
            logger.debug("device removed " + device);
            int index = devices.indexOf(device);
            if (index >= 0) {
                devices.remove(index);
                fireIntervalRemoved(this, index, index);
            }
        }

        @Override
        public void deviceConnected(final IDevice device) {
            logger.debug("Device connected: " + device);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (device.isOnline()) {
                        addDevice(device);
                    }
                }
            });
        }

        @Override
        public void deviceDisconnected(final IDevice device) {
            logger.debug("Device disconnected: " + device);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    removeDevice(device);
                }
            });
        }

        private void updateState(IDevice device, int changeMask) {
            if ((changeMask & IDevice.CHANGE_STATE) != 0) {
                if (device.isOnline()) {
                    addDevice(device);
                } else {
                    removeDevice(device);
                }
            }
            if ((changeMask & IDevice.CHANGE_BUILD_INFO) != 0) {
                fireContentsChanged(DeviceListModel.this, 0, devices.size() - 1);
            }
        }

        @Override
        public void deviceChanged(final IDevice device, final int changeMask) {
            logger.debug("Device changed: " + device + " changeMask="
                    + Integer.toHexString(changeMask));
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateState(device, changeMask);
                }
            });
        }

    }

}
