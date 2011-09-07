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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.widgets.SortingDisableSorter;

public class BookmarksController extends AbstractIndexController implements IndexController {

    private JTable table;

    private TableRowSorter<LogRecordTableModel> rowSorter;
    private BookmarksRowFilter filter = new BookmarksRowFilter();

    @SuppressWarnings("unchecked")
    public BookmarksController(JTable mainTable, LogRecordTableModel model,
            PidToProcessMapper mapper, FilterController filterController) {
        super(mainTable, model, mapper, filterController);
        getFrame().setTitle("Bookmarks");

        table = getFrame().getTable();
        rowSorter = new SortingDisableSorter<LogRecordTableModel>(model);
        table.setRowSorter(rowSorter);
        LogRecordRowFilter showHideFilter = filterController.getRowFilter();

        rowSorter.setRowFilter(RowFilter.andFilter(Arrays.asList(filter, showHideFilter)));

        new BookmarksPopupMenuHandler(table, this);
    }

    public void markRecord(int index) {
        if (!getFrame().isVisible()) {
            getFrame().setVisible(true);
        }
        filter.mark(index);
        update();
    }

    public void unmarkRecord(int index) {
        filter.unmark(index);
        update();
    }

    private class BookmarksRowFilter extends RowFilter<LogRecordTableModel, Integer> {

        private Set<Integer> markedRows = new HashSet<Integer>();

        public void mark(int index) {
            markedRows.add(index);
        }

        public void unmark(int index) {
            markedRows.remove(index);
        }

        @Override
        public boolean include(
                javax.swing.RowFilter.Entry<? extends LogRecordTableModel, ? extends Integer> entry) {
            LogRecord record = entry.getModel().getRowData(entry.getIdentifier());
            return include(entry.getIdentifier(), record);
        }

        private boolean include(int row, LogRecord record) {
            return markedRows.contains(row);
        }

        public void clear() {
            markedRows.clear();
        }
    }

    public void showWindow() {
        getFrame().setVisible(true);
    }

    @Override
    protected void onMainTableUpdate() {
        update();
    }

    public void clear() {
        filter.clear();
        update();
    }

    private void update() {
        rowSorter.sort();
    }

}
