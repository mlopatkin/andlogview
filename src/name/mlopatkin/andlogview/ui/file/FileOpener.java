/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.file;

import name.mlopatkin.andlogview.ErrorDialogsHelper;
import name.mlopatkin.andlogview.liblogcat.file.FileDataSourceFactory;
import name.mlopatkin.andlogview.liblogcat.file.UnrecognizedFormatException;
import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.preferences.LastUsedDirPref;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

/**
 * Handles opening a files as a {@link DataSource}. Shows error or warning dialogs if the file cannot be opened
 * properly.
 */
public class FileOpener {
    private static final Logger logger = Logger.getLogger(FileOpener.class);

    private final DialogFactory dialogFactory;
    private final LastUsedDirPref lastUsedDirPref;

    @Inject
    FileOpener(DialogFactory dialogFactory, LastUsedDirPref lastUsedDirPref) {
        this.dialogFactory = dialogFactory;
        this.lastUsedDirPref = lastUsedDirPref;
    }

    /**
     * Tries to open a file as a {@link DataSource}. Handles the whole UI flow, including showing the error dialogs if
     * things go wrong. Does not update the source in the main frame, though.
     * <p>
     * This method must be called on UI thread.
     *
     * @param file the file to open
     * @return the completion stage to be notified about successful file opening
     */
    public CompletionStage<DataSource> openFileAsDataSource(File file) {
        try {
            var importResult = FileDataSourceFactory.createDataSource(file);
            var parentFile = file.getAbsoluteFile().getParentFile();
            if (parentFile != null) {
                // The null pathname is unlikely, as the file can be opened. We don't want to reset the preference if
                // this is the case though.
                lastUsedDirPref.set(parentFile);
            }
            return CompletableFuture.completedFuture(importResult.getDataSource());
        } catch (UnrecognizedFormatException e) {
            logger.error("Unrecognized file format for " + file, e);
            ErrorDialogsHelper.showError(dialogFactory.getOwner(), "Unrecognized file format for " + file);
            return failedFuture(e);
        } catch (IOException e) {
            logger.error("Cannot open " + file, e);
            ErrorDialogsHelper.showError(dialogFactory.getOwner(), "Cannot read " + file);
            return failedFuture(e);
        }
    }

    private static CompletionStage<DataSource> failedFuture(Throwable th) {
        var future = new CompletableFuture<DataSource>();
        future.completeExceptionally(th);
        return future;
    }
}
