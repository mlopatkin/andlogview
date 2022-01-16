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

package name.mlopatkin.andlogview.ui;

import name.mlopatkin.andlogview.preferences.LastUsedDirPref;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.Optional;

import javax.inject.Inject;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * This class presents simple standard modal save/load dialogs.
 */
public class FileDialog {

    private final DialogFactory dialogFactory;
    private final LastUsedDirPref lastUsedDir;

    @Inject
    FileDialog(DialogFactory dialogFactory, LastUsedDirPref lastUsedDir) {
        this.dialogFactory = dialogFactory;
        this.lastUsedDir = lastUsedDir;
    }

    /**
     * Opens a dialog to open a file. This method waits until the file is selected or dialog is closed.
     *
     * @return the selected file or empty optional if the dialog was cancelled
     */
    public Optional<File> selectFileToOpen() {
        JFileChooser fileChooser = new JFileChooser(lastUsedDir.get());
        int result = fileChooser.showOpenDialog(dialogFactory.getOwner());
        if (result != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }
        File file = fileChooser.getSelectedFile();
        lastUsedDir.set(file.getAbsoluteFile().getParentFile());
        return Optional.of(file);
    }

    /**
     * Opens a dialog to save a file. This method waits until the file is selected or dialog is closed.
     *
     * @return the selected file or empty optional if the dialog was cancelled
     */
    public Optional<File> selectFileToSave() {
        return selectFileToSave(null);
    }

    /**
     * Opens a dialog to save a file. This method waits until the file is selected or dialog is closed.
     *
     * @param provisionalFileName the suggested filename or {@code null} to suggest nothing.
     * @return the selected file or empty optional if the dialog was cancelled
     */
    public Optional<File> selectFileToSave(@Nullable String provisionalFileName) {
        JFileChooser fileChooser = new JFileChooser(lastUsedDir.get());
        if (provisionalFileName != null) {
            fileChooser.setSelectedFile(new File(lastUsedDir.get(), provisionalFileName));
        }
        int result = fileChooser.showSaveDialog(dialogFactory.getOwner());
        if (result != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }
        File file = fileChooser.getSelectedFile();
        lastUsedDir.set(file.getAbsoluteFile().getParentFile());
        if (file.exists()) {
            result = JOptionPane.showConfirmDialog(
                    dialogFactory.getOwner(), "File " + file + " already exists, overwrite?");
            if (result != JOptionPane.YES_OPTION) {
                return Optional.empty();
            }
        }
        return Optional.of(file);
    }
}
