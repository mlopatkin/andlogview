/*
 * Copyright 2014 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.border.EmptyBorder;

/**
 * Base Filter Dialog UI class. Edit with WindowBuilder.
 */
abstract class BaseFilterDialogUi extends JDialog {
    protected final JPanel contentPanel = new JPanel();

    protected final JTextField tagTextField;
    protected final JTextField messageTextField;
    protected final JTextField pidTextField;

    protected final JComboBox<LogRecord.Priority> logLevelList;

    protected final FilteringModesPanel modesPanel;
    protected final JComboBox<ColorsComboBoxModel.Item> colorsList;

    protected final JButton okButton;
    protected final JButton cancelButton;

    public BaseFilterDialogUi(Frame owner) {
        super(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        {
            tagTextField = new JTextField();
            tagTextField.setColumns(10);
        }

        JLabel lblNewLabel = new JLabel("Tags to filter");

        JLabel lblMessageTextTo = new JLabel("Message text to filter");

        messageTextField = new JTextField();
        messageTextField.setColumns(10);

        JLabel lblPidsToFilter = new JLabel("PIDs or app names to filter");

        pidTextField = new JTextField();
        pidTextField.setColumns(10);

        JLabel lblLogLevel = new JLabel("Log level");

        logLevelList = new JComboBox<>(new PriorityComboBoxModel());

        JPanel modesWithDataPanel = new JPanel();

        colorsList = new JComboBox<>(new ColorsComboBoxModel());
        colorsList.setSelectedIndex(0);

        GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
        gl_contentPanel.setHorizontalGroup(
                gl_contentPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(gl_contentPanel.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(tagTextField, GroupLayout.DEFAULT_SIZE, 477,
                                                Short.MAX_VALUE)
                                        .addComponent(lblNewLabel)
                                        .addComponent(lblMessageTextTo)
                                        .addComponent(messageTextField, GroupLayout.DEFAULT_SIZE,
                                                477, Short.MAX_VALUE)
                                        .addComponent(lblPidsToFilter)
                                        .addComponent(pidTextField, GroupLayout.DEFAULT_SIZE, 477,
                                                Short.MAX_VALUE)
                                        .addComponent(lblLogLevel)
                                        .addComponent(logLevelList, 0, 477, Short.MAX_VALUE)
                                        .addGroup(gl_contentPanel.createSequentialGroup()
                                                .addComponent(modesWithDataPanel,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addGap(18)
                                                .addComponent(colorsList,
                                                        GroupLayout.PREFERRED_SIZE, 132,
                                                        GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));
        gl_contentPanel.setVerticalGroup(
                gl_contentPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                gl_contentPanel.createSequentialGroup()
                                        .addComponent(lblNewLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tagTextField, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblMessageTextTo)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(messageTextField, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(lblPidsToFilter)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(pidTextField, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(lblLogLevel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(logLevelList, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                                        .addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(modesWithDataPanel,
                                                        GroupLayout.Alignment.TRAILING,
                                                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addGroup(GroupLayout.Alignment.TRAILING,
                                                        gl_contentPanel.createSequentialGroup()
                                                                .addComponent(colorsList,
                                                                        GroupLayout.PREFERRED_SIZE,
                                                                        GroupLayout.DEFAULT_SIZE,
                                                                        GroupLayout.PREFERRED_SIZE)
                                                                .addGap(31)))));

        modesPanel = new FilteringModesPanel();
        modesWithDataPanel.add(modesPanel);
        contentPanel.setLayout(gl_contentPanel);
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);

            okButton = new JButton("OK");
            buttonPane.add(okButton);
            getRootPane().setDefaultButton(okButton);

            cancelButton = new JButton("Cancel");
            buttonPane.add(cancelButton);
        }
        pack();
        setLocationRelativeTo(getParent());
    }
}
