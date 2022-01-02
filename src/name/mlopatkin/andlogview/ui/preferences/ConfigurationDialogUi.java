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

import name.mlopatkin.andlogview.utils.CommonChars;
import name.mlopatkin.andlogview.widgets.UiHelper;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.border.EmptyBorder;

abstract class ConfigurationDialogUi extends JDialog {
    protected final JTextField adbExecutableText = new JTextField(10);
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

        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        JLabel adbExecutableTextLabel = new JLabel("ADB executable location");
        adbExecutableTextLabel.setLabelFor(adbExecutableText);

        GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
        contentPanelLayout.setHorizontalGroup(
                contentPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                contentPanelLayout.createSequentialGroup()
                                        .addComponent(adbExecutableTextLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(adbExecutableText, GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(browseAdbBtn, GroupLayout.PREFERRED_SIZE, 33,
                                                GroupLayout.PREFERRED_SIZE))
                        .addGroup(contentPanelLayout.createSequentialGroup()
                                .addComponent(autoReconnectCheckbox)
                                .addContainerGap()));
        contentPanelLayout.setVerticalGroup(
                contentPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(contentPanelLayout.createSequentialGroup()
                                .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(adbExecutableTextLabel)
                                        .addGroup(contentPanelLayout
                                                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(adbExecutableText,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addComponent(browseAdbBtn)))
                                .addGap(18)
                                .addComponent(autoReconnectCheckbox)
                                .addContainerGap(57, Short.MAX_VALUE)));
        contentPanel.setLayout(contentPanelLayout);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        JButton okButton = new JButton(okAction);
        buttonPanel.add(okButton);
        getRootPane().setDefaultButton(okButton);
        JButton cancelButton = new JButton(cancelAction);
        buttonPanel.add(cancelButton);

        pack();
        setLocationRelativeTo(owner);

        UiHelper.bindKeyGlobal(this, KeyEvent.VK_ESCAPE, "close", cancelAction);
    }

    protected abstract void onPositiveResult();

    protected abstract void onNegativeResult();
}
