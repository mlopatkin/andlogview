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

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.ComposeFilter;
import org.bitbucket.mlopatkin.android.liblogcat.LogKindFilter;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordFilter;
import org.bitbucket.mlopatkin.android.liblogcat.MessageFilter;
import org.bitbucket.mlopatkin.android.liblogcat.MultiPidFilter;
import org.bitbucket.mlopatkin.android.liblogcat.MultiTagFilter;
import org.bitbucket.mlopatkin.android.liblogcat.PriorityFilter;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Kind;

class FilterController implements CreateFilterDialog.DialogResultReceiver,
        EditFilterDialog.DialogResultReceiver {

    private static final Logger logger = Logger.getLogger(FilterController.class);

    private DecoratingRendererTable table;
    private LogRecordTableModel tableModel;
    private FilterPanel panel;

    private TableRowSorter<LogRecordTableModel> rowSorter;
    private LogRecordRowFilter rowFilter;

    private FilterChain filters = new FilterChain();
    private List<ActionListener> refreshActionListeners = new ArrayList<ActionListener>();

    // this filter acts as hiding filter, initially empty, but hides unselected
    // buffers
    private LogKindFilter kindFilter = new LogKindFilter();
    private Map<LogRecordFilter, WindowFilterController> windowControllers = new HashMap<LogRecordFilter, WindowFilterController>();

    FilterController(DecoratingRendererTable table, LogRecordTableModel tableModel) {
        this.table = table;
        this.tableModel = tableModel;
        rowSorter = new SortingDisableSorter<LogRecordTableModel>(tableModel);
        filters.addFilter(FilteringMode.HIDE, kindFilter);
        initKindFilter();
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
        for (ActionListener listener : refreshActionListeners) {
            listener.actionPerformed(null);
        }
    }

    void addFilter(FilteringMode mode, LogRecordFilter filter) {
        if (mode == FilteringMode.WINDOW) {
            WindowFilterController controller = new WindowFilterController(table, tableModel, null,
                    this, filter);
            controller.showWindow();
            windowControllers.put(filter, controller);
        }

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
        windowControllers.remove(filter);
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

    LogRecordRowFilter getRowFilter() {
        return rowFilter;
    }

    public void addRefreshListener(ActionListener listener) {
        refreshActionListeners.add(listener);
    }

    public void removeRefreshListener(ActionListener listener) {
        refreshActionListeners.remove(listener);
    }

    void enableFilter(FilteringMode mode, LogRecordFilter filter) {
        filters.addFilter(mode, filter);
        if (mode == FilteringMode.WINDOW) {
            WindowFilterController windowController = windowControllers.get(filter);
            windowController.showWindow();
        }
        onFilteringStateUpdated();
    }

    void disableFilter(FilteringMode mode, LogRecordFilter filter) {
        filters.removeFilter(filter);
        if (mode == FilteringMode.WINDOW) {
            WindowFilterController windowController = windowControllers.get(filter);
            windowController.hideWindow();
        }
        onFilteringStateUpdated();
    }

    public void setBufferEnabled(Kind kind, boolean selected) {
        kindFilter.setKindEnabled(kind, !selected);
        onFilteringStateUpdated();
    }

    private void initKindFilter() {
        for (Kind kind : Kind.values()) {
            kindFilter.setKindEnabled(kind, !Configuration.ui.bufferEnabled(kind));
            logger.debug(String.format("KindFilter: %s %s", kind, !Configuration.ui
                    .bufferEnabled(kind)));
        }
    }
}
