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

import java.awt.Component;
import java.awt.Frame;

import javax.swing.JOptionPane;

/**
 * This class contains helper methods to show error dialogs
 * to the user.
 * 
 */
class ErrorDialogsHelper {
    private ErrorDialogsHelper() {
    }

    static void showError(Component owner, String format, Object... vals) {
        String message = String.format(format, vals);
        showError(owner, message);
    }

    static void showError(Component owner, String message) {
        JOptionPane.showMessageDialog(owner, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void showAdbNotFoundError(Frame owner) {
        if (Configuration.adb.showSetupDialog()) {
            SetupAdbDialog.showDialog(owner);
        } else {
            JOptionPane.showMessageDialog(owner, "The ADB executable was not found", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
