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

package org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Presenter for the FilterEditor dialog. Allows to open dialog to create new or edit existing filter.
 */
class FilterDialogPresenter {
    /**
     * Presenter talks to view through this interface.
     */
    public interface FilterDialogView {
        void setTagsText(String text);

        String getTagsText();

        void setMessageText(String text);

        String getMessageText();

        void setPidsAppsText(String text);

        String getPidsAppsText();

        void setPriority(LogRecord.Priority priority);

        Optional<LogRecord.Priority> getPriority();

        void setMode(FilteringMode mode);

        FilteringMode getMode();

        void setHighlightColor(Color color);

        Optional<Color> getHighlightColor();

        void setCommitAction(Runnable commitAction);

        void setDiscardAction(Runnable discardAction);

        void show();

        void hide();

        void showError(String text);
    }

    private static final String ITEM_DELIMITER = ", ";
    private static final Splitter commaSplitter =
            Splitter.on(',').trimResults(CharMatcher.whitespace()).omitEmptyStrings();

    private final FilterDialogView dialogView;
    private CompletableFuture<Optional<FilterFromDialog>> editingPromise;

    private FilterDialogPresenter(FilterDialogView dialogView) {
        this.dialogView = dialogView;

        dialogView.setTagsText("");
        dialogView.setMessageText("");
        dialogView.setPidsAppsText("");
        dialogView.setPriority(null);
        dialogView.setMode(FilteringMode.getDefaultMode());
    }

    private FilterDialogPresenter(FilterDialogView dialogView, FilterFromDialog existingFilter) {
        this.dialogView = dialogView;

        dialogView.setTagsText(String.join(ITEM_DELIMITER, nullToEmpty(existingFilter.getTags())));
        dialogView.setMessageText(Strings.nullToEmpty(existingFilter.getMessagePattern()));
        dialogView.setPidsAppsText(
                Stream.concat(
                        nullToEmpty(existingFilter.getPids()).stream().map(String::valueOf),
                        nullToEmpty(existingFilter.getApps()).stream()).collect(Collectors.joining(ITEM_DELIMITER)));
        dialogView.setPriority(existingFilter.getPriority());
        dialogView.setMode(existingFilter.getMode());
        if (existingFilter.getMode() == FilteringMode.HIGHLIGHT) {
            dialogView.setHighlightColor(existingFilter.getHighlightColor());
        }
    }

    private FilterDialogPresenter init() {
        dialogView.setCommitAction(this::onDialogCommit);
        dialogView.setDiscardAction(this::onDialogDiscard);
        return this;
    }

    private void onDialogCommit() {
        assert editingPromise != null;
        if (editingPromise.isDone()) {
            // Quick clicking on buttons can cause one more commit/discard.
            return;
        }
        try {
            FilterFromDialog filter = createFilter();
            dialogView.hide();
            editingPromise.complete(Optional.of(filter));
        } catch (RequestCompilationException e) {
            dialogView.showError(
                    String.format("%s is not a valid search expression: %s", e.getRequestValue(), e.getMessage()));
        }
    }

    private void onDialogDiscard() {
        assert editingPromise != null;
        if (editingPromise.isDone()) {
            // Quick clicking on buttons can cause one more commit/discard.
            return;
        }
        dialogView.hide();
        editingPromise.complete(Optional.empty());
    }

    public CompletionStage<Optional<FilterFromDialog>> show() {
        Preconditions.checkState(editingPromise == null, "Dialog is already shown");
        dialogView.show();
        editingPromise = new CompletableFuture<>();
        return editingPromise;
    }

    public static FilterDialogPresenter create(FilterDialogView dialogView) {
        return new FilterDialogPresenter(dialogView).init();
    }

    public static FilterDialogPresenter create(FilterDialogView dialogView, FilterFromDialog existingFilter) {
        return new FilterDialogPresenter(dialogView, existingFilter).init();
    }

    private FilterFromDialog createFilter() throws RequestCompilationException {
        FilterFromDialog filter = new FilterFromDialog();
        parseTags(filter);
        filter.setMessagePattern(Strings.emptyToNull(dialogView.getMessageText().trim()));
        parseAppsAndPids(filter);
        dialogView.getPriority().map(filter::setPriority);
        filter.setMode(dialogView.getMode());
        dialogView.getHighlightColor().map(filter::setHighlightColor);
        filter.initialize();
        return filter;
    }

    private static <T> List<T> nullToEmpty(List<T> nullableList) {
        return nullableList != null ? nullableList : Collections.emptyList();
    }

    private static <T> List<T> emptyToNull(List<T> list) {
        return !list.isEmpty() ? list : null;
    }

    private void parseTags(FilterFromDialog filter) {
        filter.setTags(emptyToNull(commaSplitter.splitToList(Strings.nullToEmpty(dialogView.getTagsText()))));
    }

    private void parseAppsAndPids(FilterFromDialog filter) {
        List<String> appNames = new ArrayList<>();
        List<Integer> pids = new ArrayList<>();
        for (String item : commaSplitter.split(Strings.nullToEmpty(dialogView.getPidsAppsText()))) {
            try {
                int pid = Integer.parseInt(item);
                pids.add(pid);
            } catch (NumberFormatException e) {
                // Not a pid, probably an app name
                appNames.add(item);
            }
        }
        filter.setApps(emptyToNull(appNames));
        filter.setPids(emptyToNull(pids));
    }
}
