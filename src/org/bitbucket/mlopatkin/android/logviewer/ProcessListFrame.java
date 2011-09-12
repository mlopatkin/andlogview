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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.bitbucket.mlopatkin.android.liblogcat.DataSource;

/**
 * Displays list of available processes and their pids.
 */
public class ProcessListFrame extends JFrame {
    private JTable table;

    public ProcessListFrame() {
        setTitle("Processes");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        table = new JTable();
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        scrollPane.setViewportView(table);
    }

    private static final TableModel EMPTY_MODEL = new DefaultTableModel();

    private void reset() {
        updateTimer.stop();
        model = null;
        table.setModel(EMPTY_MODEL);
    }

    public void setSource(DataSource source) {
        assert SwingUtilities.isEventDispatchThread();
        reset();
        if (source != null) {
            assert source.getPidToProcessConverter() != null;

            model = new ProcessListModel(source.getPidToProcessConverter());
            table.setModel(model);
            updateTimer.start();
        } else {
            if (isVisible()) {
                setVisible(false);
            }
        }
    }

    private ProcessListModel model;

    @SuppressWarnings("rawtypes")
    private class ProcessListModel extends AbstractTableModel {
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
            switch (column) {
            case COLUMN_PID:
                return "pid";
            case COLUMN_PROCESS:
                return "Process";
            }
            throw new ArrayIndexOutOfBoundsException(column);
        }

        @Override
        public int getColumnCount() {
            return COLUMNS_COUNT;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
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
            for (List l : items) {
                l.clear();
            }
            fillItems();
            fireTableDataChanged();
        }
    }

    private static final int UPDATE_DELAY_MS = 1000;

    private Timer updateTimer = new Timer(UPDATE_DELAY_MS, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            model.update();
        }
    });
}
