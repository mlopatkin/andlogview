/*
 * Copyright 2022 Mikhail Lopatkin
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
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;
import name.mlopatkin.andlogview.widgets.TextFieldVerifier;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Predicate;

import javax.inject.Inject;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class ConfigurationDialogView implements ConfigurationDialogPresenter.View {
    private final DialogFactory dialogFactory;
    @Nullable
    private ConfigurationDialogUi dialog;

    @MonotonicNonNull
    private Runnable onCommit;
    @MonotonicNonNull
    private Runnable onDiscard;
    @MonotonicNonNull
    private Predicate<String> adbLocationChecker;

    @Inject
    public ConfigurationDialogView(DialogFactory dialogFactory) {
        this.dialogFactory = dialogFactory;
    }

    @Override
    public void setAdbLocation(String adbLocation) {
        getDialog().adbExecutableText.setText(adbLocation);
    }

    @Override
    public String getAdbLocation() {
        return getDialog().adbExecutableText.getText();
    }

    @Override
    public void setAutoReconnectEnabled(boolean enabled) {
        getDialog().autoReconnectCheckbox.getModel().setSelected(enabled);
    }

    @Override
    public boolean isAutoReconnectEnabled() {
        return getDialog().autoReconnectCheckbox.getModel().isSelected();
    }

    @Override
    public void setCommitAction(Runnable runnable) {
        onCommit = runnable;
    }

    @Override
    public void setDiscardAction(Runnable runnable) {
        onDiscard = runnable;
    }

    @Override
    public void setAdbLocationChecker(Predicate<String> locationChecker) {
        adbLocationChecker = locationChecker;
    }

    @Override
    public void showInvalidAdbLocationError() {
        ErrorDialogsHelper.showError(dialogFactory.getOwner(), "%s is not a valid adb file", getAdbLocation());
    }

    @Override
    public void showRestartAppWarning() {
        JOptionPane.showMessageDialog(dialogFactory.getOwner(),
                "You've changed the path to the ADB executable. Please restart the "
                        + "application to apply changes.",
                "Please restart", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void show() {
        ConfigurationDialogUi dialog = getDialog();
        if (!dialog.isVisible()) {
            dialog.setVisible(true);
        }
    }

    @Override
    public void hide() {
        ConfigurationDialogUi dialog = this.dialog;
        if (dialog == null) {
            return;
        }
        dialog.setVisible(false);
        dialog.dispose();
        this.dialog = null;
    }

    private ConfigurationDialogUi getDialog() {
        ConfigurationDialogUi dialog = this.dialog;
        if (dialog == null) {
            this.dialog = dialog = new ConfigurationDialogUi(dialogFactory.getOwner()) {
                {
                    browseAdbBtn.addActionListener(e -> {
                        JFileChooser fileChooser = new JFileChooser();
                        int result = fileChooser.showOpenDialog(this);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            adbExecutableText.setText(fileChooser.getSelectedFile().getAbsolutePath());
                        }
                    });

                    TextFieldVerifier.verifyWith(adbExecutableText, newAdbLocation -> {
                        if (adbLocationChecker != null) {
                            return adbLocationChecker.test(newAdbLocation);
                        }
                        return true;
                    });
                }

                @Override
                protected void onPositiveResult() {
                    if (onCommit != null) {
                        onCommit.run();
                    }
                }

                @Override
                protected void onNegativeResult() {
                    if (onDiscard != null) {
                        onDiscard.run();
                    }
                }
            };
        }
        return dialog;
    }

}
