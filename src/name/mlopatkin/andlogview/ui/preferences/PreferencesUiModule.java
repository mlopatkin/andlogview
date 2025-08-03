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

import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;
import name.mlopatkin.andlogview.widgets.DialogResult;
import name.mlopatkin.andlogview.widgets.dialogs.ProgressDialog;

import dagger.Binds;
import dagger.Module;

import java.io.File;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.swing.JFileChooser;

@Module
public abstract class PreferencesUiModule {
    @Binds
    abstract ConfigurationDialogPresenter.View getDialogView(ConfigurationDialogView instance);

    @Binds
    abstract DownloadAdbPresenter.SdkInitView sdkInitView(SdkInitViewImpl sdkInitView);

    @Binds
    abstract DownloadAdbPresenter.InstallView installView(InstallViewImpl installView);

    @Binds
    abstract DownloadAdbPresenter.SdkDownloadView downloadView(SdkDownloadViewImpl sdkDownloadView);

    static class SdkInitViewImpl extends ProgressDialog implements DownloadAdbPresenter.SdkInitView {
        @Inject
        SdkInitViewImpl(DialogFactory dialogFactory) {
            super(dialogFactory.getOwner(), "Loading SDK components", "Loading SDK components…", "Cancel");
        }

        @Override
        public void show(Runnable cancellationAction) {
            super.show(cancellationAction);
        }

        @Override
        public void hide() {
            super.hide();
        }
    }

    static class SdkDownloadViewImpl extends ProgressDialog implements DownloadAdbPresenter.SdkDownloadView {
        @Inject
        public SdkDownloadViewImpl(DialogFactory dialogFactory) {
            super(dialogFactory.getOwner(), "Installing SDK package", "Installing SDK package…", "Cancel");
        }

        @Override
        public void show(Runnable cancellationAction) {
            super.show(cancellationAction);
        }

        @Override
        public void hide() {
            super.hide();
        }
    }

    static class InstallViewImpl implements DownloadAdbPresenter.InstallView {
        private final InstallAdbDialogUi dialog;
        private final DialogResult.DialogSubject<File> subject = new DialogResult.DialogSubject<>();

        @Inject
        InstallViewImpl(DialogFactory dialogFactory) {
            dialog = new InstallAdbDialogUi(dialogFactory.getOwner());
            dialog.okButton.addActionListener(e -> subject.commit(new File(dialog.downloadDirectory.getText())));
            dialog.cancelButton.addActionListener(e -> subject.discard());
        }

        @Override
        public void setAcceptAction(Consumer<Boolean> acceptAction) {
            dialog.acceptLicense.addItemListener(l -> acceptAction.accept(dialog.acceptLicense.isSelected()));
        }

        @Override
        public void setLicenseText(String licenseText) {
            dialog.licenseView.setText(licenseText);
            dialog.licenseView.setCaretPosition(0);
        }

        @Override
        public void setInstallLocation(File path) {
            dialog.downloadDirectory.setText(path.getAbsolutePath());
        }

        @Override
        public void show(Consumer<? super File> commitAction, Runnable cancelAction) {
            subject.asResult().onCommit(commitAction).onDiscard(cancelAction);
            dialog.setVisible(true);
        }

        @Override
        public void hide() {
            dialog.dispose();
        }

        @Override
        public void setDownloadAllowed(boolean allowed) {
            dialog.okButton.setEnabled(allowed);
        }

        @Override
        public void setInstallLocationSelectionAction(Runnable action) {
            dialog.selectDirectory.addActionListener(e -> action.run());
        }

        @Override
        public void showInstallDirSelector(Consumer<? super File> commitAction, Runnable cancelAction) {
            var chooser = new JFileChooser(dialog.downloadDirectory.getText());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            var result = chooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                commitAction.accept(chooser.getSelectedFile());
            } else {
                cancelAction.run();
            }
        }
    }
}
