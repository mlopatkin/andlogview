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
import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.liblogcat.LogRecordFormatter;
import name.mlopatkin.andlogview.liblogcat.ddmlib.DeviceDisconnectedHandler;
import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.logmodel.Field;
import name.mlopatkin.andlogview.logmodel.LogModel;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.preferences.WindowsPositionsPref;
import name.mlopatkin.andlogview.search.logrecord.RowSearchStrategy;
import name.mlopatkin.andlogview.ui.FileDialog;
import name.mlopatkin.andlogview.ui.FrameDimensions;
import name.mlopatkin.andlogview.ui.FrameLocation;
import name.mlopatkin.andlogview.ui.UncaughtExceptionDialogHandler;
import name.mlopatkin.andlogview.ui.about.AboutUi;
import name.mlopatkin.andlogview.ui.bookmarks.BookmarkController;
import name.mlopatkin.andlogview.ui.device.AdbOpener;
import name.mlopatkin.andlogview.ui.device.AdbServicesInitializationPresenter;
import name.mlopatkin.andlogview.ui.device.AdbServicesStatus;
import name.mlopatkin.andlogview.ui.file.FileOpener;
import name.mlopatkin.andlogview.ui.filters.LogModelFilterImpl;
import name.mlopatkin.andlogview.ui.filtertree.FilterTreeFactory;
import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableColumnModel;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.ui.logtable.LogTableHeaderPopupMenuController;
import name.mlopatkin.andlogview.ui.mainframe.BufferFilterMenu;
import name.mlopatkin.andlogview.ui.mainframe.DaggerMainFrameDependencies;
import name.mlopatkin.andlogview.ui.mainframe.ErrorDialogs;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameDependencies;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameModule;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameUi;
import name.mlopatkin.andlogview.ui.mainframe.TableColumnModelFactory;
import name.mlopatkin.andlogview.ui.mainframe.search.MainFrameSearchPromptView;
import name.mlopatkin.andlogview.ui.mainframe.search.MainFrameSearchUi;
import name.mlopatkin.andlogview.ui.preferences.ConfigurationDialogPresenter;
import name.mlopatkin.andlogview.ui.processes.ProcessListFrame;
import name.mlopatkin.andlogview.ui.search.SearchPresenter;
import name.mlopatkin.andlogview.ui.search.logtable.TablePosition;
import name.mlopatkin.andlogview.ui.status.StatusPanel;
import name.mlopatkin.andlogview.ui.themes.CurrentTheme;
import name.mlopatkin.andlogview.utils.CommonChars;
import name.mlopatkin.andlogview.utils.MyFutures;
import name.mlopatkin.andlogview.widgets.UiHelper;

import com.google.common.io.Files;

import org.apache.commons.lang3.SystemUtils;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

public class MainFrame implements MainFrameSearchUi, DeviceDisconnectedHandler.DeviceAwaiter {
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);

    private static final KeyStroke KEY_HIDE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    private static final KeyStroke KEY_SHOW_SEARCH_FIELD = UiHelper.createPlatformKeystroke(KeyEvent.VK_F);
    private static final KeyStroke KEY_FIND_NEXT =
            SystemUtils.IS_OS_MAC_OSX
                    ? UiHelper.createPlatformKeystroke(KeyEvent.VK_G)
                    : KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
    private static final KeyStroke KEY_FIND_PREV =
            SystemUtils.IS_OS_MAC_OSX
                    ? UiHelper.createPlatformKeystroke(KeyEvent.VK_G, InputEvent.SHIFT_DOWN_MASK)
                    : KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_DOWN_MASK);

    @Inject
    CurrentTheme theme;

    @Inject
    MainFrameUi mainFrameUi;
    @Inject
    FilterModel filterModel;
    @Inject
    FilterTreeFactory filterTreeFactory;

    @Inject
    DataSourceHolder sourceHolder;
    private TableScrollController scrollController;

    private LogModel logModel = LogModel.empty();
    @Inject
    BookmarkController bookmarkController;
    @Inject
    BookmarkModel bookmarkModel;
    @Inject
    BufferFilterMenu bufferMenu;

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
    AdbServicesInitializationPresenter adbInitPresenter;
    @Inject
    FileOpener fileOpener;
    @Inject
    AdbOpener adbOpener;
    @Inject
    TableColumnModelFactory columnModelFactory;
    @Inject
    LogModelFilterImpl logModelFilter;
    @Inject
    FileDialog fileDialog;
    @Inject
    AdbServicesStatus adbServicesStatus;

    private @Nullable CompletableFuture<? extends @Nullable DataSource> pendingDataSource;

    private final LogModel.Observer autoscrollObserver = new LogModel.Observer() {
        @Override
        public void onBeforeRecordsInserted() {
            scrollController.notifyBeforeInsert();
        }
    };

    private final PidToProcessMapper mapper = this::mapPidToProcessName;

    // File menu
    private final Action acOpenFile =
            UiHelper.makeAction("Open...", UiHelper.createPlatformKeystroke(KeyEvent.VK_O), this::selectAndOpenFile);
    private final Action acSaveToFile =
            UiHelper.makeAction("Save...", UiHelper.createPlatformKeystroke(KeyEvent.VK_S), this::saveToFile);

    // Edit menu
    private final Action acFind =
            UiHelper.makeAction("Find" + CommonChars.ELLIPSIS,
                    KEY_SHOW_SEARCH_FIELD,
                    () -> searchPresenter.showSearchPrompt());
    private final Action acFindNext =
            UiHelper.makeAction("Find next occurrence", KEY_FIND_NEXT, () -> searchPresenter.findNext());
    private final Action acFindPrev =
            UiHelper.makeAction("Find previous occurrence", KEY_FIND_PREV, () -> searchPresenter.findPrev());
    private final Action acStopSearch =
            UiHelper.makeAction("Stop search", KEY_HIDE, () -> searchPresenter.stopSearch());

    // View menu
    private final Action acShowBookmarks =
            UiHelper.makeAction("Show bookmarks", UiHelper.createPlatformKeystroke(KeyEvent.VK_P),
                    () -> bookmarkController.showWindow());
    private final Action acShowProcesses =
            UiHelper.makeAction(this::showProcesses).name("Show processes").disabled().build();

    // ADB menu
    private final Action acConnectToDevice = UiHelper.makeAction("Connect to device...", this::connectToDevice);
    private final Action acResetLogs =
            UiHelper.makeAction("Reset logs", UiHelper.createPlatformKeystroke(KeyEvent.VK_R), this::reset);
    private final Action acChangeConfiguration =
            UiHelper.makeAction("Configuration...", () -> configurationDialogPresenter.openDialog());
    private final Action acDumpDevice = UiHelper.makeAction("Prepare device dump...", this::selectAndDumpDevice);

    // Help menu
    private final Action acAbout = UiHelper.makeAction("About", this::showAboutDialog);

    private MainFrame(AppGlobals globals, CommandLine commandLine) {
        MainFrameDependencies dependencies =
                DaggerMainFrameDependencies.factory().create(new MainFrameModule(this), globals);
        dependencies.injectMainFrame(this);
        initialize(commandLine.isDebug());
    }

    private void consumePendingSourceAsync(CompletableFuture<? extends @Nullable DataSource> origin,
            @Nullable DataSource newSource) {
        if (EventQueue.isDispatchThread()) {
            consumePendingSource(origin, newSource);
        } else {
            EventQueue.invokeLater(() -> consumePendingSource(origin, newSource));
        }
    }

    private void consumePendingSource(CompletableFuture<? extends @Nullable DataSource> origin,
            @Nullable DataSource newSource) {
        assert EventQueue.isDispatchThread();
        // Clean up current pending operation.
        // TODO(mlopatkin) think about potential race conditions
        assert pendingDataSource == origin;
        pendingDataSource = null;
        if (newSource != null) {
            setSource(newSource);
        } else if (sourceHolder.getDataSource() == null) {
            // Don't have an open data source at the moment and nothing was selected - fall back to trying to open first
            // connected device.
            tryToConnectToFirstAvailableDevice();
        }
    }

    private void setSource(DataSource newSource) {
        assert EventQueue.isDispatchThread();
        DataSource oldSource = sourceHolder.getDataSource();

        if (oldSource != null) {
            oldSource.close();
        }

        sourceHolder.setDataSource(newSource);
        bookmarkModel.clear();
        bufferMenu.setAvailableBuffers(newSource.getAvailableBuffers());
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
        logElements.getTableHeader().setComponentPopupMenu(new LogTableHeaderPopupMenuController(columns).createMenu());
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
    @EnsuresNonNull({"controlsPanel", "instantSearchTextField"})
    private void initialize(boolean isDebug) {
        mainFrameUi.setTitle(Main.APP_NAME + " " + Main.getVersionString());
        mainFrameUi.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initLogTable();

        TransferHandler fileHandler = new FileTransferHandler(this);
        mainFrameUi.setTransferHandler(fileHandler);
        logElements.setTransferHandler(new LogRecordsTransferHandler(fileHandler));

        scrollController = new TableScrollController(logElements);

        initControlPanel();

        setupMainMenu(isDebug);
        setupSize();

        var exceptionDialog = UncaughtExceptionDialogHandler.install(mainFrameUi);

        mainFrameUi.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                windowsPositionsPref.setFrameInfo(WindowsPositionsPref.Frame.MAIN,
                        new FrameLocation(mainFrameUi.getLocation().x, mainFrameUi.getLocation().y),
                        new FrameDimensions(mainFrameUi.getWidth(), mainFrameUi.getHeight()));
            }

            @Override
            public void windowClosed(WindowEvent e) {
                exceptionDialog.uninstall();
            }
        });
        mainFrameUi.pack();
    }

    private void initLogTable() {
        logElements.setFillsViewportHeight(true);
        logElements.setShowGrid(false);

        LogRecordTableColumnModel columnModel = columnModelFactory.create(mapper, Column.getColumnsForFields(
                Field.values()));
        logElements.setColumnModel(columnModel);
        logElements.getTableHeader()
                .setComponentPopupMenu(new LogTableHeaderPopupMenuController(columnModel).createMenu());

        JScrollPane logTableScrollPane = new JScrollPane(logElements);

        var filterToolbar = new JToolBar();
        var filterTreePane = new JScrollPane(filterTreeFactory.buildFilterTree(filterToolbar));
        filterTreePane.setPreferredSize(new Dimension(200, 0));

        // A combined filter tree and the filter toolbar
        var filterControls = new JPanel(new BorderLayout());
        filterControls.add(filterToolbar, BorderLayout.NORTH);
        filterControls.add(filterTreePane, BorderLayout.CENTER);

        // Add a small separator line on top of the filter toolbar to visually glue toolbar and the tree together.
        // This isn't needed on macOS, because the top menu is detached, and there is a line between the window header
        // and the content. An extra separator results in double line. This may be reconsidered if I decide to merge
        // top bar with the panel.
        if (!SystemUtils.IS_OS_MAC_OSX) {
            filterControls.setBorder(theme.get().getWidgetFactory().createTopSeparatorBorder());
        }

        var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filterControls, logTableScrollPane);
        // Some paddings around the table and the filters to avoid them overlapping the other stuff, like the toolbar.
        splitPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        mainFrameUi.getContentPane().add(splitPane, BorderLayout.CENTER);

        // Make the toolbar and the table header of the same height. This is fishy, but seems to work so far.
        alignPreferredHeights(logElements.getTableHeader(), filterToolbar);
    }

    private static void alignPreferredHeights(Component component1, Component component2) {
        var size1 = component1.getPreferredSize();
        var size2 = component2.getPreferredSize();
        size2.height = size1.height = Math.max(size1.height, size2.height);
        component1.setPreferredSize(size1);
        component2.setPreferredSize(size2);
    }

    @EnsuresNonNull({"controlsPanel", "instantSearchTextField"})
    private void initControlPanel() {
        controlsPanel = new JPanel();
        mainFrameUi.getContentPane().add(controlsPanel, BorderLayout.SOUTH);
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.PAGE_AXIS));

        initInstantSearch();

        controlsPanel.add(statusPanel.getPanel());
    }

    @EnsuresNonNull("instantSearchTextField")
    private void initInstantSearch() {
        instantSearchTextField = new JTextField();
        controlsPanel.add(instantSearchTextField);
        instantSearchTextField.setColumns(10);
        instantSearchTextField.setVisible(false);

        // Menu accelerators are significantly slower than key bindings.
        UiHelper.bindKeyGlobal(mainFrameUi, KEY_FIND_NEXT, "find_next", acFindNext);
        UiHelper.bindKeyGlobal(mainFrameUi, KEY_FIND_PREV, "find_prev", acFindPrev);
    }

    private void setupSize() {
        mainFrameUi.setPreferredSize(
                windowsPositionsPref.getFrameDimensions(WindowsPositionsPref.Frame.MAIN).toAwtDimension());
        var frameLocation = windowsPositionsPref.getFrameLocation(WindowsPositionsPref.Frame.MAIN);
        if (frameLocation != null) {
            mainFrameUi.setLocation(frameLocation.x(), frameLocation.y());
        } else {
            mainFrameUi.setLocationByPlatform(true);
        }
    }

    @Override
    public JTextField getSearchField() {
        return instantSearchTextField;
    }

    @Override
    public void showSearchField() {
        scrollController.notifyBeforeInsert();
        instantSearchTextField.setVisible(true);
        instantSearchTextField.selectAll();
        instantSearchTextField.requestFocusInWindow();
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

        JMenu mnEdit = new JMenu("Edit");
        mnEdit.add(acFind);
        mnEdit.add(acFindNext);
        mnEdit.add(acFindPrev);
        mnEdit.add(acStopSearch);
        mainMenu.add(mnEdit);

        JMenu mnView = new JMenu("View");
        mnView.add(acShowBookmarks);
        mnView.add(acShowProcesses);
        if (isDebug) {
            mnView.add(UiHelper.makeAction("Dump view hierarchy", mainFrameUi::list));
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

        mainMenu.add(bufferMenu.getBuffersMenu());

        var mnHelp = new JMenu("Help");
        mnHelp.add(acAbout);
        mainMenu.add(mnHelp);

        mainFrameUi.setJMenuBar(mainMenu);

        adbServicesStatus.asObservable().addObserver(this::onAdbServicesStatusChanged);
        onAdbServicesStatusChanged(adbServicesStatus.getStatus());
    }

    public void openFile(File file) {
        startOpeningDataSource(() -> fileOpener.openFile(file));
    }

    private void selectAndOpenFile() {
        startOpeningDataSource(fileOpener::selectAndOpenFile);
    }

    private void connectToDevice() {
        var source = adbOpener.selectAndOpenDevice();
        startOpeningDataSource(() -> source);
    }

    private void selectAndDumpDevice() {
        // TODO(mlopatkin) A progress dialog here?
        adbInitPresenter.withAdbServicesInteractive(
                adbServices -> adbServices.getDumpDevicePresenter().selectDeviceAndDump(),
                // TODO(mlopatkin) see startOpeningDataSource
                th -> {});
    }

    public void tryToConnectToFirstAvailableDevice() {
        waitForDevice();
    }

    private void startOpeningDataSource(
            Supplier<? extends CompletableFuture<? extends @Nullable DataSource>> pendingDataSource) {
        cancelPendingOperation();
        // Cancelling before obtaining is important, because obtaining a pending data source may start a nested event
        // loop and block.
        var newPendingDataSource = pendingDataSource.get();
        assert this.pendingDataSource == null;
        // it is important to assign pending data source first, because whenAvailable might fire right away and
        // overwrite data source too.
        this.pendingDataSource = newPendingDataSource;

        newPendingDataSource.handle(MyFutures.consumingHandler(
                        source -> consumePendingSourceAsync(newPendingDataSource, source),
                        th -> {
                            // TODO(mlopatkin) openers already show error dialogs for failures. However, adb opener
                            //  also propagates the loading failure here, it probably no longer should? Or how should
                            //  we communicate loading aborted because of error?
                        }))
                // The failureHandler has consumed all exceptions at this point.
                // The next stage handles exceptions from the handler itself.
                .exceptionally(MyFutures::uncaughtException);
    }

    private void cancelPendingOperation() {
        assert EventQueue.isDispatchThread();
        var operation = pendingDataSource;
        if (operation != null) {
            operation.cancel(false);
            pendingDataSource = null;
        }
    }

    /**
     * Wait for device to connect.
     */
    @Override
    public void waitForDevice() {
        var source = adbOpener.awaitDevice();
        startOpeningDataSource(() -> source);
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

    private void onAdbServicesStatusChanged(AdbServicesStatus.StatusValue newStatus) {
        acConnectToDevice.setEnabled(!newStatus.isFailed());
        acDumpDevice.setEnabled(!newStatus.isFailed());
    }

    private void showAboutDialog() {
        new AboutUi(mainFrameUi).setVisible(true);
    }

    public static class Factory {
        private final AppGlobals globals;
        private final CommandLine commandLine;

        @Inject
        public Factory(AppGlobals globals, CommandLine commandLine) {
            this.globals = globals;
            this.commandLine = commandLine;
        }

        public MainFrame get() {
            return new MainFrame(globals, commandLine);
        }
    }
}
