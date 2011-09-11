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

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

class SetupAdbDialog extends JDialog implements PropertyChangeListener {

    private JOptionPane optionPane;

    private String yesBtnString = "Yes";
    private String noBtnString = "No";

    private final JCheckBox checkBox = new JCheckBox("Never show this dialog again", false);

    private SetupAdbDialog(Frame aFrame) {
        super(aFrame, true);

        Object[] array = { "The ADB executable cannot be found. Would you like to specify it now?",
                checkBox };

        Object[] options = { yesBtnString, noBtnString };

        optionPane = new JOptionPane(array, JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION,
                null, options, options[0]);

        setContentPane(optionPane);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });
        optionPane.addPropertyChangeListener(this);
        pack();

    }

    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible()
                && (e.getSource() == optionPane)
                && (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY
                        .equals(prop))) {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                return;
            }

            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (yesBtnString.equals(value)) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ConfigurationDialog.showConfigurationDialog((Frame) getOwner());
                    }
                });
            }
            Configuration.adb.showSetupDialog(checkBox.isSelected());
            setVisible(false);
        }
    }

    public static void showDialog(Frame parent) {
        JDialog dialog = new SetupAdbDialog(parent);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        dialog.dispose();
    }
}