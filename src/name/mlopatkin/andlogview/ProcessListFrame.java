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
package name.mlopatkin.andlogview;

import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.widgets.TableCellHelper;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Displays list of available processes and their pids.
 */
@SuppressWarnings("NullAway")
public class ProcessListFrame extends JFrame {
    private JTable table;
    private JFrame owner;

    public ProcessListFrame(JFrame owner) {
        this.owner = owner;
        setTitle("Processes");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        table = new JTable();
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setAutoCreateColumnsFromModel(false);
        scrollPane.setViewportView(table);
        pack();
    }

    private static final TableModel EMPTY_MODEL = new DefaultTableModel();

    private void reset() {
        updateTimer.stop();
        model = null;
        table.setModel(EMPTY_MODEL);
    }

    private static final List<SortKey> DEFAULT_SORTING =
            Arrays.asList(new SortKey(ProcessListModel.COLUMN_PID, SortOrder.ASCENDING));

    public void setSource(@Nullable DataSource source) {
        assert SwingUtilities.isEventDispatchThread();
        @SuppressWarnings("unchecked")
        List<SortKey> oldKeys = (List<SortKey>) table.getRowSorter().getSortKeys();
        reset();
        if (source != null) {
            assert source.getPidToProcessConverter() != null;

            model = new ProcessListModel(source.getPidToProcessConverter());

            table.setModel(model);
            table.setColumnModel(new ColumnModel());
            if (oldKeys.isEmpty()) {
                table.getRowSorter().setSortKeys(DEFAULT_SORTING);
            } else {
                table.getRowSorter().setSortKeys(oldKeys);
            }
            if (Configuration.ui.hideLoggingProcesses()) {
                @SuppressWarnings("unchecked")
                TableRowSorter<ProcessListModel> sorter = (TableRowSorter<ProcessListModel>) table.getRowSorter();
                sorter.setRowFilter(new ProcessesRowFilter());
            }
            updateTimer.start();
        } else {
            if (isVisible()) {
                setVisible(false);
            }
        }
    }

    private ProcessListModel model;

    private static final String[] COLUMN_NAMES = new String[] {"PID", "Process"};
    private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {Integer.class, String.class};

    @SuppressWarnings("rawtypes")
    private static class ProcessListModel extends AbstractTableModel {
        static final int COLUMN_PID = 0;
        static final int COLUMN_PROCESS = 1;

        private static final int COLUMNS_COUNT = 2;

        private List[] items = new List<?>[COLUMNS_COUNT];
        private Map<Integer, String> mapper;

        public ProcessListModel(Map<Integer, String> mapper) {
            int l = mapper.size();
            this.mapper = mapper;
            items[COLUMN_PID] = new ArrayList<Integer>(l);
            items[COLUMN_PROCESS] = new ArrayList<Integer>(l);
            fillItems();
        }

        @SuppressWarnings("unchecked")
        private void fillItems() {
            for (Entry<Integer, String> entry : mapper.entrySet()) {
                items[COLUMN_PID].add(entry.getKey());
                items[COLUMN_PROCESS].add(entry.getValue());
            }
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return COLUMN_CLASSES[column];
        }

        @Override
        public int getColumnCount() {
            return COLUMNS_COUNT;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }

        @Override
        public int getRowCount() {
            return items[COLUMN_PID].size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return items[columnIndex].get(rowIndex);
        }

        public void update() {
            assert SwingUtilities.isEventDispatchThread();
            int oldSize = getRowCount();
            for (List l : items) {
                l.clear();
            }
            fillItems();
            int newSize = getRowCount();
            if (newSize < oldSize) {
                if (newSize > 0) {
                    fireTableRowsUpdated(0, newSize - 1);
                }
                fireTableRowsDeleted(newSize, oldSize - 1);
            } else if (newSize > oldSize) {
                if (oldSize > 0) {
                    fireTableRowsUpdated(0, oldSize - 1);
                }
                fireTableRowsInserted(oldSize, newSize - 1);
            } else {
                if (newSize > 0) {
                    fireTableRowsUpdated(0, newSize - 1);
                }
            }
        }
    }

    private static final int UPDATE_DELAY_MS = 1000;

    private Timer updateTimer = new Timer(UPDATE_DELAY_MS, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.update();
        }
    });

    private static class ColumnModel extends DefaultTableColumnModel {
        private TableCellEditor readOnlyEditor = TableCellHelper.createReadOnlyCellTextEditor();

        private TableColumn column(int columnIndex, int width) {
            TableColumn c = new TableColumn(columnIndex, width);
            c.setHeaderValue(COLUMN_NAMES[columnIndex]);
            c.setCellEditor(readOnlyEditor);
            addColumn(c);
            return c;
        }

        public ColumnModel() {
            column(ProcessListModel.COLUMN_PID, 50).setMaxWidth(200);
            column(ProcessListModel.COLUMN_PROCESS, 100);
        }
    }

    private static class ProcessesRowFilter extends RowFilter<ProcessListModel, Integer> {
        private static final Set<String> HIDDEN_PROCESSES = new HashSet<>();

        static {
            HIDDEN_PROCESSES.add("ps");
            HIDDEN_PROCESSES.add("logcat");
            HIDDEN_PROCESSES.add("/system/bin/sh");
            HIDDEN_PROCESSES.add("No info available");
        }

        @Override
        public boolean include(javax.swing.RowFilter.Entry<? extends ProcessListModel, ? extends Integer> entry) {
            ProcessListModel model = entry.getModel();
            int row = entry.getIdentifier();
            String processName = (String) model.getValueAt(row, ProcessListModel.COLUMN_PROCESS);
            return !HIDDEN_PROCESSES.contains(processName);
        }
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            Point position = Configuration.ui.processWindowPosition();
            if (position == null) {
                setLocationRelativeTo(owner);
            } else {
                setLocation(position);
            }
        } else {
            Configuration.ui.processWindowPosition(getLocation());
        }
        super.setVisible(b);
    }
}
