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

import javax.swing.table.TableRowSorter;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecordFilter;
import org.bitbucket.mlopatkin.android.liblogcat.SingleTagFilter;

class FilterController implements NewFilterDialog.DialogResultReceiver {

    private DecoratingRendererTable table;
    private LogRecordTableModel tableModel;
    private FilterPanel panel;

    private TableRowSorter<LogRecordTableModel> defaultRowSorter;

    private NewFilterDialog newFilterDialog = new NewFilterDialog();

    FilterController(DecoratingRendererTable table, LogRecordTableModel tableModel) {
        this.table = table;
        this.tableModel = tableModel;
        defaultRowSorter = new TableRowSorter<LogRecordTableModel>(tableModel) {
            @Override
            public boolean isSortable(int column) {
                return false;
            }
        };
        table.setRowSorter(defaultRowSorter);
    }

    @Override
    public void onDialogResult(String tag) {
        if (tag != null) {
            LogRecordFilter filter = new SingleTagFilter(tag);
            LogRecordRowFilter rowFilter = new LogRecordRowFilter(filter);
            // defaultRowSorter.setRowFilter(rowFilter);
            table.addDecorator(new RowHighlightRenderer(rowFilter));
            panel.addFilterButton(filter);
        }
    }

    public void startFilterCreationDialog() {
        newFilterDialog.startDialogForResult(this);
    }

    void setPanel(FilterPanel panel) {
        this.panel = panel;
    }

    public void removeFilter(LogRecordFilter filter) {
        panel.removeFilterButton(filter);
    }

    public void startEditFilterDialog(LogRecordFilter filter) {
        // TODO Auto-generated method stub
    }
}
