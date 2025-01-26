/*
 * Copyright 2021 Mikhail Lopatkin
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

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

abstract class ConfigurationDialogUi extends JDialog {
    protected final JTextField adbExecutableText = new JTextField(25);
    protected final JButton browseAdbBtn = new JButton(String.valueOf(CommonChars.ELLIPSIS));
    protected final JCheckBox autoReconnectCheckbox = new JCheckBox("Reconnect to device automatically");
    protected final JButton installAdbBtn = new JButton(UiHelper.makeAction("Install ADB...", this::onInstallAdb));
    protected final Action okAction = UiHelper.makeAction("OK", this::onPositiveResult);
    protected final Action cancelAction = UiHelper.makeAction("Cancel", this::onNegativeResult);

    public ConfigurationDialogUi(Frame owner) {
        super(owner, true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("Configuration");

        var content = getContentPane();
        content.setLayout(new MigLayout(
                LC().fillX().wrapAfter(2)
        ));

        JLabel adbExecutableTextLabel = new JLabel("ADB executable location");
        adbExecutableTextLabel.setLabelFor(adbExecutableText);

        content.add(adbExecutableTextLabel, CC().alignX("label"));
        content.add(adbExecutableText, CC().split().growX().pushX());
        content.add(browseAdbBtn, CC().wrap());

        content.add(installAdbBtn, CC().wrap().hideMode(3));

        content.add(autoReconnectCheckbox, CC().spanX(2).wrap("0 push"));

        JButton okButton = new JButton(okAction);
        content.add(okButton, CC().spanX(2).split().alignX("right"));
        getRootPane().setDefaultButton(okButton);

        content.add(new JButton(cancelAction));

        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(getSize());

        UiHelper.bindKeyGlobal(this, KeyEvent.VK_ESCAPE, "close", cancelAction);
    }

    protected abstract void onPositiveResult();

    protected abstract void onNegativeResult();

    protected abstract void onInstallAdb();
}
