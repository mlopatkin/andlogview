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
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.AdbDataSource;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.DumpstateFileDataSource;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    private JFrame frmAndroidLogViewer;
    private DecoratingRendererTable logElements;
    private JScrollPane scrollPane;

    private LogRecordTableModel recordsModel = new LogRecordTableModel();
    private AutoScrollController scrollController;
    private FilterController filterController;
    private SearchController searchController;
    private LogRecordPopupMenuHandler popupMenuHandler;
    private PinRecordsController pinRecordsController;

    private DataSource source;
    private JPanel panel;
    private JTextField instantSearchTextField;

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
        if (args.length > 0) {
            source = new DumpstateFileDataSource(new File(args[0]));
        } else {
            source = new AdbDataSource();
        }
        initialize();
        source.setLogRecordListener(scrollController);

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

        logElements = new DecoratingRendererTable();
        logElements.setFillsViewportHeight(true);
        logElements.setShowGrid(false);

        logElements.setModel(recordsModel);
        logElements.addDecorator(new PriorityColoredCellRenderer());
        logElements.setColumnModel(new LogRecordTableColumnModel(Configuration.ui.columns(), source
                .getPidToProcessConverter()));

        scrollPane = new JScrollPane(logElements);
        frmAndroidLogViewer.getContentPane().add(scrollPane, BorderLayout.CENTER);

        scrollController = new AutoScrollController(logElements, recordsModel);
        filterController = new FilterController(logElements, recordsModel);
        pinRecordsController = new PinRecordsController(recordsModel, source);
        popupMenuHandler = new LogRecordPopupMenuHandler(logElements, filterController,
                pinRecordsController);

        panel = new JPanel();
        frmAndroidLogViewer.getContentPane().add(panel, BorderLayout.SOUTH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        instantSearchTextField = new JTextField();
        panel.add(instantSearchTextField);
        instantSearchTextField.setColumns(10);
        instantSearchTextField.setVisible(false);

        JPanel filterPanel = new FilterPanel(filterController);
        panel.add(filterPanel);

        setupSearchButtons();
    }

    private void setupSearchButtons() {
        frmAndroidLogViewer.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KEY_SHOW_SEARCH_FIELD), ACTION_SHOW_SEARCH_FIELD);
        frmAndroidLogViewer.getRootPane().getActionMap().put(ACTION_SHOW_SEARCH_FIELD,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showSearchField();
                    }
                });

        frmAndroidLogViewer.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KEY_FIND_NEXT), ACTION_FIND_NEXT);
        frmAndroidLogViewer.getRootPane().getActionMap().put(ACTION_FIND_NEXT,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        searchController.continueSearch();
                    }
                });

        instantSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(KEY_HIDE_AND_START_SEARCH),
                ACTION_HIDE_AND_START_SEARCH);
        instantSearchTextField.getActionMap().put(ACTION_HIDE_AND_START_SEARCH,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        hideSearchField();
                        searchController.startSearch(instantSearchTextField.getText());
                    }
                });
        frmAndroidLogViewer.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KEY_HIDE), ACTION_HIDE_SEARCH_FIELD);
        frmAndroidLogViewer.getRootPane().getActionMap().put(ACTION_HIDE_SEARCH_FIELD,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        hideSearchField();
                        instantSearchTextField.setText(null);
                    }
                });

    }

    private static final String ACTION_SHOW_SEARCH_FIELD = "show_search";
    private static final String ACTION_HIDE_SEARCH_FIELD = "hide_search";
    private static final String ACTION_HIDE_AND_START_SEARCH = "hide_and_start_search";
    private static final String ACTION_FIND_NEXT = "find_next";
    private static final String KEY_HIDE_AND_START_SEARCH = "ENTER";
    private static final String KEY_HIDE = "ESCAPE";
    private static final char KEY_SHOW_SEARCH_FIELD = '/';
    private static final String KEY_FIND_NEXT = "F3";

    private void showSearchField() {
        instantSearchTextField.setVisible(true);
        instantSearchTextField.requestFocusInWindow();
        panel.revalidate();
        panel.repaint();

    }

    private void hideSearchField() {
        instantSearchTextField.setVisible(false);
        panel.revalidate();
        panel.repaint();
    }
}
