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

package name.mlopatkin.andlogview.ui.filterdialog;

import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.liblogcat.LogRecord;
import name.mlopatkin.andlogview.search.RequestCompilationException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

/**
 * Presenter for the FilterEditor dialog. Allows to open dialog to create new or edit existing filter.
 */
class FilterDialogPresenter implements FilterDialogHandle {
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

        void setPriority(LogRecord.@Nullable Priority priority);

        Optional<LogRecord.Priority> getPriority();

        void setMode(FilteringMode mode);

        FilteringMode getMode();

        void setHighlightColor(@Nullable Color color);

        Optional<Color> getHighlightColor();

        void setCommitAction(Runnable commitAction);

        void setDiscardAction(Runnable discardAction);

        void show();

        void hide();

        void showError(String text);

        void bringToFront();
    }

    private final FilterDialogView dialogView;
    private @MonotonicNonNull CompletableFuture<Optional<FilterFromDialog>> editingPromise;

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

        dialogView.setTagsText(PatternsList.join(nullToEmpty(existingFilter.getTags())));
        dialogView.setMessageText(Strings.nullToEmpty(existingFilter.getMessagePattern()));
        dialogView.setPidsAppsText(PatternsList.join(
                Stream.concat(
                        nullToEmpty(existingFilter.getPids()).stream().map(String::valueOf),
                        nullToEmpty(existingFilter.getApps()).stream())
        ));
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
                    formatErrorMessage(e.getMessage(), e.getRequestValue(), e.getStartPos(), e.getEndPos()));
        } catch (PatternsList.FormatException e) {
            dialogView.showError(formatErrorMessage(e.getMessage(), e.getPattern(), e.getStartPos(), e.getEndPos()));
        }
    }

    private String formatErrorMessage(@Nullable String errorMessage, String pattern, int beginError, int endError) {
        String brokenPatternSpan = pattern;
        if (beginError != -1) {
            brokenPatternSpan = pattern.substring(0, beginError);
            brokenPatternSpan += "<span style='background-color: red'>";
            if (endError != -1) {
                brokenPatternSpan += pattern.substring(beginError, endError);
                brokenPatternSpan += "</span>";
                brokenPatternSpan += pattern.substring(endError);
            } else {
                brokenPatternSpan += pattern.substring(beginError);
                brokenPatternSpan += "</span>";
            }
        }
        String result = "<html>Invalid filter expression: ";
        result += brokenPatternSpan;
        if (errorMessage != null) {
            result += "<br/>";
            result += errorMessage;
        }
        result += "</html>";
        return result;
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

    @Override
    public void bringToFront() {
        dialogView.bringToFront();
    }

    @Override
    public CompletionStage<Optional<FilterFromDialog>> getResult() {
        Preconditions.checkState(editingPromise != null, "Dialog is not shown");
        return editingPromise;
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

    private FilterFromDialog createFilter() throws RequestCompilationException, PatternsList.FormatException {
        FilterFromDialog filter = new FilterFromDialog();
        parseTags(filter);
        filter.setMessagePattern(Strings.emptyToNull(dialogView.getMessageText().trim()));
        parseAppsAndPids(filter);
        dialogView.getPriority().ifPresent(filter::setPriority);
        filter.setMode(dialogView.getMode());
        dialogView.getHighlightColor().ifPresent(filter::setHighlightColor);
        filter.initialize();
        return filter;
    }

    private static <T> List<T> nullToEmpty(@Nullable List<T> nullableList) {
        return nullableList != null ? nullableList : Collections.emptyList();
    }

    private static <T> @Nullable List<T> emptyToNull(List<T> list) {
        return !list.isEmpty() ? list : null;
    }

    private void parseTags(FilterFromDialog filter) throws PatternsList.FormatException {
        filter.setTags(emptyToNull(PatternsList.split(Strings.nullToEmpty(dialogView.getTagsText()))));
    }

    private void parseAppsAndPids(FilterFromDialog filter) throws PatternsList.FormatException {
        List<String> appNames = new ArrayList<>();
        List<Integer> pids = new ArrayList<>();
        for (String item : PatternsList.split(Strings.nullToEmpty(dialogView.getPidsAppsText()))) {
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
