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
package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

public class PinRecordsController {

    private LogRecordTableModel model;
    private IndexTableColumnModel columnsModel;
    private JTable mainTable;
    private JTable table;

    private IndexFrame frame;
    private TableRowSorter<LogRecordTableModel> rowSorter;
    private PinnedRowsFilter filter = new PinnedRowsFilter();
    private PinRecordsPopupMenuHandler popupMenuHandler;

    @SuppressWarnings("unchecked")
    public PinRecordsController(JTable mainTable, LogRecordTableModel model, DataSource source,
            FilterController filterController) {
        this.model = model;
        this.mainTable = mainTable;

        columnsModel = new IndexTableColumnModel(source.getPidToProcessConverter());

        frame = new IndexFrame(model, columnsModel, this);
        table = frame.getTable();
        rowSorter = new SortingDisableSorter<LogRecordTableModel>(model);
        table.setRowSorter(rowSorter);
        LogRecordRowFilter showHideFilter = filterController.getRowFilter();

        rowSorter.setRowFilter(RowFilter.andFilter(Arrays.asList(filter, showHideFilter)));

        popupMenuHandler = new PinRecordsPopupMenuHandler(table, this);

        filterController.addRefreshListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                rowSorter.sort();
            }
        });
    }

    public void pinRecord(int index) {
        if (!frame.isVisible()) {
            frame.setVisible(true);
        }
        filter.pin(index);
        rowSorter.sort();
    }

    public void unpinRecord(int index) {
        filter.unpin(index);
        rowSorter.sort();
    }

    private class PinnedRowsFilter extends RowFilter<LogRecordTableModel, Integer> {

        private Set<Integer> pinnedRows = new HashSet<Integer>();

        public void pin(int index) {
            pinnedRows.add(index);
        }

        public void unpin(int index) {
            pinnedRows.remove(index);
        }

        @Override
        public boolean include(
                javax.swing.RowFilter.Entry<? extends LogRecordTableModel, ? extends Integer> entry) {
            LogRecord record = entry.getModel().getRowData(entry.getIdentifier());
            return include(entry.getIdentifier(), record);
        }

        private boolean include(int row, LogRecord record) {
            return pinnedRows.contains(row);
        }
    }

    public void showWindow() {
        frame.setVisible(true);
    }

    public void activateRow(int row) {
        int rowTable = mainTable.convertRowIndexToView(row);
        mainTable.getSelectionModel().setSelectionInterval(rowTable, rowTable);
        mainTable.scrollRectToVisible(mainTable.getCellRect(rowTable,
                mainTable.getSelectedColumn(), false));
    }

}
