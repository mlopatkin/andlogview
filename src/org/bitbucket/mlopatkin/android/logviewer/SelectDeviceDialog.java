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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.android.ddmlib.IDevice;

public class SelectDeviceDialog extends JDialog {

    private JList deviceList;

    private DeviceListModel devices = new DeviceListModel();

    public SelectDeviceDialog() {
        setTitle("Select device");
        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new FlowLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        {
            deviceList = new JList();
            deviceList.setModel(devices);
            contentPanel.add(deviceList);
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

    }

    private void onNegativeResult() {

    }

    public IDevice getSelectedDevice() {
        int selected = deviceList.getSelectedIndex();
        if (selected >= 0) {
            return devices.getDevice(selected);
        } else {
            return null;
        }
    }

    private static class DeviceListModel extends AbstractListModel {

        @Override
        public int getSize() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Object getElementAt(int index) {
            // TODO Auto-generated method stub
            return null;
        }

        public IDevice getDevice(int index) {
            return null;
        }
    }

}
