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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.filters.ComposeFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.LogBufferFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.LogRecordFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.MessageFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.MultiPidFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.MultiTagFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.PriorityFilter;
import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingRendererTable;
import org.bitbucket.mlopatkin.android.logviewer.widgets.SortingDisableSorter;

/**
 * This class manages all filter-related stuff: adding, removing, enabling, etc.
 * 
 */
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
    private LogBufferFilter bufferFilter = new LogBufferFilter();

    private EnumMap<FilteringMode, FilteringModeHandler<?>> handlers = new EnumMap<FilteringMode, FilteringModeHandler<?>>(
            FilteringMode.class);

    private FilterChainHandler showHideHandler = new FilterChainHandler();
    private IndexWindowHandler windowHandler = new IndexWindowHandler();
    private HighlightHandler highlightHandler = new HighlightHandler();

    FilterController(DecoratingRendererTable table, LogRecordTableModel tableModel) {
        this.table = table;
        this.tableModel = tableModel;
        rowSorter = new SortingDisableSorter<LogRecordTableModel>(tableModel);
        filters.addFilter(FilteringMode.HIDE, bufferFilter);
        initBufferFilter();
        table.setRowSorter(rowSorter);
        rowFilter = new LogRecordRowFilter(filters);
        rowSorter.setRowFilter(rowFilter);
        table.addDecorator(new RowHighlightRenderer(highlightHandler));
        handlers.put(FilteringMode.SHOW, showHideHandler);
        handlers.put(FilteringMode.HIDE, showHideHandler);
        handlers.put(FilteringMode.HIGHLIGHT, highlightHandler);
        handlers.put(FilteringMode.WINDOW, windowHandler);
    }

    private void onFilteringStateUpdated() {
        rowSorter.sort();
        table.repaint();
        if (table.getSelectedRow() != -1) {
            table.scrollRectToVisible(table.getCellRect(table.getSelectedRow(),
                    table.getSelectedColumn(), false));
        }
        for (ActionListener listener : refreshActionListeners) {
            listener.actionPerformed(null);
        }
    }

    private FilteringModeHandler<?> getHandler(FilteringMode mode) {
        return handlers.get(mode);
    }

    void addFilter(FilteringMode mode, LogRecordFilter filter) {
        addFilter(mode, filter, null);
    }

    <T> void addFilter(FilteringMode mode, LogRecordFilter filter, T data) {
        @SuppressWarnings("unchecked")
        FilteringModeHandler<T> handler = (FilteringModeHandler<T>) getHandler(mode);
        handler.addFilter(mode, filter, data);
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

    public void removeFilter(FilteringMode mode, LogRecordFilter filter) {
        panel.removeFilterButton(filter);
        getHandler(mode).removeFilter(mode, filter);
        onFilteringStateUpdated();
    }

    public void startEditFilterDialog(FilteringMode mode, LogRecordFilter filter) {
        EditFilterDialog.startEditFilterDialog(mode, filter, this);
    }

    @Override
    public void onDialogResult(EditFilterDialog dialog, FilteringMode oldMode,
            LogRecordFilter oldFilter, boolean success) {
        if (success) {
            LogRecordFilter filter = createFilterFromDialog(dialog);
            FilteringMode mode = getModeFromDialog(dialog);
            if (filter != null) {
                removeFilter(oldMode, oldFilter);
                addFilter(mode, filter);
            } else {
                removeFilter(oldMode, oldFilter);
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
        getHandler(mode).enableFilter(mode, filter);
        onFilteringStateUpdated();
    }

    void disableFilter(FilteringMode mode, LogRecordFilter filter) {
        getHandler(mode).disableFilter(mode, filter);
        onFilteringStateUpdated();
    }

    public void setBufferEnabled(Buffer buffer, boolean selected) {
        bufferFilter.setBufferEnabled(buffer, !selected);
        onFilteringStateUpdated();
    }

    private void initBufferFilter() {
        for (Buffer buffer : Buffer.values()) {
            bufferFilter.setBufferEnabled(buffer, !Configuration.ui.bufferEnabled(buffer));
            logger.debug(String.format("BufferFilter: %s %s", buffer,
                    !Configuration.ui.bufferEnabled(buffer)));
        }
    }

    /**
     * This interface is intended to hide mode-specific details of filter
     * handling. The map of mode handlers is used to lookup exact handler based
     * on mode.
     */
    interface FilteringModeHandler<T> {
        void addFilter(FilteringMode mode, LogRecordFilter filter, T data);

        void removeFilter(FilteringMode mode, LogRecordFilter filter);

        void enableFilter(FilteringMode mode, LogRecordFilter filter);

        void disableFilter(FilteringMode mode, LogRecordFilter filter);
    }

    private class FilterChainHandler implements FilteringModeHandler<Object> {

        @Override
        public void addFilter(FilteringMode mode, LogRecordFilter filter, Object data) {
            filters.addFilter(mode, filter);
        }

        @Override
        public void removeFilter(FilteringMode mode, LogRecordFilter filter) {
            filters.removeFilter(filter);
        }

        @Override
        public void enableFilter(FilteringMode mode, LogRecordFilter filter) {
            filters.addFilter(mode, filter);
        }

        @Override
        public void disableFilter(FilteringMode mode, LogRecordFilter filter) {
            filters.removeFilter(filter);
        }

    }

    private class IndexWindowHandler implements FilteringModeHandler<Object> {

        private Map<LogRecordFilter, WindowFilterController> windowControllers = new HashMap<LogRecordFilter, WindowFilterController>();

        @Override
        public void addFilter(FilteringMode mode, LogRecordFilter filter, Object data) {
            WindowFilterController controller = new WindowFilterController(table, tableModel, null,
                    FilterController.this, filter);
            controller.showWindow();
            windowControllers.put(filter, controller);
        }

        @Override
        public void removeFilter(FilteringMode mode, LogRecordFilter filter) {
            WindowFilterController windowController = windowControllers.remove(filter);
            if (windowController != null) {
                windowController.dispose();
            }
        }

        @Override
        public void enableFilter(FilteringMode mode, LogRecordFilter filter) {
            WindowFilterController windowController = windowControllers.get(filter);
            windowController.showWindow();
        }

        @Override
        public void disableFilter(FilteringMode mode, LogRecordFilter filter) {
            logger.debug("Disable filter");
            WindowFilterController windowController = windowControllers.get(filter);
            windowController.hideWindow();
            panel.getFilterButton(filter).setSelected(false);
        }

    }
}
