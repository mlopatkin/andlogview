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

package org.bitbucket.mlopatkin.android.logviewer.ui.logtable;

import org.bitbucket.mlopatkin.android.logviewer.PriorityColoredCellRenderer;
import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingCellRenderer;
import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingRendererTable;
import org.bitbucket.mlopatkin.android.logviewer.widgets.SortingDisableSorter;

import java.awt.Component;

import javax.inject.Inject;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableCellRenderer;

/**
 * The ultimate log displaying table.
 */
public class LogTable extends DecoratingRendererTable implements LogModelFilter.Observer {

    private final LogModelFilter filterModel;
    private final SortingDisableSorter<LogRecordTableModel> sorter;

    @Inject
    public LogTable(LogRecordTableModel dataModel, LogModelFilter filterModel) {
        this.filterModel = filterModel;

        filterModel.asObservable().addObserver(this);
        addDecorator(new PriorityColoredCellRenderer());
        addDecorator(new RowHighlightRenderer(filterModel));

        setModel(dataModel);
        sorter = new SortingDisableSorter<>(dataModel);

        RowFilter<LogRecordTableModel, Integer> rowFilter = new RowFilter<LogRecordTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends LogRecordTableModel, ? extends Integer> entry) {
                return LogTable.this.filterModel.shouldShowRecord(entry.getModel().getRowData(entry.getIdentifier()));
            }
        };

        sorter.setRowFilter(rowFilter);
        setRowSorter(sorter);
    }

    @Override
    public void onModelChange() {
        sorter.sort();
        repaint();
        // if the filtering state has changed and row is selected - scroll to selected row to avoid "get lost" syndrome
        if (getSelectedRow() != -1) {
            scrollRectToVisible(getCellRect(getSelectedRow(), getSelectedColumn(), false));
        }
    }
}
