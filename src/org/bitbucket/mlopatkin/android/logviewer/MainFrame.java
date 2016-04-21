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

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordFormatter;
import org.bitbucket.mlopatkin.android.liblogcat.RecordListener;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDataSource;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDeviceManager;
import org.bitbucket.mlopatkin.android.liblogcat.file.FileDataSourceFactory;
import org.bitbucket.mlopatkin.android.liblogcat.file.UnrecognizedFormatException;
import org.bitbucket.mlopatkin.android.logviewer.SelectDeviceDialog.DialogResultReceiver;
import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilterStorage;
import org.bitbucket.mlopatkin.android.logviewer.filters.MainFilterController;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;
import org.bitbucket.mlopatkin.android.logviewer.ui.bookmarks.BookmarkController;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableColumnModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogTable;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogTableHeaderPopupMenuController;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.BufferFilterMenu;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.DaggerMainFrameDependencies;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.MainFrameDependencies;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.MainFrameModule;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.border.EtchedBorder;

public class MainFrame extends JFrame implements DialogResultReceiver {
    private static final Logger logger = Logger.getLogger(MainFrame.class);
    private final FilterStorage storage;

    private LogRecordTableModel recordsModel;
    private TableScrollController scrollController;
    private SearchController searchController;
    private RecordListener<LogRecord> listener;

    private BookmarkController bookmarkController;
    private BookmarkModel bookmarkModel;
    private ProcessListFrame processListFrame;

    private DataSource source;

    private BufferFilterMenu bufferMenu;
    private LogTable logElements;
    private JPanel controlsPanel;
    private JTextField instantSearchTextField;

    private JPanel statusPanel;
    private JLabel searchStatusLabel;
    private JLabel sourceStatusLabel;

    public MainFrame(FilterStorage storage) {
        super();
        this.storage = storage;
        initialize();
        processListFrame = new ProcessListFrame(this);
    }

    public void setSource(DataSource newSource) {
        assert EventQueue.isDispatchThread();
        if (source != null) {
            source.close();
        }
        stopWaitingForDevice();
        source = newSource;
        recordsModel.clear();
        bookmarkModel.clear();
        source.setLogRecordListener(listener);
        bufferMenu.setAvailableBuffers(source.getAvailableBuffers());
        showSourceMessage(source.toString());
        updatingTimer.start();
        if (source != null && source.getPidToProcessConverter() != null) {
            acShowProcesses.setEnabled(true);
            processListFrame.setSource(source);
        } else {
            processListFrame.setSource(null);
            acShowProcesses.setEnabled(false);
        }

        LogRecordTableColumnModel columns = LogRecordTableColumnModel.create(
                mapper, Column.getFilteredSelectedColumns(source.getAvailableFields()));
        logElements.setColumnModel(columns);
    }

    public void setSourceAsync(final DataSource newSource) {
        if (EventQueue.isDispatchThread()) {
            setSource(newSource);
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setSource(newSource);
                }
            });
        }
    }

    private PidToProcessMapper mapper = new PidToProcessMapper() {

        @Override
        public String getProcessName(int pid) {
            if (source != null && source.getPidToProcessConverter() != null) {
                return source.getPidToProcessConverter().get(pid);
            } else {
                return null;
            }
        }
    };

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setTitle("Android Log Viewer " + Main.APP_VERSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MainFrameDependencies dependencies =
                DaggerMainFrameDependencies.builder().mainFrameModule(new MainFrameModule(this, storage)).build();
        bookmarkModel = dependencies.getBookmarkModel();
        bookmarkController = dependencies.getBookmarkController();
        recordsModel = dependencies.getLogModel();
        logElements = dependencies.getLogTable();
        logElements.setFillsViewportHeight(true);
        logElements.setShowGrid(false);

        LogRecordTableColumnModel columnModel = LogRecordTableColumnModel.create(mapper, Column.getSelectedColumns());
        logElements.setColumnModel(columnModel);
        UiHelper.addPopupMenu(logElements.getTableHeader(), new LogTableHeaderPopupMenuController(columnModel).createMenu());
        TransferHandler fileHandler = new FileTransferHandler(this);
        setTransferHandler(fileHandler);
        logElements.setTransferHandler(new LogRecordsTransferHandler(fileHandler));

        logElements.addDecorator(dependencies.getBookmarkHighlighter());

        dependencies.getPopupMenuHandlerFactory().attachMenuHandle(logElements);

        JScrollPane scrollPane = new JScrollPane(logElements);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        scrollController = new TableScrollController(logElements);

        searchController = new SearchController(logElements, recordsModel);
        listener = new BufferedListener<>(recordsModel, scrollController);

        controlsPanel = new JPanel();
        getContentPane().add(controlsPanel, BorderLayout.SOUTH);
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.PAGE_AXIS));

        instantSearchTextField = new JTextField();
        controlsPanel.add(instantSearchTextField);
        instantSearchTextField.setColumns(10);
        instantSearchTextField.setVisible(false);

        JComponent filterPanel = dependencies.getFilterPanel();
        controlsPanel.add(filterPanel);

        statusPanel = new JPanel();
        statusPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        controlsPanel.add(statusPanel);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));

        searchStatusLabel = new JLabel();
        searchStatusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(searchStatusLabel);

        Component horizontalGlue = Box.createHorizontalGlue();
        statusPanel.add(horizontalGlue);

        sourceStatusLabel = new JLabel();
        statusPanel.add(sourceStatusLabel);

        Component rigidArea = Box.createRigidArea(new Dimension(0, 16));
        statusPanel.add(rigidArea);

        setupSearchButtons();
        setupMainMenu(dependencies.getMainFilterController());
        setPreferredSize(new Dimension(Configuration.ui.mainWindowWidth(),
                Configuration.ui.mainWindowHeight()));
        if (Configuration.ui.mainWindowPosition() != null) {
            setLocation(Configuration.ui.mainWindowPosition());
        } else {
            setLocationByPlatform(true);
        }
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Configuration.ui.mainWindowHeight(getHeight());
                Configuration.ui.mainWindowWidth(getWidth());
                Configuration.ui.mainWindowPosition(getLocation());
            }
        });
        pack();
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
                        showSearchMessage(MESSAGE_NOT_FOUND);
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
                        showSearchMessage(MESSAGE_NOT_FOUND);
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
                try {
                    searchController.startSearch(null);
                } catch (RequestCompilationException e1) {
                    logger.error("Unexpected exception", e1);
                }
            }
        });

        UiHelper.bindKeyFocused(instantSearchTextField, KEY_HIDE_AND_START_SEARCH,
                ACTION_HIDE_AND_START_SEARCH, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        hideSearchField();
                        String request = instantSearchTextField.getText();
                        try {
                            if (!searchController.startSearch(request)) {
                                logElements.requestFocusInWindow();
                                showSearchMessage(MESSAGE_NOT_FOUND);
                            }
                        } catch (RequestCompilationException e1) {
                            ErrorDialogsHelper.showError(MainFrame.this,
                                    "%s isn't a valid search expression: %s", request,
                                    e1.getMessage());
                        }
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
    private static final String KEY_FIND_PREV = "shift F3";

    private void showSearchField() {
        scrollController.notifyBeforeInsert();
        instantSearchTextField.setVisible(true);
        instantSearchTextField.selectAll();
        instantSearchTextField.requestFocusInWindow();
        searchStatusLabel.setVisible(false);
        controlsPanel.revalidate();
        controlsPanel.repaint();
        scrollController.scrollIfNeeded();
    }

    private void hideSearchField() {
        scrollController.notifyBeforeInsert();
        instantSearchTextField.setVisible(false);
        controlsPanel.revalidate();
        controlsPanel.repaint();
        scrollController.scrollIfNeeded();
    }

    private static final int MESSAGE_DELAY = 2000;
    private static final String MESSAGE_NOT_FOUND = "Text not found";
    Timer hidingTimer = new Timer(MESSAGE_DELAY, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            searchStatusLabel.setVisible(false);
        }
    });

    Timer updatingTimer = new Timer(MESSAGE_DELAY, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (source != null) {
                showSourceMessage(source.toString());
            }
        }
    });

    private void showSearchMessage(String text) {
        searchStatusLabel.setText(text);
        searchStatusLabel.setVisible(true);
        hidingTimer.setRepeats(false);
        hidingTimer.start();
    }

    private void showSourceMessage(String text) {
        sourceStatusLabel.setText(text);
        statusPanel.revalidate();
        statusPanel.repaint();
    }

    public void reset() {
        recordsModel.clear();
        if (!source.reset()) {
            bookmarkModel.clear();
        }
    }

    private void setupMainMenu(MainFilterController mainFilterController) {
        JMenuBar mainMenu = new JMenuBar();

        JMenu mnFile = new JMenu("File");
        mnFile.add(acOpenFile);
        mnFile.add(acSaveToFile);
        mainMenu.add(mnFile);

        JMenu mnView = new JMenu("View");
        mnView.add(acShowBookmarks);
        mnView.add(acShowProcesses);
        mainMenu.add(mnView);

        JMenu mnAdb = new JMenu("ADB");
        mnAdb.add(acConnectToDevice);
        mnAdb.addSeparator();
        mnAdb.add(acResetLogs);
        mnAdb.add(acChangeConfiguration);
        mainMenu.add(mnAdb);

        JMenu mnFilters = new JMenu("Buffers");
        bufferMenu = new BufferFilterMenu(mnFilters, mainFilterController);
        mainMenu.add(mnFilters);

        setJMenuBar(mainMenu);
    }

    private File recentDir = null;

    private Action acOpenFile = new AbstractAction("Open...") {

        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser(recentDir);
            int result = fileChooser.showOpenDialog(MainFrame.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                setRecentDir(file.getAbsoluteFile().getParentFile());
                try {
                    DataSource source = FileDataSourceFactory.createDataSource(file);
                    setSource(source);
                } catch (UnrecognizedFormatException e) {
                    logger.error("Unrecognized source file " + file, e);
                    ErrorDialogsHelper.showError(MainFrame.this, "Unrecognized file format for "
                            + file);
                } catch (IOException e) {
                    logger.error("IO Exception while reading " + file, e);
                    ErrorDialogsHelper.showError(MainFrame.this, "Cannot read " + file);
                }

            }
        }
    };

    private Action acConnectToDevice = new AbstractAction("Connect to device...") {

        @Override
        public void actionPerformed(ActionEvent e) {
            SelectDeviceDialog.showSelectDeviceDialog(MainFrame.this, MainFrame.this);
        }
    };

    @Override
    public void onDialogResult(SelectDeviceDialog dialog, IDevice selectedDevice) {
        if (selectedDevice != null) {
            DeviceDisconnectedHandler.startWatching(this, selectedDevice);
            setSource(new AdbDataSource(selectedDevice));
        }
    }

    private Action acResetLogs = new AbstractAction("Reset logs") {
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control R"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            reset();
        }
    };

    private Action acChangeConfiguration = new AbstractAction("Configuration...") {

        @Override
        public void actionPerformed(ActionEvent e) {
            ConfigurationDialog.showConfigurationDialog(MainFrame.this);
        }
    };
    private IDeviceChangeListener pendingAttacher;

    /**
     * Wait for device to connect.
     */
    public void waitForDevice() {
        synchronized (this) {
            isWaitingForDevice = true;
        }
        pendingAttacher = new AdbDeviceManager.AbstractDeviceListener() {
            @Override
            public void deviceConnected(final IDevice device) {
                if (device.isOnline()) {
                    connectDevicePending(device);
                }
            }

            @Override
            public void deviceChanged(IDevice device, int changeMask) {
                if ((changeMask & IDevice.CHANGE_STATE) != 0 && device.isOnline()) {
                    connectDevicePending(device);
                }
            }

        };
        AdbDeviceManager.addDeviceChangeListener(pendingAttacher);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                showSourceMessage("Waiting for device...");
            }
        });
        IDevice device = AdbDeviceManager.getDefaultDevice();
        if (device != null) {
            connectDevicePending(device);
        }
    }

    private volatile boolean isWaitingForDevice;

    private synchronized void connectDevicePending(IDevice device) {
        if (!isWaitingForDevice) {
            return;
        }
        isWaitingForDevice = false;
        stopWaitingForDevice();
        DeviceDisconnectedHandler.startWatching(this, device);
        setSourceAsync(new AdbDataSource(device));
    }

    private void stopWaitingForDevice() {
        if (pendingAttacher != null) {
            AdbDeviceManager.removeDeviceChangeListener(pendingAttacher);
            pendingAttacher = null;
        }
    }

    private Action acSaveToFile = new AbstractAction("Save...") {

        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            JFileChooser fileChooser = new JFileChooser(recentDir);
            int result = fileChooser.showSaveDialog(MainFrame.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                setRecentDir(file.getAbsoluteFile().getParentFile());
                if (file.exists()) {
                    result = JOptionPane.showConfirmDialog(MainFrame.this, "File " + file
                            + " already exists, overwrite?");
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                saveTableToFile(file);
            }
        }
    };

    private AbstractAction acShowBookmarks = new AbstractAction("Show bookmarks") {
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control P"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            bookmarkController.showWindow();
        }
    };

    private AbstractAction acShowProcesses = new AbstractAction("Show processes") {
        {
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            assert source != null;
            assert source.getPidToProcessConverter() != null;
            processListFrame.setVisible(true);
        }
    };

    private void saveTableToFile(File file) {
        try {
            PrintWriter out = new PrintWriter(file);
            try {
                final int rowCount = logElements.getRowCount();
                for (int i = 0; i < rowCount; ++i) {
                    LogRecord record = recordsModel.getRowData(logElements
                            .convertRowIndexToModel(i));
                    out.println(LogRecordFormatter.formatAppropriate(record));
                }
            } finally {
                out.close();
            }
        } catch (FileNotFoundException e) {
            logger.warn("Unexpected exception", e);
        }
    }

    void disableAdbCommandsAsync() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                acConnectToDevice.setEnabled(false);
            }
        });
    }

    void setRecentDir(File dir) {
        recentDir = dir;
    }
}
