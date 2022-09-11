/*
 * Copyright 2020 Mikhail Lopatkin
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

import name.mlopatkin.andlogview.AppExecutors;
import name.mlopatkin.andlogview.ErrorDialogsHelper;
import name.mlopatkin.andlogview.device.Device;
import name.mlopatkin.andlogview.device.dump.DeviceDumpFactory;
import name.mlopatkin.andlogview.ui.FileDialog;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;

import com.google.common.io.Files;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JOptionPane;

/**
 * This presenter handles "Dump Device..." menu command.
 */
public class DumpDevicePresenter {
    private static final Logger logger = Logger.getLogger(DumpDevicePresenter.class);

    private final DialogFactory dialogFactory;
    private final DeviceDumpFactory dumpFactory;
    private final Executor uiExecutor;
    private final Executor fileExecutor;
    private final FileDialog fileDialog;
    private final SelectDeviceDialog.Factory selectDeviceDialogFactory;

    @Inject
    DumpDevicePresenter(DialogFactory dialogFactory, DeviceDumpFactory dumpFactory, @Named(AppExecutors.UI_EXECUTOR)
            Executor uiExecutor, @Named(AppExecutors.FILE_EXECUTOR) Executor fileExecutor,
            FileDialog fileDialog, SelectDeviceDialog.Factory selectDeviceDialogFactory) {
        this.dialogFactory = dialogFactory;
        this.dumpFactory = dumpFactory;
        this.uiExecutor = uiExecutor;
        this.fileExecutor = fileExecutor;
        this.fileDialog = fileDialog;
        this.selectDeviceDialogFactory = selectDeviceDialogFactory;
    }

    /**
     * Prompts the user to select device, then to select save location, then does the dumping and shows a message upon
     * completion.
     */
    public void selectDeviceAndDump() {
        // TODO(mlopatkin): Wrap this dialog interaction into View interface. This isn't particularly Clean because
        //   another indirection layer is needed: use case (this class) should call real presenter via an interface and
        //   the presenter should show dialog via view interface. This complexity isn't justified here though.
        selectDeviceDialogFactory.show((dialog, selectedDevice) -> dumpDevice(selectedDevice));
    }

    /**
     * Prompts the user to select save location, then does the dumping and shows a message upon completion.
     *
     * @param selectedDevice the device to dump
     */
    private void dumpDevice(@Nullable Device selectedDevice) {
        if (selectedDevice == null) {
            // User cancelled dump.
            return;
        }
        String provisionalFileName = dumpFactory.getProvisionalOutputFileName(selectedDevice);
        fileDialog.selectFileToSave(provisionalFileName).ifPresent(file -> onOutputFileSelected(selectedDevice, file));
    }

    private void onOutputFileSelected(Device selectedDevice, File outputFile) {
        dumpFactory.collectAsync(selectedDevice, Files.asByteSink(outputFile), fileExecutor).handleAsync(
                (unused, throwable) -> {
                    if (throwable == null) {
                        onDeviceDumpReady(outputFile);
                    } else {
                        onDeviceDumpFailed(outputFile, throwable);
                    }
                    return null;
                },
                uiExecutor
        );
    }

    private void onDeviceDumpReady(File outputFile) {
        // TODO(mlopatkin): Show status bar message instead
        JOptionPane.showMessageDialog(dialogFactory.getOwner(),
                "Device dump is ready at:\n" + outputFile.getAbsolutePath());
    }

    private void onDeviceDumpFailed(File outputFile, Throwable error) {
        logger.error("Failed to prepare device dump", error);
        if (outputFile.exists() && !outputFile.delete()) {
            logger.error("Failed to delete partially written file");
        }

        ErrorDialogsHelper.showError(dialogFactory.getOwner(), "Failed to prepare device dump");
    }
}
