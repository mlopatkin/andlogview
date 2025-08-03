/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.preferences;

import static name.mlopatkin.andlogview.widgets.MigConstraints.CC;
import static name.mlopatkin.andlogview.widgets.MigConstraints.LC;

import name.mlopatkin.andlogview.utils.CommonChars;
import name.mlopatkin.andlogview.widgets.UiHelper;

import net.miginfocom.swing.MigLayout;

import java.awt.Frame;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class InstallAdbDialogUi extends JDialog {
    protected final JButton selectDirectory = new JButton(String.valueOf(CommonChars.ELLIPSIS));
    protected final JButton okButton = new JButton("OK");
    protected final JButton cancelButton = new JButton("Cancel");
    protected final JTextArea licenseView = createLicenseView();
    protected final JCheckBox acceptLicense =
            new JCheckBox("I have read and agree with the above terms and conditions");
    protected final JTextField downloadDirectory = new JTextField();

    public InstallAdbDialogUi(Frame owner) {
        super(owner, "Install ADB", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        var content = getContentPane();
        content.setLayout(new MigLayout(LC().fillX().wrapAfter(2)));

        content.add(new JScrollPane(licenseView),
                CC().spanX(2).minHeight("600lp").minWidth("500lp").grow().wrap("unrel push"));

        content.add(acceptLicense, CC().spanX(2).wrap());

        JLabel platformToolsInstallLocationLabel = new JLabel("Install into");
        platformToolsInstallLocationLabel.setLabelFor(downloadDirectory);

        content.add(platformToolsInstallLocationLabel, CC().alignX("label"));
        content.add(downloadDirectory, CC().split().growX().pushX());
        content.add(selectDirectory, CC().wrap());

        content.add(okButton, CC().spanX(2).split().alignX("right"));
        getRootPane().setDefaultButton(okButton);

        content.add(cancelButton);

        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(getSize());

        UiHelper.bindKeyGlobal(this, KeyEvent.VK_ESCAPE, "close", e -> cancelButton.doClick());
    }

    private JTextArea createLicenseView() {
        var licenseContent = new JTextArea();
        licenseContent.setEditable(false);
        licenseContent.setLineWrap(true);
        licenseContent.setWrapStyleWord(true);
        return licenseContent;
    }
}
