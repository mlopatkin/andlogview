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
package name.mlopatkin.andlogview.ui.preferences;

import name.mlopatkin.andlogview.ErrorDialogsHelper;
import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.widgets.UiHelper;

import com.google.common.base.Objects;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.inject.Inject;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ConfigurationDialog extends ConfigurationDialogUi {
    private final AdbConfigurationPref adbConfigurationPref;

    private ConfigurationDialog(Frame owner, AdbConfigurationPref adbConfigurationPref) {
        super(owner);
        this.adbConfigurationPref = adbConfigurationPref;

        adbExecutableText.setText(adbConfigurationPref.getAdbLocation());
        browseAdbBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(ConfigurationDialog.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                adbExecutableText.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        autoReconnectCheckbox.getModel().setSelected(Configuration.adb.isAutoReconnectEnabled());

        UiHelper.bindKeyGlobal(this, KeyEvent.VK_ESCAPE, "close", cancelAction);
    }

    @Override
    protected void onPositiveResult() {
        if (validateData()) {
            notifyAboutChanges();
            save();
            ConfigurationDialog.this.setVisible(false);
            ConfigurationDialog.this.dispose();
        } else {
            adbExecutableText.requestFocusInWindow();
        }
    }

    @Override
    protected void onNegativeResult() {
        ConfigurationDialog.this.setVisible(false);
        ConfigurationDialog.this.dispose();
    }

    private boolean validateData() {
        String filename = adbExecutableText.getText();
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
        if (!Objects.equal(adbConfigurationPref.getAdbLocation(), adbExecutableText.getText())) {
            JOptionPane.showMessageDialog(this,
                    "You've changed the path to the ADB executable. Please restart the "
                            + "application to apply changes.",
                    "Please restart", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void save() {
        adbConfigurationPref.setAdbLocation(adbExecutableText.getText());
        Configuration.adb.setAutoReconnectEnabled(autoReconnectCheckbox.getModel().isSelected());
    }

    /**
     * Transition class to migrate Configuration dialog to the Dependency Injection
     */
    public static class Controller {

        private final AdbConfigurationPref adbConfigurationPref;

        @Inject
        public Controller(AdbConfigurationPref adbConfigurationPref) {
            this.adbConfigurationPref = adbConfigurationPref;
        }

        public void showConfigurationDialog(JFrame owner) {
            assert EventQueue.isDispatchThread();
            ConfigurationDialog dialog = new ConfigurationDialog(owner, adbConfigurationPref);
            dialog.setVisible(true);
        }
    }
}
