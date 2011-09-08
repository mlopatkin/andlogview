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

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingCellRenderer;
import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingRendererTable;
import org.bitbucket.mlopatkin.android.logviewer.widgets.SortingDisableSorter;

public class BookmarksController extends AbstractIndexController implements IndexController {

    private JTable table;

    private TableRowSorter<LogRecordTableModel> rowSorter;
    private BookmarksRowFilter filter = new BookmarksRowFilter();

    @SuppressWarnings("unchecked")
    public BookmarksController(DecoratingRendererTable mainTable, LogRecordTableModel model,
            PidToProcessMapper mapper, FilterController filterController) {
        super(mainTable, model, mapper, filterController);
        getFrame().setTitle("Bookmarks");

        table = getFrame().getTable();
        rowSorter = new SortingDisableSorter<LogRecordTableModel>(model);
        table.setRowSorter(rowSorter);
        LogRecordRowFilter showHideFilter = filterController.getRowFilter();

        rowSorter.setRowFilter(RowFilter.andFilter(Arrays.asList(filter, showHideFilter)));
        mainTable.addDecorator(new BookmarksHighlighter());
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

        boolean include(int row, LogRecord record) {
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
        getMainTable().repaint();
    }

    private class BookmarksHighlighter implements DecoratingCellRenderer {

        private TableCellRenderer inner;

        public void setInnerRenderer(TableCellRenderer renderer) {
            inner = renderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component cmp = inner.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
            int modelRow = table.convertRowIndexToModel(row);
            LogRecord record = getModel().getRowData(modelRow);
            if (filter.include(modelRow, record)) {
                highlight(cmp, isSelected);
            }

            return cmp;
        }

        private void highlight(Component cmp, boolean isSelected) {
            Color backgroundColor = Configuration.ui.bookmarkBackground();
            Color foregroundColor = Configuration.ui.bookmarkedForeground();
            if (isSelected) {
                backgroundColor = backgroundColor.brighter();
            }
            if (backgroundColor != null) {
                cmp.setBackground(backgroundColor);
            }
            if (foregroundColor != null) {
                cmp.setForeground(foregroundColor);
            }
        }

    }
}
