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

import name.mlopatkin.andlogview.bookmarks.BookmarkModel;
import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.device.AdbDevice;
import name.mlopatkin.andlogview.device.AdbDeviceList.DeviceChangeObserver;
import name.mlopatkin.andlogview.device.AdbManager;
import name.mlopatkin.andlogview.filters.MainFilterController;
import name.mlopatkin.andlogview.liblogcat.DataSource;
import name.mlopatkin.andlogview.liblogcat.LogRecord;
import name.mlopatkin.andlogview.liblogcat.LogRecordFormatter;
import name.mlopatkin.andlogview.liblogcat.RecordListener;
import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbDataSource;
import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbDeviceManager;
import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbException;
import name.mlopatkin.andlogview.liblogcat.file.FileDataSourceFactory;
import name.mlopatkin.andlogview.liblogcat.file.UnrecognizedFormatException;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.search.RequestCompilationException;
import name.mlopatkin.andlogview.ui.bookmarks.BookmarkController;
import name.mlopatkin.andlogview.ui.device.AdbServicesBridge;
import name.mlopatkin.andlogview.ui.device.DumpDevicePresenter;
import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableColumnModel;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.ui.logtable.LogTableHeaderPopupMenuController;
import name.mlopatkin.andlogview.ui.mainframe.BufferFilterMenu;
import name.mlopatkin.andlogview.ui.mainframe.DaggerMainFrameDependencies;
import name.mlopatkin.andlogview.ui.mainframe.ErrorDialogs;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameDependencies;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameModule;
import name.mlopatkin.andlogview.ui.preferences.ConfigurationDialogPresenter;
import name.mlopatkin.andlogview.ui.status.SearchStatusPresenter;
import name.mlopatkin.andlogview.ui.status.SourceStatusPresenter;
import name.mlopatkin.andlogview.ui.status.StatusPanel;
import name.mlopatkin.andlogview.utils.Optionals;
import name.mlopatkin.andlogview.utils.SystemUtils;
import name.mlopatkin.andlogview.widgets.DecoratingRendererTable;
import name.mlopatkin.andlogview.widgets.UiHelper;

import com.google.common.io.Files;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.TransferHandler;

public class MainFrame extends JFrame {
    private static final Logger logger = Logger.getLogger(MainFrame.class);

    private final DataSourceHolder sourceHolder;

    private LogRecordTableModel recordsModel;
    private TableScrollController scrollController;
    private SearchController searchController;

    private RecordListener<LogRecord> listener;
    private BookmarkController bookmarkController;
    private BookmarkModel bookmarkModel;

    private ProcessListFrame processListFrame;

    private BufferFilterMenu bufferMenu;
    private JTable logElements;
    private JPanel controlsPanel;
    private JTextField instantSearchTextField;

    @Inject
    SourceStatusPresenter sourceStatusPresenter;
    @Inject
    SearchStatusPresenter searchStatusPresenter;
    @Inject
    StatusPanel statusPanel;
    @Inject
    ErrorDialogs errorDialogs;
    @Inject
    AdbConfigurationPref adbConfigurationPref;
    @Inject
    ConfigurationDialogPresenter configurationDialogPresenter;
    @Inject
    @Named(AppExecutors.UI_EXECUTOR)
    Executor uiExecutor;

    @Inject
    AdbManager adbManager;
    @Inject
    AdbServicesBridge adbServicesBridge;

    @Nullable
    private DeviceChangeObserver pendingAttacher;

    private final MainFrameDependencies dependencies;
    private final CommandLine commandLine;

    public static class Factory implements Provider<MainFrame> {
        private final AppGlobals globals;
        private final CommandLine commandLine;

        @Inject
        public Factory(AppGlobals globals, CommandLine commandLine) {
            this.globals = globals;
            this.commandLine = commandLine;
        }

        @Override
        public MainFrame get() {
            return new MainFrame(globals, commandLine);
        }
    }

    @SuppressWarnings("NullAway")
    public MainFrame(AppGlobals globals, CommandLine commandLine) {
        dependencies = DaggerMainFrameDependencies.factory().create(new MainFrameModule(this), globals);
        sourceHolder = dependencies.getDataSourceHolder();

        this.commandLine = commandLine;

        dependencies.injectMainFrame(this);
        initialize();
        processListFrame = new ProcessListFrame(this);
    }

    public void setSource(DataSource newSource) {
        assert EventQueue.isDispatchThread();
        DataSource oldSource = sourceHolder.getDataSource();

        if (oldSource != null) {
            oldSource.close();
        }
        stopWaitingForDevice();
        sourceHolder.setDataSource(newSource);
        recordsModel.clear();
        bookmarkModel.clear();
        newSource.setLogRecordListener(listener);
        bufferMenu.setAvailableBuffers(newSource.getAvailableBuffers());
        updatingTimer.start();
        if (newSource != null && newSource.getPidToProcessConverter() != null) {
            acShowProcesses.setEnabled(true);
            processListFrame.setSource(newSource);
        } else {
            processListFrame.setSource(null);
            acShowProcesses.setEnabled(false);
        }

        Collection<Column> availableColumns = Column.getColumnsForFields(newSource.getAvailableFields());
        LogRecordTableColumnModel columns = dependencies.getColumnModelFactory().create(mapper, availableColumns);
        logElements.setColumnModel(columns);
        UiHelper.addPopupMenu(
                logElements.getTableHeader(), new LogTableHeaderPopupMenuController(columns).createMenu());
    }

    public void setSourceAsync(final DataSource newSource) {
        if (EventQueue.isDispatchThread()) {
            setSource(newSource);
        } else {
            EventQueue.invokeLater(() -> setSource(newSource));
        }
    }

    private PidToProcessMapper mapper = new PidToProcessMapper() {
        @Override
        public @Nullable String getProcessName(int pid) {
            DataSource source = sourceHolder.getDataSource();
            if (source == null) {
                return null;
            }
            Map<Integer, String> pidToProcessConverter = source.getPidToProcessConverter();
            if (pidToProcessConverter == null) {
                return null;
            }
            return pidToProcessConverter.get(pid);
        }
    };

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setTitle("AndLogView " + Main.getVersionString());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        bookmarkModel = dependencies.getBookmarkModel();
        bookmarkController = dependencies.getBookmarkController();
        recordsModel = dependencies.getLogModel();
        logElements = dependencies.getLogTable();
        logElements.setFillsViewportHeight(true);
        logElements.setShowGrid(false);

        LogRecordTableColumnModel columnModel =
                dependencies.getColumnModelFactory().create(mapper, Column.getSelectedColumns());
        logElements.setColumnModel(columnModel);
        UiHelper.addPopupMenu(
                logElements.getTableHeader(), new LogTableHeaderPopupMenuController(columnModel).createMenu());
        TransferHandler fileHandler = new FileTransferHandler(this);
        setTransferHandler(fileHandler);
        logElements.setTransferHandler(new LogRecordsTransferHandler(fileHandler));

        JScrollPane scrollPane = new JScrollPane(logElements);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        scrollController = new TableScrollController(logElements);

        // TODO(mlopatkin) Replace this cast with injection
        searchController = new SearchController((DecoratingRendererTable) logElements, recordsModel);
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
        controlsPanel.add(statusPanel.getPanel());
        sourceStatusPresenter.init();

        setupSearchButtons();
        setupMainMenu(dependencies.getMainFilterController());
        setPreferredSize(new Dimension(Configuration.ui.mainWindowWidth(), Configuration.ui.mainWindowHeight()));
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
        UiHelper.bindKeyGlobal(this, KEY_SHOW_SEARCH_FIELD, ACTION_SHOW_SEARCH_FIELD, e -> showSearchField());
        UiHelper.bindKeyGlobal(this, KEY_FIND_NEXT, ACTION_FIND_NEXT, e -> {
            if (searchController.isActive()) {
                if (searchController.searchNext()) {
                    searchStatusPresenter.reset();
                } else {
                    searchStatusPresenter.showNotFoundMessage();
                }
            } else {
                showSearchField();
            }
        });

        UiHelper.bindKeyGlobal(this, KEY_FIND_PREV, ACTION_FIND_PREV, e -> {
            if (searchController.isActive()) {
                if (searchController.searchPrev()) {
                    searchStatusPresenter.reset();
                } else {
                    searchStatusPresenter.showNotFoundMessage();
                }
            } else {
                showSearchField();
            }
        });

        UiHelper.bindKeyGlobal(this, KEY_HIDE, ACTION_HIDE_SEARCH_FIELD, e -> {
            hideSearchField();
            instantSearchTextField.setText(null);
            try {
                searchController.startSearch(null);
            } catch (RequestCompilationException e1) {
                logger.error("Unexpected exception", e1);
            }
        });

        UiHelper.bindKeyFocused(
                instantSearchTextField, KEY_HIDE_AND_START_SEARCH, ACTION_HIDE_AND_START_SEARCH, e -> {
                    hideSearchField();
                    String request = instantSearchTextField.getText();
                    try {
                        if (!searchController.startSearch(request)) {
                            logElements.requestFocusInWindow();
                            searchStatusPresenter.showNotFoundMessage();
                        }
                    } catch (RequestCompilationException e1) {
                        ErrorDialogsHelper.showError(
                                MainFrame.this, "%s isn't a valid search expression: %s", request, e1.getMessage());
                    }
                });
    }

    private static final String ACTION_SHOW_SEARCH_FIELD = "show_search";
    private static final String ACTION_HIDE_SEARCH_FIELD = "hide_search";
    private static final String ACTION_HIDE_AND_START_SEARCH = "hide_and_start_search";
    private static final String ACTION_FIND_NEXT = "find_next";
    private static final String ACTION_FIND_PREV = "find_prev";

    private static final KeyStroke KEY_HIDE_AND_START_SEARCH = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    private static final KeyStroke KEY_HIDE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    private static final KeyStroke KEY_SHOW_SEARCH_FIELD = UiHelper.createPlatformKeystroke(KeyEvent.VK_F);
    private static final KeyStroke KEY_FIND_NEXT =
            SystemUtils.IS_OS_MACOS
                    ? UiHelper.createPlatformKeystroke(KeyEvent.VK_G)
                    : KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
    private static final KeyStroke KEY_FIND_PREV =
            SystemUtils.IS_OS_MACOS
                    ? UiHelper.createPlatformKeystroke(KeyEvent.VK_G, InputEvent.SHIFT_DOWN_MASK)
                    : KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_DOWN_MASK);

    private void showSearchField() {
        scrollController.notifyBeforeInsert();
        instantSearchTextField.setVisible(true);
        instantSearchTextField.selectAll();
        instantSearchTextField.requestFocusInWindow();
        searchStatusPresenter.reset();
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

    Timer updatingTimer = new Timer(2000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            sourceStatusPresenter.updateSourceStatus();
        }
    });

    public void reset() {
        recordsModel.clear();
        DataSource source = sourceHolder.getDataSource();
        if (source != null && !source.reset()) {
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
        if (commandLine.isDebug()) {
            mnAdb.addSeparator();
            mnAdb.add(acDumpDevice);
        }
        mainMenu.add(mnAdb);

        JMenu mnFilters = new JMenu("Buffers");
        bufferMenu = new BufferFilterMenu(mnFilters, mainFilterController);
        mainMenu.add(mnFilters);

        setJMenuBar(mainMenu);
    }

    private final Action acOpenFile = new AbstractAction("Open...") {
        {
            putValue(ACCELERATOR_KEY, UiHelper.createPlatformKeystroke(KeyEvent.VK_O));
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            dependencies.getFileDialog().selectFileToOpen().ifPresent(file -> {
                try {
                    DataSource source = FileDataSourceFactory.createDataSource(file);
                    setSource(source);
                } catch (UnrecognizedFormatException e) {
                    logger.error("Unrecognized source file " + file, e);
                    ErrorDialogsHelper.showError(MainFrame.this, "Unrecognized file format for " + file);
                } catch (IOException e) {
                    logger.error("IO Exception while reading " + file, e);
                    ErrorDialogsHelper.showError(MainFrame.this, "Cannot read " + file);
                }
            });
        }
    };

    private final Action acConnectToDevice = new AbstractAction("Connect to device...") {
        @Override
        public void actionPerformed(ActionEvent e) {
            Optionals.ifPresentOrElse(adbServicesBridge.getAdbServices(),
                    adbServices -> {
                        AdbDeviceManager adbDeviceManager = adbServices.getDeviceManager();
                        adbServices.getSelectDeviceDialogFactory().show((dialog, selectedDevice) -> {
                            if (selectedDevice != null) {
                                DeviceDisconnectedHandler.startWatching(MainFrame.this, adbConfigurationPref,
                                        adbDeviceManager, selectedDevice);
                                setSource(new AdbDataSource(adbDeviceManager, selectedDevice));
                            }
                        });
                    },
                    MainFrame.this::disableAdbCommandsAsync);
        }
    };

    private final Action acResetLogs = new AbstractAction("Reset logs") {
        {
            putValue(ACCELERATOR_KEY, UiHelper.createPlatformKeystroke(KeyEvent.VK_R));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            reset();
        }
    };

    private final Action acChangeConfiguration = new AbstractAction("Configuration...") {
        @Override
        public void actionPerformed(ActionEvent e) {
            configurationDialogPresenter.openDialog();
        }
    };

    private final Action acDumpDevice = new AbstractAction("Prepare device dump...") {
        @Override
        public void actionPerformed(ActionEvent e) {
            Optionals.ifPresentOrElse(adbServicesBridge.getDumpDevicePresenter(),
                    DumpDevicePresenter::selectDeviceAndDump,
                    MainFrame.this::disableAdbCommandsAsync);
        }
    };

    public void tryToConnectToFirstAvailableDevice() {
        Optionals.ifPresentOrElse(adbServicesBridge.getAdbDeviceManager(), adbDeviceManager -> {
            AdbDevice device = adbDeviceManager.getDefaultDevice();
            if (device != null) {
                DeviceDisconnectedHandler.startWatching(this, adbConfigurationPref, adbDeviceManager, device);
                setSourceAsync(new AdbDataSource(adbDeviceManager, device));
            } else {
                waitForDevice();
            }
        }, this::disableAdbCommandsAsync);
    }

    /**
     * Wait for device to connect.
     */
    public void waitForDevice() {
        synchronized (this) {
            isWaitingForDevice = true;
        }
        DeviceChangeObserver attacher = pendingAttacher = new DeviceChangeObserver() {
            @Override
            public void onDeviceConnected(AdbDevice device) {
                if (device.isOnline()) {
                    connectDevicePending(device);
                }
            }

            @Override
            public void onDeviceChanged(AdbDevice device) {
                if (device.isOnline()) {
                    connectDevicePending(device);
                }
            }
        };

        Optionals.ifPresentOrElse(adbServicesBridge.getAdbServices(), adbServices -> {
            adbServices.getDeviceList().asObservable().addObserver(attacher);
            AdbDevice device = adbServices.getDeviceManager().getDefaultDevice();
            if (device != null) {
                connectDevicePending(device);
            }
        }, this::disableAdbCommandsAsync);
    }

    private volatile boolean isWaitingForDevice;

    private synchronized void connectDevicePending(AdbDevice device) {
        if (!isWaitingForDevice) {
            return;
        }
        isWaitingForDevice = false;
        stopWaitingForDevice();
        Optionals.ifPresentOrElse(adbServicesBridge.getAdbDeviceManager(), adbDeviceManager -> {
            DeviceDisconnectedHandler.startWatching(this, adbConfigurationPref, adbDeviceManager, device);
            setSourceAsync(new AdbDataSource(adbDeviceManager, device));
        }, this::disableAdbCommandsAsync);
    }

    private void stopWaitingForDevice() {
        DeviceChangeObserver attacher = pendingAttacher;
        if (attacher != null) {
            Optionals.ifPresentOrElse(adbServicesBridge.getAdbDeviceList(), adbDeviceList -> {
                adbDeviceList.asObservable().removeObserver(attacher);
            }, this::disableAdbCommandsAsync);
        }
        pendingAttacher = null;
    }

    private Action acSaveToFile = new AbstractAction("Save...") {
        {
            putValue(ACCELERATOR_KEY, UiHelper.createPlatformKeystroke(KeyEvent.VK_S));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            dependencies.getFileDialog().selectFileToSave().ifPresent(MainFrame.this::saveTableToFile);
        }
    };

    private AbstractAction acShowBookmarks = new AbstractAction("Show bookmarks") {
        {
            putValue(ACCELERATOR_KEY, UiHelper.createPlatformKeystroke(KeyEvent.VK_P));
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
            DataSource source = sourceHolder.getDataSource();
            assert source != null;
            assert source.getPidToProcessConverter() != null;
            processListFrame.setVisible(true);
        }
    };

    private void saveTableToFile(File file) {
        try {
            try (PrintWriter out = new PrintWriter(Files.newWriter(file, StandardCharsets.UTF_8))) {
                final int rowCount = logElements.getRowCount();
                for (int i = 0; i < rowCount; ++i) {
                    LogRecord record = recordsModel.getRowData(logElements.convertRowIndexToModel(i));
                    out.println(LogRecordFormatter.formatAppropriate(record));
                }
            }
        } catch (FileNotFoundException e) {
            logger.warn("Unexpected exception", e);
        }
    }

    void disableAdbCommandsAsync() {
        EventQueue.invokeLater(() -> acConnectToDevice.setEnabled(false));
    }

    void setRecentDir(File dir) {
        dependencies.getLastUsedDir().set(dir);
    }

    public boolean tryInitAdbBridge() {
        try {
            adbManager.setAdbLocation(adbConfigurationPref);
            adbManager.startServer();
            return true;
        } catch (AdbException e) {
            logger.warn("Cannot start in ADB mode", e);
            disableAdbCommandsAsync();
            errorDialogs.showAdbNotFoundError();
        }
        return false;
    }
}
