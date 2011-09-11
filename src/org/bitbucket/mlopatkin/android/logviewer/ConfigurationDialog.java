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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

public class ConfigurationDialog extends JDialog {

    private final JPanel contentPanel = new JPanel();

    private ConfigurationDialog(Frame owner) {
        super(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle("Configuration");
        setBounds(100, 100, 400, 122);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        JLabel lblAdbExecutableLocation = new JLabel("ADB executable location");

        textAdbExecutable = new JTextField(Configuration.adb.executable());
        lblAdbExecutableLocation.setLabelFor(textAdbExecutable);
        textAdbExecutable.setColumns(10);

        JButton btBrowseAdb = new JButton("...");

        btBrowseAdb.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(ConfigurationDialog.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    textAdbExecutable.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
        gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        gl_contentPanel
                                .createSequentialGroup()
                                .addComponent(lblAdbExecutableLocation)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(textAdbExecutable, GroupLayout.DEFAULT_SIZE, 486,
                                        Short.MAX_VALUE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(btBrowseAdb, GroupLayout.PREFERRED_SIZE, 33,
                                        GroupLayout.PREFERRED_SIZE)));
        gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        gl_contentPanel
                                .createSequentialGroup()
                                .addGroup(
                                        gl_contentPanel
                                                .createParallelGroup(Alignment.LEADING)
                                                .addComponent(lblAdbExecutableLocation)
                                                .addGroup(
                                                        gl_contentPanel
                                                                .createParallelGroup(
                                                                        Alignment.BASELINE)
                                                                .addComponent(textAdbExecutable,
                                                                        GroupLayout.PREFERRED_SIZE,
                                                                        GroupLayout.DEFAULT_SIZE,
                                                                        GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(btBrowseAdb)))
                                .addContainerGap(375, Short.MAX_VALUE)));
        contentPanel.setLayout(gl_contentPanel);
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton(acOk);
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton(acCancel);
                buttonPane.add(cancelButton);
            }
        }
    }

    private Action acOk = new AbstractAction("OK") {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (validateData()) {
                notifyAboutChanges();
                save();
                ConfigurationDialog.this.setVisible(false);
                ConfigurationDialog.this.dispose();
            } else {
                textAdbExecutable.requestFocusInWindow();
            }
        }
    };

    private Action acCancel = new AbstractAction("Cancel") {
        @Override
        public void actionPerformed(ActionEvent e) {
            ConfigurationDialog.this.setVisible(false);
            ConfigurationDialog.this.dispose();
        }
    };
    private JTextField textAdbExecutable;

    public static void showConfigurationDialog(Frame owner) {
        assert EventQueue.isDispatchThread();
        ConfigurationDialog dialog = new ConfigurationDialog(owner);
        dialog.setVisible(true);
    }

    private boolean validateData() {
        String filename = textAdbExecutable.getText();
        if (filename == null) {
            return false; // silently ignore
        }
        File f = new File(filename);
        if (Configuration.adb.DEFAULT_EXECUTABLE.equals(filename) || (f.exists() && f.canExecute())) {
            return true;
        }
        ErrorDialogsHelper.showError(this, "%s is not a valid adb file", filename);
        return false;
    }

    private void notifyAboutChanges() {
        if (!StringUtils.equals(Configuration.adb.executable(), textAdbExecutable.getText())) {
            JOptionPane
                    .showMessageDialog(
                            this,
                            "You've changed the path to the ADB executable. Please restart the application to apply changes.",
                            "Please restart", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void save() {
        Configuration.adb.executable(textAdbExecutable.getText());
    }
}
