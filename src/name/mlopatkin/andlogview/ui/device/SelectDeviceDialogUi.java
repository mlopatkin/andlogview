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

import static name.mlopatkin.andlogview.widgets.MigConstraints.CC;
import static name.mlopatkin.andlogview.widgets.MigConstraints.LC;

import name.mlopatkin.andlogview.device.ProvisionalDevice;

import net.miginfocom.swing.MigLayout;

import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 * UI base for SelectDeviceDialog.
 */
class SelectDeviceDialogUi extends JDialog {
    protected final JList<ProvisionalDevice> deviceList;
    protected final JButton okButton;
    protected final JButton cancelButton;

    public SelectDeviceDialogUi(Frame owner) {
        super(owner, true);
        setTitle("Select device");

        var content = getContentPane();
        content.setLayout(new MigLayout(
                LC().wrapAfter(1).fill()
        ));

        deviceList = new JList<>();
        content.add(new JScrollPane(deviceList), CC().minWidth("300lp").minHeight("200lp").grow().push().wrap());

        okButton = new JButton("OK");

        content.add(okButton, CC().split().alignX("right"));
        getRootPane().setDefaultButton(okButton);

        cancelButton = new JButton("Cancel");
        content.add(cancelButton);

        pack();
        setMinimumSize(getPreferredSize());
        setLocationRelativeTo(owner);
    }
}
