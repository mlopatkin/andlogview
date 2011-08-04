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
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import org.bitbucket.mlopatkin.android.liblogcat.SingleTagFilter;
import org.bitbucket.mlopatkin.android.logviewer.NewFilterDialog.DialogResultReceiver;

public class Main implements DialogResultReceiver {

    private JFrame frmAndroidLogViewer;
    private JTable logElements;
    private JScrollPane scrollPane;

    private LogRecordsTableModel recordsModel = new LogRecordsTableModel();
    private AutoScrollController scrollController;
    private JPanel filterPanel;

    private NewFilterDialog newFilterDialog;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Main window = new Main();
                    window.frmAndroidLogViewer.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Create the application.
     */
    public Main() {
        initialize();
        scrollController = new AutoScrollController(logElements, recordsModel);
        defaultRowSorter = new TableRowSorter<LogRecordsTableModel>(recordsModel);
        logElements.setRowSorter(defaultRowSorter);

        newFilterDialog = new NewFilterDialog();

        final AdbDataSource source = new AdbDataSource(scrollController);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                source.close();
            }
        });
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmAndroidLogViewer = new JFrame();
        frmAndroidLogViewer.setTitle("Android Log Viewer");
        frmAndroidLogViewer.setBounds(100, 100, 1000, 450);
        frmAndroidLogViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logElements = new JTable();
        logElements.setFillsViewportHeight(true);
        logElements.setShowGrid(false);

        logElements.setModel(recordsModel);
        logElements.setDefaultRenderer(Object.class, new PriorityColoredCellRenderer());
        logElements.setColumnModel(new LogcatTableColumnModel(Configuration.ui.columns()));

        scrollPane = new JScrollPane(logElements);
        frmAndroidLogViewer.getContentPane().add(scrollPane, BorderLayout.CENTER);

        filterPanel = new JPanel();
        filterPanel.addMouseListener(new FilterPanelClickListener());
        frmAndroidLogViewer.getContentPane().add(filterPanel, BorderLayout.SOUTH);
    }

    private class FilterPanelClickListener extends MouseAdapter implements MouseListener {
        private static final int DOUBLE_CLICK_COUNT = 2;
        @Override
        public void mouseClicked(MouseEvent e) {
            // double click to add filter
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == DOUBLE_CLICK_COUNT) {
                startFilterCreationDialog();
            }
        }
    }

    private void startFilterCreationDialog() {
        newFilterDialog.startDialogForResult(this);
    }

    private TableRowSorter<LogRecordsTableModel> defaultRowSorter;

    @Override
    public void onDialogResult(String tag) {
        if (tag != null) {
            LogRecordRowFilter filter = new LogRecordRowFilter(new SingleTagFilter(tag));
            defaultRowSorter.setRowFilter(filter);
        }
    }
}
