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

package org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.ErrorDialogsHelper;
import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.DialogFactory;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import static org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilteringModesPanel.ModeChangedListener;

/**
 * Common GUI logic related to filtering.
 */
class FilterDialog extends BaseFilterDialogUi implements FilterDialogPresenter.FilterDialogView {
    private Runnable commitAction;
    private Runnable discardAction;

    /**
     * Create the dialog.
     */
    @Inject
    public FilterDialog(DialogFactory dialogFactory) {
        super(dialogFactory.getOwner());

        okButton.addActionListener(e -> onPositiveResult());
        cancelButton.addActionListener(e -> onNegativeResult());

        ModeChangedListener modeListener = new ModeChangedListener() {
            @Override
            public void modeSelected(FilteringMode mode) {
                colorsList.setVisible(mode == FilteringMode.HIGHLIGHT);
                colorsList.revalidate();
                colorsList.repaint();
            }
        };
        modesPanel.setModeChangedListener(modeListener);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                onNegativeResult();
            }
        });
    }

    protected void onPositiveResult() {
        commitAction.run();
    }

    protected void onNegativeResult() {
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
    public void setPriority(LogRecord.Priority priority) {
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
    public void setHighlightColor(Color color) {
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
}
