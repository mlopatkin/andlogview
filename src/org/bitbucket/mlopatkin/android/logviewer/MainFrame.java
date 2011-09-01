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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDataSource;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDeviceManager;
import org.bitbucket.mlopatkin.android.liblogcat.file.FileDataSourceFactory;
import org.bitbucket.mlopatkin.android.liblogcat.file.UnrecognizedFormatException;
import org.bitbucket.mlopatkin.android.logviewer.SelectDeviceDialog.DialogResultReceiver;
import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingRendererTable;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;

public class MainFrame extends JFrame implements DialogResultReceiver {
    private static final Logger logger = Logger.getLogger(MainFrame.class);

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
     * @wbp.parser.entryPoint
     */
    public MainFrame() {
        super();
        initialize();
    }

    public void setSource(DataSource newSource) {
        if (source != null) {
            source.close();
        }
        source = newSource;
        recordsModel.clear();
        pinRecordsController.clear();
        source.setLogRecordListener(scrollController);
        bufferMenu.setAvailableBuffers(source.getAvailableBuffers());
    }

    private PidToProcessMapper mapper = new PidToProcessMapper() {

        @Override
        public String getProcessName(int pid) {
            if (source != null && source.getPidToProcessConverter() != null) {
                return source.getPidToProcessConverter().getProcessName(pid);
            } else {
                return null;
            }
        }
    };

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setTitle("Android Log Viewer");
        setBounds(100, 100, 1000, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logElements = new DecoratingRendererTable();
        logElements.setFillsViewportHeight(true);
        logElements.setShowGrid(false);

        logElements.setModel(recordsModel);
        logElements.addDecorator(new PriorityColoredCellRenderer());
        logElements
                .setColumnModel(new LogRecordTableColumnModel(Configuration.ui.columns(), mapper));

        TransferHandler fileHandler = new FileTransferHandler(this);
        setTransferHandler(fileHandler);
        logElements.setTransferHandler(new LogRecordsTransferHandler(fileHandler));

        scrollPane = new JScrollPane(logElements);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        scrollController = new AutoScrollController(logElements, recordsModel);
        filterController = new FilterController(logElements, recordsModel);
        pinRecordsController = new PinRecordsController(logElements, recordsModel, mapper,
                filterController);
        popupMenuHandler = new LogRecordPopupMenuHandler(this, logElements, filterController,
                pinRecordsController);
        searchController = new SearchController(logElements, recordsModel);

        panel = new JPanel();
        getContentPane().add(panel, BorderLayout.SOUTH);
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

        setupSearchButtons();
        setupMainMenu();
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

        UiHelper.bindKeyFocused(instantSearchTextField, KEY_HIDE_AND_START_SEARCH,
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

    private void bindKeyGlobal(String key, String actionKey, Action action) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(key), actionKey);
        getRootPane().getActionMap().put(actionKey, action);
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

    public void reset() {
        recordsModel.clear();
        source.reset();
    }

    private BufferFilterMenu bufferMenu;

    private void setupMainMenu() {
        JMenuBar mainMenu = new JMenuBar();

        JMenu mnFile = new JMenu("File");
        mnFile.add(acOpenFile);
        mainMenu.add(mnFile);

        JMenu mnAdb = new JMenu("ADB");
        mnAdb.add(acConnectToDevice);
        mnAdb.addSeparator();
        mnAdb.add(acResetLogs);
        mainMenu.add(mnAdb);

        JMenu mnFilters = new JMenu("Buffers");
        bufferMenu = new BufferFilterMenu(mnFilters, filterController);
        mainMenu.add(mnFilters);

        setJMenuBar(mainMenu);
    }

    private Action acOpenFile = new AbstractAction("Open...") {

        @Override
        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(MainFrame.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    DataSource source = FileDataSourceFactory.createDataSource(file);
                    setSource(source);
                } catch (UnrecognizedFormatException e) {
                    logger.error("Unrecognized source file " + file, e);
                    ErrorDialogsHelper.showError("Unrecognized file format for " + file);
                } catch (IOException e) {
                    logger.error("IO Exception while reading " + file, e);
                    ErrorDialogsHelper.showError("Cannot read " + file);
                }

            }
        }
    };

    private Action acConnectToDevice = new AbstractAction("Connect to device...") {

        @Override
        public void actionPerformed(ActionEvent e) {
            SelectDeviceDialog.showSelectDeviceDialog(MainFrame.this);
        }
    };

    @Override
    public void onDialogResult(SelectDeviceDialog dialog, IDevice selectedDevice) {
        if (selectedDevice != null) {
            DeviceDisconnectedNotifier.startWatching(selectedDevice);
            setSource(new AdbDataSource(selectedDevice));
        }
    }

    private Action acResetLogs = new AbstractAction("Reset logs") {

        @Override
        public void actionPerformed(ActionEvent e) {
            reset();
        }
    };

    /**
     * Wait for device to connect.
     */
    public void waitForDevice() {
        IDeviceChangeListener pendingAttacher = new AdbDeviceManager.AbstractDeviceListener() {
            @Override
            public void deviceConnected(IDevice device) {
                DeviceDisconnectedNotifier.startWatching(device);
                setSource(new AdbDataSource(device));
                AdbDeviceManager.removeDeviceChangeListener(this);
            }
        };
        AdbDeviceManager.addDeviceChangeListener(pendingAttacher);
    }
}
