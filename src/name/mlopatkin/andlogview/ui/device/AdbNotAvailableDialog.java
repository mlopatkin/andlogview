/*
 * Copyright 2023 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.device;

import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;
import name.mlopatkin.andlogview.ui.preferences.ConfigurationDialogPresenter;

import dagger.Lazy;

import java.awt.event.ItemEvent;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

public class AdbNotAvailableDialog {
    private static final String SETUP_ADB_OPTION = "Set up ADB";
    private static final String IGNORE_OPTION = "Proceed without ADB";

    private final DialogFactory dialogFactory;
    private final AdbConfigurationPref adbConfigurationPref;
    private final Lazy<ConfigurationDialogPresenter> configurationDialogPresenter;

    @Inject
    public AdbNotAvailableDialog(
            DialogFactory dialogFactory,
            AdbConfigurationPref adbConfigurationPref,
            Lazy<ConfigurationDialogPresenter> configurationDialogPresenter
    ) {
        this.dialogFactory = dialogFactory;
        this.adbConfigurationPref = adbConfigurationPref;
        this.configurationDialogPresenter = configurationDialogPresenter;
    }

    public void show(String failureMessage, boolean isAutoStart) {
        var messageComponents = new ArrayList<>();
        messageComponents.add(failureMessage);

        if (isAutoStart) {
            var doNotStartCheckbox = new JCheckBox("Do not show again on startup");
            messageComponents.add(doNotStartCheckbox);

            doNotStartCheckbox.addItemListener(e -> {
                var isSelected = e.getStateChange() == ItemEvent.SELECTED;
                adbConfigurationPref.setShowAdbAutostartFailures(!isSelected);
            });
        }

        int result = JOptionPane.showOptionDialog(
                dialogFactory.getOwner(),
                messageComponents.toArray(),
                "Error",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[] {SETUP_ADB_OPTION, IGNORE_OPTION},
                SETUP_ADB_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            configurationDialogPresenter.get().openDialog();
        }
    }
}
