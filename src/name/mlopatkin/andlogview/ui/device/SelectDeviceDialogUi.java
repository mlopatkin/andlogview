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

package name.mlopatkin.andlogview.ui.device;

import com.android.ddmlib.IDevice;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * UI base for SelectDeviceDialog. Should be edited with WindowBuilder.
 */
class SelectDeviceDialogUi extends JDialog {
    protected final JList<IDevice> deviceList;
    protected final JButton okButton;
    protected final JButton cancelButton;

    public SelectDeviceDialogUi(Frame owner) {
        super(owner, true);
        setPreferredSize(new Dimension(450, 300));
        setTitle("Select device");
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        deviceList = new JList<>();
        contentPanel.add(deviceList);
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        okButton = new JButton("OK");

        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);

        cancelButton = new JButton("Cancel");
        buttonPane.add(cancelButton);
        pack();
        setLocationRelativeTo(owner);
    }
}
