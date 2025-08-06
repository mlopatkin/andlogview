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

import name.mlopatkin.andlogview.logmodel.LogRecord;

import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

class PriorityComboBoxModel extends AbstractListModel<LogRecord.@Nullable Priority>
        implements ComboBoxModel<LogRecord.@Nullable Priority> {
    private @Nullable Object selected;

    @Override
    public @Nullable Object getSelectedItem() {
        return selected;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selected = anItem;
    }

    @Override
    @SuppressWarnings("EnumOrdinal")
    public LogRecord.@Nullable Priority getElementAt(int index) {
        if (index == 0) {
            return null;
        }
        return LogRecord.Priority.values()[index - 1];
    }

    @Override
    public int getSize() {
        return LogRecord.Priority.values().length + 1;
    }
}
