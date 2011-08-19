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

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordFilter;
import org.bitbucket.mlopatkin.android.liblogcat.PidToProcessConverter;

public class WindowFilterController extends AbstractIndexController implements IndexController {

    private static final Logger logger = Logger.getLogger(WindowFilterController.class);
    private TableRowSorter<LogRecordTableModel> rowSorter;
    private FilterController filterController;
    private LogRecordFilter filter;

    @SuppressWarnings("unchecked")
    public WindowFilterController(JTable mainTable, LogRecordTableModel model,
            PidToProcessConverter converter, FilterController filterController,
            LogRecordFilter filter) {
        super(mainTable, model, converter, filterController);
        this.filterController = filterController;
        this.filter = filter;

        JTable table = getFrame().getTable();
        rowSorter = new SortingDisableSorter<LogRecordTableModel>(model);
        LogRecordRowFilter showHideFilter = filterController.getRowFilter();

        rowSorter.setRowFilter(RowFilter.andFilter(Arrays.asList(
                new LogRecordFilterWrapper(filter), showHideFilter)));
        table.setRowSorter(rowSorter);
    }

    @Override
    protected void onMainTableUpdate() {
        rowSorter.sort();
    }

    @Override
    public void onWindowClosed() {
        logger.debug("onWindowClosed");
        filterController.disableFilter(FilteringMode.WINDOW, filter);
    }

    public void dispose() {
        getFrame().dispose();
    }
}
