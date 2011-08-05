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

import org.bitbucket.mlopatkin.android.liblogcat.ComposeFilter;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordFilter;
import org.bitbucket.mlopatkin.android.liblogcat.MessageFilter;
import org.bitbucket.mlopatkin.android.liblogcat.MultiTagFilter;

class FilterController implements NewFilterDialog.DialogResultReceiver {

    private DecoratingRendererTable table;
    private LogRecordTableModel tableModel;
    private FilterPanel panel;

    private TableRowSorter<LogRecordTableModel> defaultRowSorter;
    private LogRecordRowFilter rowFilter;

    private NewFilterDialog newFilterDialog = new NewFilterDialog();

    private FilterChain filters = new FilterChain();

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
        rowFilter = new LogRecordRowFilter(filters);
        defaultRowSorter.setRowFilter(rowFilter);
        table.addDecorator(new RowHighlightRenderer(filters));
    }

    private void addFilter(LogRecordFilter filter) {
        panel.addFilterButton(filter);
        table.repaint();
    }

    private void addHighlightFilter(LogRecordFilter filter) {
        filters.addHighlightFilter(filter);
        addFilter(filter);
    }

    private void addHideFilter(LogRecordFilter filter) {
        filters.addHideFilter(filter);
        defaultRowSorter.sort();
        addFilter(filter);
    }

    private static LogRecordFilter appendFilter(LogRecordFilter a, LogRecordFilter b) {
        if (a == null) {
            return b;
        } else if (a instanceof ComposeFilter) {
            return ((ComposeFilter) a).append(b);
        } else {
            return new ComposeFilter(a, b);
        }
    }

    @Override
    public void onDialogResult(boolean success) {
        if (success) {
            LogRecordFilter filter = null;
            if (newFilterDialog.getTags() != null) {
                filter = appendFilter(filter, new MultiTagFilter(newFilterDialog.getTags()));
            }
            if (newFilterDialog.getMessageText() != null) {
                filter = appendFilter(filter, new MessageFilter(newFilterDialog.getMessageText()));
            }
            if (filter != null) {
                if (newFilterDialog.isHideMode()) {
                    addHideFilter(filter);
                }
                if (newFilterDialog.isHighlightMode()) {
                    addHighlightFilter(filter);
                }
            }
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
        filters.removeFilter(filter);
        defaultRowSorter.sort();
        table.repaint();
    }

    public void startEditFilterDialog(LogRecordFilter filter) {
        // TODO Auto-generated method stub
    }
}
