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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

abstract class ConfigurationDialogUi extends JDialog {
    protected final JTextField adbExecutableText = new JTextField(25);
    protected final JButton browseAdbBtn = new JButton(String.valueOf(CommonChars.ELLIPSIS));
    protected final JCheckBox autoReconnectCheckbox = new JCheckBox("Reconnect to device automatically");
    protected final Action okAction = UiHelper.makeAction("OK", this::onPositiveResult);
    protected final Action cancelAction = UiHelper.makeAction("Cancel", this::onNegativeResult);

    public ConfigurationDialogUi(Frame owner) {
        super(owner);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Configuration");
        getContentPane().setLayout(new BorderLayout());


        var content = new JPanel(new MigLayout(
                LC().fillX().wrapAfter(2).insets("8lp")
        ));

        JLabel adbExecutableTextLabel = new JLabel("ADB executable location");
        adbExecutableTextLabel.setLabelFor(adbExecutableText);

        content.add(adbExecutableTextLabel, CC().alignX("label"));
        content.add(adbExecutableText, CC().split().growX().pushX());
        content.add(browseAdbBtn, CC().wrap());

        content.add(autoReconnectCheckbox, CC().spanX(2));

        getContentPane().add(content, BorderLayout.CENTER);

        var buttonPanel = new JPanel(new MigLayout(LC().alignX("right").insets("8lp")));
        JButton okButton = new JButton(okAction);
        buttonPanel.add(okButton);
        JButton cancelButton = new JButton(cancelAction);
        buttonPanel.add(cancelButton);

        getRootPane().setDefaultButton(okButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(getSize());

        UiHelper.bindKeyGlobal(this, KeyEvent.VK_ESCAPE, "close", cancelAction);
    }

    protected abstract void onPositiveResult();

    protected abstract void onNegativeResult();
}
