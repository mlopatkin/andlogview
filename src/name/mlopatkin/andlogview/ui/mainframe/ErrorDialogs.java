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

import name.mlopatkin.andlogview.ErrorDialogsHelper;
import name.mlopatkin.andlogview.config.Configuration;

import javax.inject.Inject;
import javax.swing.JFrame;

/**
 * Factory class to show error dialogs in the Main frame.
 */
public class ErrorDialogs {
    private final DialogFactory dialogFactory;
    private final SetupAdbDialog.Controller setupAdbDialogController;

    @Inject
    ErrorDialogs(DialogFactory dialogFactory, SetupAdbDialog.Controller setupAdbDialogController) {
        this.dialogFactory = dialogFactory;
        this.setupAdbDialogController = setupAdbDialogController;
    }

    /**
     * Shows the "The ADB executable was not found" dialog.
     */
    public void showAdbNotFoundError() {
        JFrame dialogOwner = dialogFactory.getOwner();
        if (Configuration.adb.showSetupDialog()) {
            setupAdbDialogController.showSetupAdbDialog(dialogOwner);
        } else {
            ErrorDialogsHelper.showError(dialogOwner, "The ADB executable was not found");
        }
    }

}
