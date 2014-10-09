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

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.filters.LogBufferFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.LogRecordFilter;
import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterFromDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.FilterPanel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;
import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingRendererTable;
import org.bitbucket.mlopatkin.android.logviewer.widgets.SortingDisableSorter;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.table.TableRowSorter;

/**
 * This class manages all filter-related stuff: adding, removing, enabling, etc.
 */
class FilterController {

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

    private EnumMap<FilteringMode, FilteringModeHandler<?>> handlers
            = new EnumMap<FilteringMode, FilteringModeHandler<?>>(
            FilteringMode.class);

    private FilterChainHandler showHideHandler = new FilterChainHandler();
    private IndexWindowHandler windowHandler = new IndexWindowHandler();
    private HighlightFilteringModeHandler highlightHandler = new HighlightFilteringModeHandler();

    private JFrame main;

    FilterController(JFrame main, DecoratingRendererTable table, LogRecordTableModel tableModel) {
        this.main = main;
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
            table.scrollRectToVisible(table.getCellRect(table.getSelectedRow(), table
                    .getSelectedColumn(), false));
        }
        for (ActionListener listener : refreshActionListeners) {
            listener.actionPerformed(null);
        }
    }

    private FilteringModeHandler<?> getHandler(FilteringMode mode) {
        return handlers.get(mode);
    }

    void addFilter(FilteringMode mode, LogRecordFilter filter) {

    }

    <T> void addFilter(FilteringMode mode, FilterFromDialog filter, T data) {
        @SuppressWarnings("unchecked")
        FilteringModeHandler<T> handler = (FilteringModeHandler<T>) getHandler(mode);
        handler.addFilter(mode, filter, data);
        onFilteringStateUpdated();
    }

    public void startFilterCreationDialog() {
//        CreateFilterDialog.startCreateFilterDialog(main, this);
    }

    void setPanel(FilterPanel panel) {
        this.panel = panel;
    }

    public void removeFilter(FilteringMode mode, LogRecordFilter filter) {
//        panel.removeFilterButton(filter);
        getHandler(mode).removeFilter(mode, filter);
        onFilteringStateUpdated();
    }

    public void startEditFilterDialog(FilteringMode mode, FilterFromDialog filter) {
        Object data = getHandler(mode).getData(mode, filter);
//        EditFilterDialog.startEditFilterDialog(main, mode, filter, this, data);
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

    void enableFilter(FilteringMode mode, Predicate<LogRecord> filter) {
        getHandler(mode).enableFilter(mode, filter);
        onFilteringStateUpdated();
    }

    void disableFilter(FilteringMode mode, Predicate<LogRecord> filter) {
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
        }
    }

    /**
     * This interface is intended to hide mode-specific details of filter
     * handling. The map of mode handlers is used to lookup exact handler based
     * on mode.
     */
    interface FilteringModeHandler<T> {

        void addFilter(FilteringMode mode, Predicate<LogRecord> filter, T data);

        void removeFilter(FilteringMode mode, Predicate<LogRecord> filter);

        void enableFilter(FilteringMode mode, Predicate<LogRecord> filter);

        void disableFilter(FilteringMode mode, Predicate<LogRecord> filter);

        T getData(FilteringMode mode, Predicate<LogRecord> filter);
    }

    private class FilterChainHandler implements FilteringModeHandler<Object> {

        @Override
        public void addFilter(FilteringMode mode, Predicate<LogRecord> filter, Object data) {
            filters.addFilter(mode, filter);
        }

        @Override
        public void removeFilter(FilteringMode mode, Predicate<LogRecord> filter) {
            filters.removeFilter(filter);
        }

        @Override
        public void enableFilter(FilteringMode mode, Predicate<LogRecord> filter) {
            filters.addFilter(mode, filter);
        }

        @Override
        public void disableFilter(FilteringMode mode, Predicate<LogRecord> filter) {
            filters.removeFilter(filter);
        }

        @Override
        public Object getData(FilteringMode mode, Predicate<LogRecord> filter) {
            return null;
        }

    }

    private class IndexWindowHandler implements FilteringModeHandler<Object> {

        private Map<Predicate<LogRecord>, WindowFilterController> windowControllers = Maps
                .newHashMap();

        @Override
        public void addFilter(FilteringMode mode, Predicate<LogRecord> filter, Object data) {
            WindowFilterController controller = new WindowFilterController(main, table, tableModel,
                    null,
                    FilterController.this, filter);
            controller.showWindow();
            windowControllers.put(filter, controller);
        }

        @Override
        public void removeFilter(FilteringMode mode, Predicate<LogRecord> filter) {
            WindowFilterController windowController = windowControllers.remove(filter);
            if (windowController != null) {
                windowController.dispose();
            }
        }

        @Override
        public void enableFilter(FilteringMode mode, Predicate<LogRecord> filter) {
            WindowFilterController windowController = windowControllers.get(filter);
            windowController.showWindow();
        }

        @Override
        public void disableFilter(FilteringMode mode, Predicate<LogRecord> filter) {
            logger.debug("Disable filter");
            WindowFilterController windowController = windowControllers.get(filter);
            windowController.hideWindow();
        }

        @Override
        public Object getData(FilteringMode mode, Predicate<LogRecord> filter) {
            return null;
        }

    }
}
