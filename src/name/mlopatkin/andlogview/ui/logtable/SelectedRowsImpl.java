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

package name.mlopatkin.andlogview.ui.logtable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.Objects;

import javax.inject.Inject;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

@LogTableScoped
class SelectedRowsImpl implements SelectedRows {

    private final JTable logTable;
    private final LogRecordTableModel tableModel;
    private final ListSelectionModel selectionModel;

    @Inject
    SelectedRowsImpl(LogTable table, LogRecordTableModel tableModel) {
        this((JTable) table, tableModel);
    }

    @VisibleForTesting
    SelectedRowsImpl(JTable table, LogRecordTableModel tableModel) {
        this.logTable = table;
        this.tableModel = tableModel;
        this.selectionModel = logTable.getSelectionModel();
    }

    @Override
    public ImmutableList<TableRow> getSelectedRows() {
        if (selectionModel.getMinSelectionIndex() < 0) {
            return ImmutableList.of();
        }
        ImmutableList.Builder<TableRow> builder = ImmutableList.builder();
        for (int i = selectionModel.getMinSelectionIndex(); i <= selectionModel.getMaxSelectionIndex(); ++i) {
            if (selectionModel.isSelectedIndex(i)) {
                builder.add(tableModel.getRow(getRowModelIndex(i)));
            }
        }
        return builder.build();
    }

    @Override
    public void clearSelection() {
        logTable.clearSelection();
    }

    @Override
    public void setSelectedRow(TableRow row) {
        Preconditions.checkArgument(Objects.equals(row.getRecord(), tableModel.getRowData(row.getRowIndex())),
                "Row index mismatch: at %s expected %s but found %s", row.getRowIndex(), row.getRecord(),
                tableModel.getRowData(row.getRowIndex()));
        selectionModel.setValueIsAdjusting(true);
        clearSelection();
        int viewIndex = getRowViewIndex(row);
        selectionModel.setSelectionInterval(viewIndex, viewIndex);
        selectionModel.setValueIsAdjusting(false);
    }

    @Override
    public boolean isRowSelected(TableRow row) {
        Preconditions.checkArgument(Objects.equals(row.getRecord(), tableModel.getRowData(row.getRowIndex())),
                "Row index mismatch: at %s expected %s but found %s", row.getRowIndex(), row.getRecord(),
                tableModel.getRowData(row.getRowIndex()));
        return selectionModel.isSelectedIndex(getRowViewIndex(row));
    }

    private int getRowViewIndex(TableRow row) {
        return logTable.convertRowIndexToView(row.getRowIndex());
    }

    private int getRowModelIndex(int viewIndex) {
        return logTable.convertRowIndexToModel(viewIndex);
    }
}
