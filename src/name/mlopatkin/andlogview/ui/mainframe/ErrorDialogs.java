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

package name.mlopatkin.andlogview.ui.mainframe;

import name.mlopatkin.andlogview.ui.device.AdbNotAvailableDialog;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JOptionPane;

/**
 * Factory class to show error dialogs in the Main frame.
 */
public class ErrorDialogs {
    private final DialogFactory dialogFactory;
    private final Provider<AdbNotAvailableDialog> adbDialogFactory;

    @Inject
    ErrorDialogs(DialogFactory dialogFactory, Provider<AdbNotAvailableDialog> adbDialogFactory) {
        this.dialogFactory = dialogFactory;
        this.adbDialogFactory = adbDialogFactory;
    }

    /**
     * Shows the "The ADB executable was not found" dialog.
     *
     * @param failureMessage the description of the ADB failure
     * @param isAutoStart if the start attempt was automatic
     */
    public void showAdbFailedToStartError(String failureMessage, boolean isAutoStart) {
        adbDialogFactory.get().show(failureMessage, isAutoStart);
    }

    /**
     * Shows a modal warning dialog when the observed device become disconnected
     *
     * @param disconnectReason the disconnect reason
     */
    public void showDeviceDisconnectedWarning(String disconnectReason) {
        JOptionPane.showMessageDialog(dialogFactory.getOwner(), disconnectReason, "Warning",
                JOptionPane.WARNING_MESSAGE);
    }
}
