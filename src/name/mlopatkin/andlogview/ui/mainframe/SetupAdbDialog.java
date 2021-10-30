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
package name.mlopatkin.andlogview.ui.mainframe;

import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.ui.preferences.ConfigurationDialog;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

class SetupAdbDialog extends JDialog implements PropertyChangeListener {
    private final Provider<ConfigurationDialog.Controller> configurationDialogController;
    private final JOptionPane optionPane;

    private final String yesBtnString = "Yes";
    private final String noBtnString = "No";

    private final JCheckBox checkBox = new JCheckBox("Never show this dialog again", false);

    private SetupAdbDialog(Frame parent, Provider<ConfigurationDialog.Controller> configurationDialogController) {
        super(parent, true);
        this.configurationDialogController = configurationDialogController;

        Object[] array = {"The ADB executable cannot be found. Would you like to specify it now?", checkBox};

        Object[] options = {yesBtnString, noBtnString};

        optionPane =
                new JOptionPane(array, JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, options[0]);

        setContentPane(optionPane);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                optionPane.setValue(JOptionPane.CLOSED_OPTION);
            }
        });
        optionPane.addPropertyChangeListener(this);
        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible() && (e.getSource() == optionPane)
                && (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                return;
            }

            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (yesBtnString.equals(value)) {
                SwingUtilities.invokeLater(() -> configurationDialogController.get().showConfigurationDialog(
                        (JFrame) getOwner()));
            }
            Configuration.adb.showSetupDialog(checkBox.isSelected());
            setVisible(false);
        }
    }

    public static class Controller {
        private final Provider<ConfigurationDialog.Controller> configurationDialogController;

        @Inject
        public Controller(Provider<ConfigurationDialog.Controller> configurationDialogController) {
            this.configurationDialogController = configurationDialogController;
        }

        public void showSetupAdbDialog(JFrame owner) {
            JDialog dialog = new SetupAdbDialog(owner, configurationDialogController);
            dialog.setLocationRelativeTo(owner);
            dialog.setVisible(true);
            dialog.dispose();
        }
    }
}
