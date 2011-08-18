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
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.AdbDataSource;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.FileDataSourceFactory;

import com.android.ddmlib.AndroidDebugBridge;

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
                }
            }
        });

    }

    /**
     * Create the application.
     * 
     * @throws IOException
     */
    public Main(String[] args) throws IOException {
        Configuration.forceInit();

        if (args.length > 0) {
            source = FileDataSourceFactory.createDataSource(new File(args[0]));
            if (source == null) {
                JOptionPane.showMessageDialog(null, "Unrecognized file " + args[0], "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            source = AdbDataSource.createAdbDataSource();
        }

        initialize();
        source.setLogRecordListener(scrollController);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                source.close();
                if (AndroidDebugBridge.getBridge() != null) {
                    AndroidDebugBridge.terminate();
                }
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

        logElements.setTransferHandler(new LogRecordsTransferHandler());

        scrollPane = new JScrollPane(logElements);
        frmAndroidLogViewer.getContentPane().add(scrollPane, BorderLayout.CENTER);

        scrollController = new AutoScrollController(logElements, recordsModel);
        filterController = new FilterController(logElements, recordsModel);
        pinRecordsController = new PinRecordsController(logElements, recordsModel, source,
                filterController);
        popupMenuHandler = new LogRecordPopupMenuHandler(logElements, filterController,
                pinRecordsController);
        searchController = new SearchController(logElements, recordsModel);

        panel = new JPanel();
        frmAndroidLogViewer.getContentPane().add(panel, BorderLayout.SOUTH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        statusLabel = new JLabel();
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setVisible(false);
        panel.add(statusLabel);

        instantSearchTextField = new JTextField();
        panel.add(instantSearchTextField);
        instantSearchTextField.setColumns(10);
        instantSearchTextField.setVisible(false);

        JPanel filterPanel = new FilterPanel(filterController);
        panel.add(filterPanel);

        if (source.getAvailableBuffers() != null) {
            KindFilterMenu menu = new KindFilterMenu(source.getAvailableBuffers(), filterController);
            UiHelper.addPopupMenu(filterPanel, menu);
        }

        setupSearchButtons();
    }

    private void setupSearchButtons() {
        bindKeyGlobal(KEY_SHOW_SEARCH_FIELD, ACTION_SHOW_SEARCH_FIELD, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSearchField();
            }
        });

        bindKeyGlobal(KEY_FIND_NEXT, ACTION_FIND_NEXT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (searchController.isActive()) {
                    if (!searchController.searchNext()) {
                        showMessage(MESSAGE_NOT_FOUND);
                    }
                } else {
                    showSearchField();
                }
            }
        });

        bindKeyGlobal(KEY_FIND_PREV, ACTION_FIND_PREV, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (searchController.isActive()) {
                    if (!searchController.searchPrev()) {
                        showMessage(MESSAGE_NOT_FOUND);
                    }
                } else {
                    showSearchField();
                }
            }
        });

        bindKeyGlobal(KEY_HIDE, ACTION_HIDE_SEARCH_FIELD, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideSearchField();
                instantSearchTextField.setText(null);
                searchController.startSearch(null);
            }
        });

        bindKeyFocused(instantSearchTextField, KEY_HIDE_AND_START_SEARCH,
                ACTION_HIDE_AND_START_SEARCH, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        hideSearchField();
                        if (!searchController.startSearch(instantSearchTextField.getText())) {
                            showMessage(MESSAGE_NOT_FOUND);
                        }
                    }
                });

        bindKeyGlobal(KEY_SHOW_PINNED, ACTION_SHOW_PINNED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pinRecordsController.showWindow();
            }
        });
    }

    private static void bindKeyFocused(JComponent component, String key, String actionKey,
            Action action) {
        component.getInputMap().put(KeyStroke.getKeyStroke(key), actionKey);
        component.getActionMap().put(actionKey, action);
    }

    private void bindKeyGlobal(String key, String actionKey, Action action) {
        frmAndroidLogViewer.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(key), actionKey);
        frmAndroidLogViewer.getRootPane().getActionMap().put(actionKey, action);
    }

    private static final String ACTION_SHOW_SEARCH_FIELD = "show_search";
    private static final String ACTION_HIDE_SEARCH_FIELD = "hide_search";
    private static final String ACTION_HIDE_AND_START_SEARCH = "hide_and_start_search";
    private static final String ACTION_FIND_NEXT = "find_next";
    private static final String ACTION_FIND_PREV = "find_prev";

    private static final String KEY_HIDE_AND_START_SEARCH = "ENTER";
    private static final String KEY_HIDE = "ESCAPE";
    private static final String KEY_SHOW_SEARCH_FIELD = "control F";
    private static final String KEY_FIND_NEXT = "F3";
    private static final String KEY_FIND_PREV = "control F3";

    private void showSearchField() {
        instantSearchTextField.setVisible(true);
        instantSearchTextField.selectAll();
        instantSearchTextField.requestFocusInWindow();
        statusLabel.setVisible(false);
        panel.revalidate();
        panel.repaint();

    }

    private void hideSearchField() {
        instantSearchTextField.setVisible(false);
        panel.revalidate();
        panel.repaint();
    }

    private static final String ACTION_SHOW_PINNED = "show_pinned";
    private static final String KEY_SHOW_PINNED = "control P";
    private JLabel statusLabel;

    private static final int MESSAGE_DELAY = 2000;
    private static final String MESSAGE_NOT_FOUND = "Text not found";
    Timer hidingTimer = new Timer(MESSAGE_DELAY, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            statusLabel.setVisible(false);
        }
    });

    private void showMessage(String text) {
        statusLabel.setText(text);
        statusLabel.setVisible(true);

        hidingTimer.setRepeats(false);
        hidingTimer.start();
    }
}
