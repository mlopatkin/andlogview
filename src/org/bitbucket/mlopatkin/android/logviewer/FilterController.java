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
import org.bitbucket.mlopatkin.android.liblogcat.MultiPidFilter;
import org.bitbucket.mlopatkin.android.liblogcat.MultiTagFilter;
import org.bitbucket.mlopatkin.android.liblogcat.PriorityFilter;

class FilterController implements CreateFilterDialog.DialogResultReceiver,
        EditFilterDialog.DialogResultReceiver {

    private DecoratingRendererTable table;
    private LogRecordTableModel tableModel;
    private FilterPanel panel;

    private TableRowSorter<LogRecordTableModel> rowSorter;
    private LogRecordRowFilter rowFilter;

    private FilterChain filters = new FilterChain();

    FilterController(DecoratingRendererTable table, LogRecordTableModel tableModel) {
        this.table = table;
        this.tableModel = tableModel;
        rowSorter = new SortingDisableSorter<LogRecordTableModel>(tableModel);
        table.setRowSorter(rowSorter);
        rowFilter = new LogRecordRowFilter(filters);
        rowSorter.setRowFilter(rowFilter);
        table.addDecorator(new RowHighlightRenderer(filters));
    }

    private void onFilteringStateUpdated() {
        rowSorter.sort();
        table.repaint();
        if (table.getSelectedRow() != -1) {
            table.scrollRectToVisible(table.getCellRect(table.getSelectedRow(), table
                    .getSelectedColumn(), false));
        }
    }

    void addFilter(FilteringMode mode, LogRecordFilter filter) {
        filters.addFilter(mode, filter);
        panel.addFilterButton(mode, filter);
        onFilteringStateUpdated();
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

    private LogRecordFilter createFilterFromDialog(FilterDialog dialog) {
        LogRecordFilter filter = null;
        if (dialog.getTags() != null) {
            filter = appendFilter(filter, new MultiTagFilter(dialog.getTags()));
        }
        if (dialog.getMessageText() != null) {
            filter = appendFilter(filter, new MessageFilter(dialog.getMessageText()));
        }
        if (dialog.getPids() != null) {
            filter = appendFilter(filter, new MultiPidFilter(dialog.getPids()));
        }
        if (dialog.getPriority() != null) {
            filter = appendFilter(filter, new PriorityFilter(dialog.getPriority()));
        }
        return filter;
    }

    private FilteringMode getModeFromDialog(FilterDialog dialog) {
        return dialog.getFilteringMode();
    }

    @Override
    public void onDialogResult(CreateFilterDialog dialog, boolean success) {
        if (success) {
            LogRecordFilter filter = createFilterFromDialog(dialog);
            FilteringMode mode = getModeFromDialog(dialog);
            if (filter != null) {
                addFilter(mode, filter);
            }
        }
    }

    public void startFilterCreationDialog() {
        CreateFilterDialog.startCreateFilterDialog(this);
    }

    void setPanel(FilterPanel panel) {
        this.panel = panel;
    }

    public void removeFilter(LogRecordFilter filter) {
        panel.removeFilterButton(filter);
        filters.removeFilter(filter);
        onFilteringStateUpdated();
    }

    public void startEditFilterDialog(FilteringMode mode, LogRecordFilter filter) {
        EditFilterDialog.startEditFilterDialog(mode, filter, this);
    }

    @Override
    public void onDialogResult(EditFilterDialog dialog, LogRecordFilter oldFilter, boolean success) {
        if (success) {
            LogRecordFilter filter = createFilterFromDialog(dialog);
            FilteringMode mode = getModeFromDialog(dialog);
            if (filter != null) {
                removeFilter(oldFilter);
                addFilter(mode, filter);
            } else {
                removeFilter(oldFilter);
            }
        }

    }
}
