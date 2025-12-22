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

import static name.mlopatkin.andlogview.utils.MyFutures.failedFuture;

import name.mlopatkin.andlogview.ErrorDialogsHelper;
import name.mlopatkin.andlogview.liblogcat.file.FileDataSourceFactory;
import name.mlopatkin.andlogview.liblogcat.file.ImportProblem;
import name.mlopatkin.andlogview.liblogcat.file.UnrecognizedFormatException;
import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.preferences.LastUsedDirPref;
import name.mlopatkin.andlogview.ui.FileDialog;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;
import name.mlopatkin.andlogview.utils.CommonChars;
import name.mlopatkin.andlogview.utils.TextUtils;
import name.mlopatkin.andlogview.widgets.dialogs.OptionPaneBuilder;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

/**
 * Handles opening a files as a {@link DataSource}. Shows error or warning dialogs if the file cannot be opened
 * properly.
 */
public class FileOpener {
    private static final Logger logger = LoggerFactory.getLogger(FileOpener.class);
    private static final int MAX_PROBLEMS_DISPLAYED = 10;

    private final DialogFactory dialogFactory;
    private final FileDialog fileDialog;
    private final LastUsedDirPref lastUsedDirPref;

    @Inject
    FileOpener(DialogFactory dialogFactory, FileDialog fileDialog, LastUsedDirPref lastUsedDirPref) {
        this.dialogFactory = dialogFactory;
        this.fileDialog = fileDialog;
        this.lastUsedDirPref = lastUsedDirPref;
    }

    /**
     * Tries to open a file as a {@link DataSource}. Handles the whole UI flow, including showing the error dialogs if
     * things go wrong. Does not update the source in the main frame, though.
     * <p>
     * This method must be called on UI thread.
     *
     * @param file the file to open
     * @return the cancellable handle to stop initialization
     */
    public CompletableFuture<DataSource> openFile(File file) {
        return openFileAsDataSource(file);
    }

    /**
     * Presents a dialog to the user and tries to open a file as a {@link DataSource}. Handles the whole UI flow,
     * including showing the error dialogs if things go wrong. Does not update the source in the main frame, though.
     * <p>
     * If the user cancels the dialog, then the consumer receives null as the data source.
     * <p>
     * This method must be called on UI thread.
     *
     * @return the cancellable handle to stop initialization
     */
    @SuppressWarnings("RedundantCast") // The cast helps NullAway to infer the proper nullability
    public CompletableFuture<@Nullable DataSource> selectAndOpenFile() {
        return fileDialog.selectFileToOpen()
                .map(file -> (CompletableFuture<@Nullable DataSource>) openFileAsDataSource(file))
                .orElse(CompletableFuture.completedFuture(null));
    }

    private CompletableFuture<DataSource> openFileAsDataSource(File file) {
        try {
            var importResult = FileDataSourceFactory.createDataSource(file);
            var parentFile = file.getAbsoluteFile().getParentFile();
            if (parentFile != null) {
                // The null pathname is unlikely, as the file can be opened. We don't want to reset the preference if
                // this is the case though.
                lastUsedDirPref.set(parentFile);
            }
            showImportProblemsIfNeeded(importResult.getProblems());
            return CompletableFuture.completedFuture(importResult.getDataSource());
        } catch (UnrecognizedFormatException e) {
            logger.error("Unrecognized file format for {}", file, e);
            ErrorDialogsHelper.showError(dialogFactory.getOwner(), "Unrecognized file format for " + file);
            return failedFuture(e);
        } catch (IOException e) {
            logger.error("Cannot open {}", file, e);
            ErrorDialogsHelper.showError(dialogFactory.getOwner(), "Cannot read " + file);
            return failedFuture(e);
        }
    }

    private void showImportProblemsIfNeeded(Collection<ImportProblem> problems) {
        if (problems.isEmpty()) {
            return;
        }

        var numProblems = problems.size();
        int problemsToShow = Math.min(numProblems, MAX_PROBLEMS_DISPLAYED);
        StringBuilder warningMessage = new StringBuilder("<html>");
        warningMessage.append("File import finished with ")
                .append(TextUtils.plural(numProblems, "a problem", "problems"))
                .append(':');


        warningMessage.append("<ul>");
        formatProblemsList(problemsToShow, problems, warningMessage);
        warningMessage.append("</ul>");
        if (problemsToShow < numProblems) {
            warningMessage.append(CommonChars.ELLIPSIS)
                    .append("and ")
                    .append(numProblems - problemsToShow)
                    .append(" more.");
        }
        warningMessage.append("</html>");

        OptionPaneBuilder.warning(TextUtils.plural(numProblems, "Import problem", numProblems + " import problems"))
                .message(warningMessage.toString())
                .show(dialogFactory.getOwner());
    }

    private void formatProblemsList(int problemsToShow, Collection<ImportProblem> problems, StringBuilder output) {
        assert problems.size() >= problemsToShow;

        problems.stream()
                .limit(problemsToShow)
                .map(ImportProblem::getMessage)
                .forEachOrdered(msg -> output.append("<li>").append(msg).append("</li>"));
    }
}
