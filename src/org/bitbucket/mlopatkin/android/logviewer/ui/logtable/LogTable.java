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
import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingRendererTable;
import org.bitbucket.mlopatkin.android.logviewer.widgets.SortingDisableSorter;

import javax.swing.RowFilter;

/**
 * The ultimate log displaying table.
 */
public class LogTable extends DecoratingRendererTable {

    private final FilteredLogModel filterModel;

    private final RowFilter<LogRecordTableModel, Integer> rowFilter = new RowFilter<LogRecordTableModel, Integer>() {
        @Override
        public boolean include(Entry<? extends LogRecordTableModel, ? extends Integer> entry) {
            return filterModel.shouldShowRecord(entry.getModel().getRowData(entry.getIdentifier()));
        }
    };

    public LogTable(LogRecordTableModel dataModel, FilteredLogModel filterModel) {
        this.filterModel = filterModel;

        addDecorator(new PriorityColoredCellRenderer());

        setModel(dataModel);
        SortingDisableSorter<LogRecordTableModel> sorter = new SortingDisableSorter<>(dataModel);
        sorter.setRowFilter(rowFilter);
        setRowSorter(sorter);
    }
}
