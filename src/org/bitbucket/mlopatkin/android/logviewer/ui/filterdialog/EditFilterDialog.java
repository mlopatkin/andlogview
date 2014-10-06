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

import java.awt.Frame;

public class EditFilterDialog extends FilterDialog {
    private FilterFromDialog filter;

    private static final Joiner ITEM_JOINER = Joiner.on(", ");

    protected EditFilterDialog(Frame owner, FilterFromDialog filter) {
        super(owner);
        setTitle("Edit filter");
        this.filter = filter;

        getMessageTextField().setText(filter.getMessagePattern());
        getPidTextField().setText(ITEM_JOINER.join(Iterables.concat(filter.getPids(), filter.getApps())));
        getTagTextField().setText(ITEM_JOINER.join(filter.getTags()));
        getLogLevelList().setSelectedItem(filter.getPriority());
        getModePanel().setSelectedMode(filter.getMode());
        setSelectedColor(filter.getHighlightColor());
    }

    @Override
    protected void onPositiveResult() {
        if (!isInputValid()) {
            return;
        }
        setVisible(false);
    }

    @Override
    protected void onNegativeResult() {
        setVisible(false);
    }

//    public static void startEditFilterDialog(Frame owner, FilteringMode mode,
//            FilterFromDialog filter, DialogResultReceiver resultReceiver, Object data) {
//        if (filter == null) {
//            throw new NullPointerException("Filter should be not null");
//        }
//        if (resultReceiver == null) {
//            throw new NullPointerException("resultReceiver should be not null");
//        }
//        if (mode == null) {
//            throw new NullPointerException("mode should be not null");
//        }
//        EditFilterDialog dialog = new EditFilterDialog(owner, mode, filter, resultReceiver, data);
//        dialog.setVisible(true);
//    }
}
