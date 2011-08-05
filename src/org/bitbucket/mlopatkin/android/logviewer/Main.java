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
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.bitbucket.mlopatkin.android.liblogcat.DumpstateFileDataSource;

public class Main {

    private JFrame frmAndroidLogViewer;
    private DecoratingRendererTable logElements;
    private JScrollPane scrollPane;

    private LogRecordTableModel recordsModel = new LogRecordTableModel();
    private AutoScrollController scrollController;
    private FilterController filterController;

    /**
     * Launch the application.
     */
    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Main window = new Main(args);
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
    public Main(String[] args) {
        initialize();

        if (args.length > 0) {
            for (String fileName : args) {
                new DumpstateFileDataSource(scrollController, new File(fileName));
            }
        } else {
            final AdbDataSource source = new AdbDataSource(scrollController);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    source.close();
                }
            });
        }
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmAndroidLogViewer = new JFrame();
        frmAndroidLogViewer.setTitle("Android Log Viewer");
        frmAndroidLogViewer.setBounds(100, 100, 1000, 450);
        frmAndroidLogViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logElements = new DecoratingRendererTable();
        logElements.setFillsViewportHeight(true);
        logElements.setShowGrid(false);

        logElements.setModel(recordsModel);
        logElements.addDecorator(new PriorityColoredCellRenderer());
        logElements.setColumnModel(new LogRecordTableColumnModel(Configuration.ui.columns()));

        scrollPane = new JScrollPane(logElements);
        frmAndroidLogViewer.getContentPane().add(scrollPane, BorderLayout.CENTER);

        scrollController = new AutoScrollController(logElements, recordsModel);
        filterController = new FilterController(logElements, recordsModel);
        JPanel filterPanel = new FilterPanel(filterController);
        frmAndroidLogViewer.getContentPane().add(filterPanel, BorderLayout.SOUTH);
    }

}
