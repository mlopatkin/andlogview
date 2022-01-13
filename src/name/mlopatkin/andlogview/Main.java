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

import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.liblogcat.file.FileDataSourceFactory;
import name.mlopatkin.andlogview.liblogcat.file.UnrecognizedFormatException;
import name.mlopatkin.andlogview.thirdparty.systemutils.SystemUtils;
import name.mlopatkin.andlogview.ui.themes.Theme;
import name.mlopatkin.andlogview.utils.Try;
import name.mlopatkin.andlogview.utils.properties.IllegalConfigurationException;
import name.mlopatkin.andlogview.utils.properties.PropertyUtils;

import org.apache.log4j.Logger;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JOptionPane;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    private static final String THEME_SYSTEM_PROPERTY = "name.mlopatkin.andlogview.theme";

    private static final String SHORT_APP_NAME = "logview";
    private final Provider<MainFrame> mainFrameProvider;
    private final CommandLine commandLine;

    public static File getConfigurationDir() {
        return PropertyUtils.getAppConfigDir(SHORT_APP_NAME);
    }

    public static void main(String[] args) {
        Configuration.init();
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

        CommandLine commandLine;
        try {
            commandLine = new CommandLine(args);
        } catch (IllegalConfigurationException e) {
            // Parse error: fall back to the default command line.
            commandLine = new CommandLine();
        }
        boolean isDebugMode = commandLine.isDebug();

        Try<?> configurationState = Try.ofCallable(() -> {
            Configuration.load(isDebugMode);
            return true;
        });

        Theme theme = initLaf();

        AppGlobals globals = DaggerAppGlobals.factory().create(
                new GlobalsModule(getConfigurationDir()),
                commandLine,
                theme);

        logger.info("AndLogView " + getVersionString());
        logger.info("Revision " + BuildInfo.REVISION);

        EventQueue.invokeLater(() -> globals.getMain().start(configurationState));
    }

    private static Theme initLaf() {
        if (SystemUtils.IS_OS_MACOS) {
            // Move JMenuBar to macOS native global Menu bar.
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // Change App name in menu bar.
            System.setProperty("apple.awt.application.name", "AndLogView");
            // Force default light style even with global system dark mode to fix black-on-black text in some controls.
            System.setProperty("apple.awt.application.appearance", "NSAppearanceNameAqua");
        }
        String configuredThemeName = System.getProperty(THEME_SYSTEM_PROPERTY);
        Theme theme = Theme.findByName(configuredThemeName).orElseGet(Theme::getDefault);
        assert theme.isSupported();
        if (!theme.install()) {
            theme = Theme.getFallback();
            theme.install();
        }
        return theme;
    }

    @Inject
    Main(MainFrame.Factory mainFrameFactory, CommandLine commandLine) {
        this.mainFrameProvider = mainFrameFactory;
        this.commandLine = commandLine;
    }

    @SuppressWarnings("ConstantConditions")
    public static String getVersionString() {
        if (!BuildInfo.VERSION.endsWith("SNAPSHOT")) {
            return BuildInfo.VERSION;
        }
        return BuildInfo.VERSION + " (rev " + BuildInfo.REVISION + ")";
    }

    private MainFrame createAndShowWindow() {
        MainFrame window = mainFrameProvider.get();
        window.setVisible(true);
        return window;
    }

    /** Opens the main frame. Must be called from EDT. */
    private void start(Try<?> configurationLoadingState) {
        MainFrame window = createAndShowWindow();

        // As this shows a dialog, we have to do this on the EDT.
        configurationLoadingState.handleError(e -> {
            logger.warn("Unexpected exception while parsing config file", e);
            ErrorDialogsHelper.showError(window, "Error in configuration file: " + e.getMessage());
        });

        if (commandLine.isShouldShowUsage()) {
            JOptionPane.showMessageDialog(window, "<html>Usage:<br>java -jar logview.jar [FILENAME]</html>",
                    "Incorrect parameters", JOptionPane.ERROR_MESSAGE);
        }
        File fileToOpen = commandLine.getFileArgument();
        if (fileToOpen != null) {
            // TODO(mlopatkin) Handle null parent file here
            File baseDir = fileToOpen.getAbsoluteFile().getParentFile();
            window.setRecentDir(baseDir);
            try {
                window.setSourceAsync(FileDataSourceFactory.createDataSource(fileToOpen));
            } catch (UnrecognizedFormatException e) {
                ErrorDialogsHelper.showError(window, "Unrecognized file format for " + fileToOpen);
                logger.error("Exception while reading the file", e);
            } catch (IOException e) {
                ErrorDialogsHelper.showError(window, "Cannot read " + fileToOpen);
                logger.error("Exception while reading the file", e);
            }
        } else {
            if (window.tryInitAdbBridge()) {
                window.tryToConnectToFirstAvailableDevice();
            }
        }
    }

    private static UncaughtExceptionHandler exceptionHandler = (thread, throwable) -> {
        try {
            logger.error("Uncaught exception in " + thread.getName(), throwable);
            ErrorDialogsHelper.showError(null,
                    "<html>Unhandled exception occured. Please collect log file at<br>"
                            + new File(SystemUtils.getJavaIoTmpDir(), "logview.log").getAbsolutePath()
                            + "<br>and send it to the authors, then restart the program");
        } catch (Throwable ex) { // OK to catch Throwable here
            // bad idea to log something if we already failed with logging
            // logger.error("Exception in exception handler", ex);
        }
    };
}
