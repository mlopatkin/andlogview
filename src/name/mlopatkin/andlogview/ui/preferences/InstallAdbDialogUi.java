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

import com.google.common.base.Strings;

import net.miginfocom.swing.MigLayout;

import java.awt.Frame;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class InstallAdbDialogUi extends JDialog {
    protected final JButton selectDirectory = new JButton(String.valueOf(CommonChars.ELLIPSIS));
    protected final Action okAction = UiHelper.makeAction("OK", this::onPositiveResult);
    protected final Action cancelAction = UiHelper.makeAction("Cancel", this::onNegativeResult);

    public InstallAdbDialogUi(Frame owner) {
        super(owner, "Install ADB", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        var content = getContentPane();
        content.setLayout(new MigLayout(LC().fillX().wrapAfter(2)));

        content.add(new JScrollPane(createLicenseView()),
                CC().spanX(2).minHeight("600lp").minWidth("500lp").grow().wrap("unrel push"));


        content.add(new JCheckBox(" I have read and agree with the above terms and conditions"),
                CC().spanX(2).wrap());

        var platformToolsInstallLocation = new JTextField();
        JLabel platformToolsInstallLocationLabel = new JLabel("Install into");
        platformToolsInstallLocationLabel.setLabelFor(platformToolsInstallLocation);

        content.add(platformToolsInstallLocationLabel, CC().alignX("label"));
        content.add(platformToolsInstallLocation, CC().split().growX().pushX());
        content.add(selectDirectory, CC().wrap());

        JButton okButton = new JButton(okAction);
        content.add(okButton, CC().spanX(2).split().alignX("right"));
        getRootPane().setDefaultButton(okButton);

        content.add(new JButton(cancelAction));

        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(getSize());

        UiHelper.bindKeyGlobal(this, KeyEvent.VK_ESCAPE, "close", cancelAction);
    }

    private JComponent createLicenseView() {
        @SuppressWarnings("InlineMeInliner")
        var licenseContent = new JTextArea(Strings.repeat("""
                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et \
                dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut \
                aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum \
                dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui \
                officia deserunt mollit anim id est laborum.

                """, 10));
        licenseContent.setEditable(false);
        licenseContent.setLineWrap(true);
        licenseContent.setWrapStyleWord(true);
        return licenseContent;
    }

    private void onPositiveResult() {
        dispose();
    }

    private void onNegativeResult() {
        dispose();
    }
}
