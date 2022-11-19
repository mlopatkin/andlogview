/*
 * Copyright 2011 Mikhail Lopatkin and the Andlogview authors
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

import name.mlopatkin.andlogview.base.concurrent.SequentialExecutor;
import name.mlopatkin.andlogview.bookmarks.BookmarkModel;
import name.mlopatkin.andlogview.device.AdbDeviceList;
import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.device.AdbManager;
import name.mlopatkin.andlogview.device.Device;
import name.mlopatkin.andlogview.device.DeviceChangeObserver;
import name.mlopatkin.andlogview.filters.MainFilterController;
import name.mlopatkin.andlogview.liblogcat.LogRecordFormatter;
import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.logmodel.LogModel;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.preferences.WindowsPositionsPref;
import name.mlopatkin.andlogview.search.logrecord.RowSearchStrategy;
import name.mlopatkin.andlogview.thirdparty.systemutils.SystemUtils;
import name.mlopatkin.andlogview.ui.FileDialog;
import name.mlopatkin.andlogview.ui.FrameDimensions;
import name.mlopatkin.andlogview.ui.FrameLocation;
import name.mlopatkin.andlogview.ui.bookmarks.BookmarkController;
import name.mlopatkin.andlogview.ui.device.AdbServicesBridge;
import name.mlopatkin.andlogview.ui.device.DumpDevicePresenter;
import name.mlopatkin.andlogview.ui.file.FileOpener;
import name.mlopatkin.andlogview.ui.filterpanel.FilterPanel;
import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableColumnModel;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.ui.logtable.LogTableHeaderPopupMenuController;
import name.mlopatkin.andlogview.ui.mainframe.BufferFilterMenu;
import name.mlopatkin.andlogview.ui.mainframe.DaggerMainFrameDependencies;
import name.mlopatkin.andlogview.ui.mainframe.ErrorDialogs;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameDependencies;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameModule;
import name.mlopatkin.andlogview.ui.mainframe.TableColumnModelFactory;
import name.mlopatkin.andlogview.ui.mainframe.search.MainFrameSearchPromptView;
import name.mlopatkin.andlogview.ui.mainframe.search.MainFrameSearchUi;
import name.mlopatkin.andlogview.ui.preferences.ConfigurationDialogPresenter;
import name.mlopatkin.andlogview.ui.processes.ProcessListFrame;
import name.mlopatkin.andlogview.ui.search.SearchPresenter;
import name.mlopatkin.andlogview.ui.search.logtable.TablePosition;
import name.mlopatkin.andlogview.ui.status.SearchStatusPresenter;
import name.mlopatkin.andlogview.ui.status.SourceStatusPresenter;
import name.mlopatkin.andlogview.ui.status.StatusPanel;
import name.mlopatkin.andlogview.utils.Optionals;
import name.mlopatkin.andlogview.widgets.UiHelper;

import com.google.common.io.Files;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.swing.Action;
import javax.swing.BoxLayout;
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

public class MainFrame extends JFrame implements MainFrameSearchUi {
    private static final Logger logger = Logger.getLogger(MainFrame.class);

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

    @Inject
    DataSourceHolder sourceHolder;
    private TableScrollController scrollController;

    private LogModel logModel = LogModel.empty();
    @Inject
    BookmarkController bookmarkController;
    @Inject
    BookmarkModel bookmarkModel;
    private BufferFilterMenu bufferMenu;
    private JPanel controlsPanel;
    private JTextField instantSearchTextField;

    @Inject
    @Named(MainFrameDependencies.FOR_MAIN_FRAME)
    JTable logElements;
    @Inject
    LogRecordTableModel recordsModel;
    @Inject
    ProcessListFrame processListFrame;
    @Inject
    SourceStatusPresenter sourceStatusPresenter;
    @Inject
    SearchStatusPresenter searchStatusPresenter;
    @Inject
    MainFrameSearchPromptView searchPromptView;

    @Inject
    SearchPresenter<LogRecord, TablePosition, RowSearchStrategy> searchPresenter;

    @Inject
    StatusPanel statusPanel;
    @Inject
    ErrorDialogs errorDialogs;
    @Inject
    AdbConfigurationPref adbConfigurationPref;
    @Inject
    WindowsPositionsPref windowsPositionsPref;
    @Inject
    ConfigurationDialogPresenter configurationDialogPresenter;
    @Inject
    @Named(AppExecutors.UI_EXECUTOR)
    Executor uiExecutor;

    @Inject
    AdbManager adbManager;
    @Inject
    AdbServicesBridge adbServicesBridge;
    @Inject
    FileOpener fileOpener;
    @Inject
    TableColumnModelFactory columnModelFactory;
    @Inject
    FilterPanel filterPanel;
    @Inject
    MainFilterController mainFilterController;
    @Inject
    FileDialog fileDialog;

    @Nullable
    private DeviceChangeObserver pendingAttacher;
    private volatile boolean isWaitingForDevice;

    private final LogModel.Observer autoscrollObserver = new LogModel.Observer() {
        @Override
        public void onBeforeRecordsInserted() {
            scrollController.notifyBeforeInsert();
        }
    };

    private final PidToProcessMapper mapper = this::mapPidToProcessName;

    private final Action acOpenFile =
            UiHelper.makeAction("Open...", UiHelper.createPlatformKeystroke(KeyEvent.VK_O), this::openFile);
    private final Action acSaveToFile =
            UiHelper.makeAction("Save...", UiHelper.createPlatformKeystroke(KeyEvent.VK_S), this::saveToFile);

    private final Action acShowBookmarks =
            UiHelper.makeAction("Show bookmarks", UiHelper.createPlatformKeystroke(KeyEvent.VK_P),
                    () -> bookmarkController.showWindow());
    private final Action acShowProcesses =
            UiHelper.makeAction(this::showProcesses).name("Show processes").disabled().build();

    private final Action acConnectToDevice = UiHelper.makeAction("Connect to device...", this::connectToDevice);
    private final Action acResetLogs =
            UiHelper.makeAction("Reset logs", UiHelper.createPlatformKeystroke(KeyEvent.VK_R), this::reset);
    private final Action acChangeConfiguration =
            UiHelper.makeAction("Configuration...", () -> configurationDialogPresenter.openDialog());
    private final Action acDumpDevice = UiHelper.makeAction("Prepare device dump...", this::selectAndDumpDevice);

    private final Timer updatingTimer = new Timer(2000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            sourceStatusPresenter.updateSourceStatus();
        }
    });

    @SuppressWarnings("NullAway")
    private MainFrame(AppGlobals globals, CommandLine commandLine) {
        MainFrameDependencies dependencies =
                DaggerMainFrameDependencies.factory().create(new MainFrameModule(this), globals);
        dependencies.injectMainFrame(this);
        initialize(commandLine.isDebug());
    }

    public void setSource(DataSource newSource) {
        assert EventQueue.isDispatchThread();
        DataSource oldSource = sourceHolder.getDataSource();

        if (oldSource != null) {
            oldSource.close();
        }
        stopWaitingForDevice();
        sourceHolder.setDataSource(newSource);
        bookmarkModel.clear();
        bufferMenu.setAvailableBuffers(newSource.getAvailableBuffers());
        updatingTimer.start();
        logModel.asObservable().removeObserver(autoscrollObserver);
        logModel = LogModel.fromDataSource(newSource, SequentialExecutor.edt());
        logModel.asObservable().addObserver(autoscrollObserver);
        recordsModel.setLogModel(logModel);
        if (newSource.getPidToProcessConverter() != null) {
            acShowProcesses.setEnabled(true);
            processListFrame.setSource(newSource);
        } else {
            processListFrame.setSource(null);
            acShowProcesses.setEnabled(false);
        }

        Collection<Column> availableColumns = Column.getColumnsForFields(newSource.getAvailableFields());
        LogRecordTableColumnModel columns = columnModelFactory.create(mapper, availableColumns);
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

    private @Nullable String mapPidToProcessName(int pid) {
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

    /**
     * Initialize the contents of the frame.
     */
    private void initialize(boolean isDebug) {
        setTitle("AndLogView " + Main.getVersionString());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logElements.setFillsViewportHeight(true);
        logElements.setShowGrid(false);

        LogRecordTableColumnModel columnModel = columnModelFactory.create(mapper, Column.getSelectedColumns());
        logElements.setColumnModel(columnModel);
        UiHelper.addPopupMenu(
                logElements.getTableHeader(), new LogTableHeaderPopupMenuController(columnModel).createMenu());
        TransferHandler fileHandler = new FileTransferHandler(this, fileOpener);
        setTransferHandler(fileHandler);
        logElements.setTransferHandler(new LogRecordsTransferHandler(fileHandler));

        JScrollPane scrollPane = new JScrollPane(logElements);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        scrollController = new TableScrollController(logElements);

        controlsPanel = new JPanel();
        getContentPane().add(controlsPanel, BorderLayout.SOUTH);
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.PAGE_AXIS));

        instantSearchTextField = new JTextField();
        controlsPanel.add(instantSearchTextField);
        instantSearchTextField.setColumns(10);
        instantSearchTextField.setVisible(false);

        controlsPanel.add(filterPanel);
        controlsPanel.add(statusPanel.getPanel());
        sourceStatusPresenter.init();

        setupSearchButtons();
        setupMainMenu(isDebug);
        setPreferredSize(windowsPositionsPref.getFrameDimensions(WindowsPositionsPref.Frame.MAIN).toAwtDimension());
        Optional<FrameLocation> frameLocation = windowsPositionsPref.getFrameLocation(WindowsPositionsPref.Frame.MAIN);
        if (frameLocation.isPresent()) {
            setLocation(frameLocation.get().x, frameLocation.get().y);
        } else {
            setLocationByPlatform(true);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                windowsPositionsPref.setFrameInfo(WindowsPositionsPref.Frame.MAIN,
                        new FrameLocation(getLocation().x, getLocation().y),
                        new FrameDimensions(getWidth(), getHeight()));
            }
        });
        pack();
    }

    @Override
    public JTextField getSearchField() {
        return instantSearchTextField;
    }

    private void setupSearchButtons() {
        UiHelper.bindKeyGlobal(this, KEY_SHOW_SEARCH_FIELD, ACTION_SHOW_SEARCH_FIELD,
                e -> searchPresenter.showSearchPrompt());
        UiHelper.bindKeyGlobal(this, KEY_FIND_NEXT, ACTION_FIND_NEXT, e -> searchPresenter.findNext());

        UiHelper.bindKeyGlobal(this, KEY_FIND_PREV, ACTION_FIND_PREV, e -> searchPresenter.findPrev());

        UiHelper.bindKeyGlobal(this, KEY_HIDE, ACTION_HIDE_SEARCH_FIELD, e -> {
            searchPresenter.stopSearch();
        });

        UiHelper.bindKeyFocused(
                instantSearchTextField, KEY_HIDE_AND_START_SEARCH, ACTION_HIDE_AND_START_SEARCH,
                e -> searchPromptView.commit());
    }

    @Override
    public void showSearchField() {
        scrollController.notifyBeforeInsert();
        instantSearchTextField.setVisible(true);
        instantSearchTextField.selectAll();
        instantSearchTextField.requestFocusInWindow();
        searchStatusPresenter.reset();
        controlsPanel.revalidate();
        controlsPanel.repaint();
        scrollController.scrollIfNeeded();
    }

    @Override
    public void hideSearchField() {
        scrollController.notifyBeforeInsert();
        instantSearchTextField.setVisible(false);
        controlsPanel.revalidate();
        controlsPanel.repaint();
        scrollController.scrollIfNeeded();
    }

    public void reset() {
        LogModel model = logModel;
        if (model != null) {
            // TODO(mlopatkin) We should only do this if the source is resettable.
            model.clear();
        }
        DataSource source = sourceHolder.getDataSource();
        if (source != null && !source.reset()) {
            bookmarkModel.clear();
        }
    }

    private void setupMainMenu(boolean isDebug) {
        JMenuBar mainMenu = new JMenuBar();

        JMenu mnFile = new JMenu("File");
        mnFile.add(acOpenFile);
        mnFile.add(acSaveToFile);
        mainMenu.add(mnFile);

        JMenu mnView = new JMenu("View");
        mnView.add(acShowBookmarks);
        mnView.add(acShowProcesses);
        if (isDebug) {
            mnView.add(UiHelper.makeAction("Dump view hierarchy", this::list));
        }
        mainMenu.add(mnView);

        JMenu mnAdb = new JMenu("ADB");
        mnAdb.add(acConnectToDevice);
        mnAdb.addSeparator();
        mnAdb.add(acResetLogs);
        mnAdb.add(acChangeConfiguration);
        if (isDebug) {
            mnAdb.addSeparator();
            mnAdb.add(acDumpDevice);
        }
        mainMenu.add(mnAdb);

        JMenu mnFilters = new JMenu("Buffers");
        bufferMenu = new BufferFilterMenu(mnFilters, mainFilterController);
        mainMenu.add(mnFilters);

        setJMenuBar(mainMenu);
    }

    private void openFile() {
        fileDialog.selectFileToOpen()
                .ifPresent(file ->
                        fileOpener.openFileAsDataSource(file)
                                .thenAccept(MainFrame.this::setSourceAsync));
    }

    private void connectToDevice() {
        Optionals.ifPresentOrElse(
                adbServicesBridge.getAdbDataSourceFactory(),
                adbDataSourceFactory -> adbDataSourceFactory.selectDeviceAndOpenAsDataSource(
                        MainFrame.this::setSourceAsync),
                MainFrame.this::disableAdbCommandsAsync);
    }

    private void selectAndDumpDevice() {
        Optionals.ifPresentOrElse(adbServicesBridge.getDumpDevicePresenter(),
                DumpDevicePresenter::selectDeviceAndDump,
                MainFrame.this::disableAdbCommandsAsync);
    }

    public void tryToConnectToFirstAvailableDevice() {
        Optionals.ifPresentOrElse(adbServicesBridge.getAdbServices(),
                adbServices -> Optionals.ifPresentOrElse(
                        getFirstOnlineDevice(adbServices.getDeviceList()),
                        device -> adbServices.getDataSourceFactory().openDeviceAsDataSource(
                                device, this::setSourceAsync),
                        this::waitForDevice),
                this::disableAdbCommandsAsync);
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
            public void onDeviceConnected(Device device) {
                if (device.isOnline()) {
                    connectDevicePending(device);
                }
            }

            @Override
            public void onDeviceChanged(Device device) {
                if (device.isOnline()) {
                    connectDevicePending(device);
                }
            }
        };

        Optionals.ifPresentOrElse(adbServicesBridge.getAdbDeviceList(), deviceList -> {
            deviceList.asObservable().addObserver(attacher);
            getFirstOnlineDevice(deviceList).ifPresent(this::connectDevicePending);
        }, this::disableAdbCommandsAsync);
    }

    private synchronized void connectDevicePending(Device device) {
        if (!isWaitingForDevice) {
            return;
        }
        isWaitingForDevice = false;
        stopWaitingForDevice();
        Optionals.ifPresentOrElse(adbServicesBridge.getAdbDataSourceFactory(),
                dataSourceFactory -> dataSourceFactory.openDeviceAsDataSource(device, this::setSourceAsync),
                this::disableAdbCommandsAsync);
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

    private void saveToFile() {
        fileDialog.selectFileToSave().ifPresent(MainFrame.this::saveTableToFile);
    }

    private void showProcesses() {
        DataSource source = sourceHolder.getDataSource();
        assert source != null;
        assert source.getPidToProcessConverter() != null;
        processListFrame.show();
    }

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

    private Optional<Device> getFirstOnlineDevice(AdbDeviceList deviceList) {
        return deviceList.getDevices().stream().filter(Device::isOnline).findFirst();
    }

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
}
