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

package name.mlopatkin.andlogview.ui.logtable;

import name.mlopatkin.andlogview.PriorityColoredCellRenderer;
import name.mlopatkin.andlogview.ui.themes.ThemeColors;
import name.mlopatkin.andlogview.widgets.DecoratingRendererTable;
import name.mlopatkin.andlogview.widgets.SortingDisableSorter;

import javax.swing.RowFilter;

/**
 * The ultimate log displaying table.
 */
@LogTableScoped
class LogTable extends DecoratingRendererTable implements LogModelFilter.Observer {
    private final LogModelFilter filterModel;
    private final SortingDisableSorter<LogRecordTableModel> sorter;

    private LogTable(ThemeColors themeColors, LogRecordTableModel dataModel, LogModelFilter filterModel) {
        this.filterModel = filterModel;

        addDecorator(new PriorityColoredCellRenderer(themeColors));
        addDecorator(new RowHighlightRenderer(filterModel, themeColors));

        setModel(dataModel);
        sorter = new SortingDisableSorter<>(dataModel);

        RowFilter<LogRecordTableModel, Integer> rowFilter = new RowFilter<>() {
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

    static LogTable create(ThemeColors themeColors, LogRecordTableModel dataModel, LogModelFilter filterModel) {
        LogTable logTable = new LogTable(themeColors, dataModel, filterModel);
        filterModel.asObservable().addObserver(logTable);
        return logTable;
    }
}
