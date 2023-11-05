/*
 * Copyright 2014 Mikhail Lopatkin
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

import name.mlopatkin.andlogview.ErrorDialogsHelper;
import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;
import name.mlopatkin.andlogview.widgets.UiHelper;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

/**
 * Common GUI logic related to filtering.
 */
class FilterDialog extends BaseFilterDialogUi implements FilterDialogPresenter.FilterDialogView {
    private @MonotonicNonNull Runnable commitAction;
    private @MonotonicNonNull Runnable discardAction;

    /**
     * Create the dialog.
     */
    @Inject
    public FilterDialog(DialogFactory dialogFactory) {
        super(dialogFactory.getOwner());

        okButton.addActionListener(e -> onPositiveResult());

        ActionListener cancelAction = e -> onNegativeResult();
        cancelButton.addActionListener(cancelAction);
        UiHelper.bindKeyGlobal(this, KeyEvent.VK_ESCAPE, "close", cancelAction);

        modesPanel.setModeChangedListener(mode -> {
            colorsList.setVisible(mode == FilteringMode.HIGHLIGHT);
            colorsList.revalidate();
            colorsList.repaint();
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                onNegativeResult();
            }
        });
    }

    protected void onPositiveResult() {
        assert commitAction != null;
        commitAction.run();
    }

    protected void onNegativeResult() {
        assert discardAction != null;
        discardAction.run();
    }

    @Override
    public void setTagsText(String text) {
        tagTextField.setText(text);
    }

    @Override
    public String getTagsText() {
        return tagTextField.getText();
    }

    @Override
    public void setMessageText(String text) {
        messageTextField.setText(text);
    }

    @Override
    public String getMessageText() {
        return messageTextField.getText();
    }

    @Override
    public void setPidsAppsText(String text) {
        pidTextField.setText(text);
    }

    @Override
    public String getPidsAppsText() {
        return pidTextField.getText();
    }

    @Override
    public void setPriority(LogRecord.@Nullable Priority priority) {
        logLevelList.setSelectedItem(priority);
    }

    @Override
    public Optional<LogRecord.Priority> getPriority() {
        return Optional.ofNullable((LogRecord.Priority) logLevelList.getSelectedItem());
    }

    @Override
    public void setMode(FilteringMode mode) {
        modesPanel.setSelectedMode(mode);
    }

    @Override
    public FilteringMode getMode() {
        return modesPanel.getSelectedMode();
    }

    @Override
    public void setHighlightColor(@Nullable Color color) {
        int index = 0;
        for (Color current : Configuration.ui.highlightColors()) {
            if (current.equals(color)) {
                colorsList.setSelectedIndex(index);
                return;
            } else {
                ++index;
            }
        }
    }

    @Override
    public Optional<Color> getHighlightColor() {
        if (getMode() == FilteringMode.HIGHLIGHT) {
            return Optional.of(Configuration.ui.highlightColors().get(colorsList.getSelectedIndex()));
        }

        return Optional.empty();
    }

    @Override
    public void setCommitAction(Runnable commitAction) {
        this.commitAction = Objects.requireNonNull(commitAction);
    }

    @Override
    public void setDiscardAction(Runnable discardAction) {
        this.discardAction = Objects.requireNonNull(discardAction);
    }

    @Override
    public void showError(String text) {
        ErrorDialogsHelper.showError(this, text);
    }

    @Override
    public void bringToFront() {
        this.toFront();
    }
}
