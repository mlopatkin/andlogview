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

package name.mlopatkin.andlogview.ui.device;

import com.google.common.base.Preconditions;

import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

class DeviceSelectionModel extends DefaultListSelectionModel {
    private final DeviceListModel listModel;

    public DeviceSelectionModel(DeviceListModel listModel) {
        this.listModel = listModel;
        ListDataListener listDataListener = new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                if (affectsSelection(e)) {
                    clearSelection();
                }
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                if (affectsSelection(e) && !listModel.isSelectable(getMaxSelectionIndex())) {
                    clearSelection();
                }
            }
        };
        listModel.addListDataListener(listDataListener);
        setSelectionMode(SINGLE_SELECTION);
    }

    @Override
    public void setSelectionMode(int selectionMode) {
        Preconditions.checkArgument(selectionMode == SINGLE_SELECTION, "Only single selection is allowed, got %s",
                selectionMode);
        super.setSelectionMode(selectionMode);
    }

    @Override
    public void setSelectionInterval(int index0, int index1) {
        if (index1 == -1 || listModel.isSelectable(index1)) {
            super.setSelectionInterval(index0, index1);
        }
    }

    @Override
    public boolean isSelectedIndex(int index) {
        return super.isSelectedIndex(index);
    }

    private boolean affectsSelection(ListDataEvent e) {
        return !isSelectionEmpty() && (e.getIndex0() <= getMaxSelectionIndex()
                && getMaxSelectionIndex() <= e.getIndex1());
    }
}
