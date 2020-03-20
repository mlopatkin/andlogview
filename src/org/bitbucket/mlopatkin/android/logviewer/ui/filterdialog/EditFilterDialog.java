/*
 * Copyright 2011 Mikhail Lopatkin
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

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import org.bitbucket.mlopatkin.android.logviewer.ErrorDialogsHelper;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;

import java.awt.Frame;
import java.util.Optional;

import javax.swing.SwingUtilities;

public class EditFilterDialog extends FilterDialog {
    public interface DialogResultReceiver {
        void onDialogResult(FilterFromDialog oldFilter, Optional<FilterFromDialog> newFilter);
    }

    private final FilterFromDialog originalFilter;
    private final DialogResultReceiver receiver;

    private static final Joiner ITEM_JOINER = Joiner.on(", ");

    protected EditFilterDialog(Frame owner, FilterFromDialog filter, DialogResultReceiver receiver) {
        super(owner);
        this.receiver = receiver;
        setTitle("Edit filter");
        this.originalFilter = filter;

        getMessageTextField().setText(filter.getMessagePattern());
        getPidTextField().setText(ITEM_JOINER.join(Iterables.concat(filter.getPids(), filter.getApps())));
        getTagTextField().setText(ITEM_JOINER.join(filter.getTags()));
        getLogLevelList().setSelectedItem(filter.getPriority());
        getModePanel().setSelectedMode(filter.getMode());
        setSelectedColor(filter.getHighlightColor());
    }

    @Override
    protected void onPositiveResult() {
        try {
            receiver.onDialogResult(originalFilter, Optional.of(createFilter()));
        } catch (RequestCompilationException e) {
            ErrorDialogsHelper.showError(
                    this, "%s is not a valid search expression: %s", e.getRequestValue(), e.getMessage());
            return;
        }
        setVisible(false);
    }

    @Override
    protected void onNegativeResult() {
        receiver.onDialogResult(originalFilter, Optional.empty());
        setVisible(false);
    }

    public static void startEditFilterDialog(
            Frame owner, FilterFromDialog filter, DialogResultReceiver resultReceiver) {
        assert SwingUtilities.isEventDispatchThread();

        EditFilterDialog editFilterDialog = new EditFilterDialog(owner, filter, resultReceiver);
        editFilterDialog.setVisible(true);
    }
}
