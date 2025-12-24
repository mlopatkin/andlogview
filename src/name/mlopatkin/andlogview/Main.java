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
import name.mlopatkin.andlogview.config.ConfigurationLocation;
import name.mlopatkin.andlogview.ui.themes.Theme;
import name.mlopatkin.andlogview.utils.Try;
import name.mlopatkin.andlogview.widgets.dialogs.OptionPanes;

import com.formdev.flatlaf.util.SystemInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.EventQueue;
import java.io.File;

import javax.inject.Inject;

public class Main {
    static {
        // Configure system properties for the runtime.
        // We must do it before AWT initializes, and it can happen in surprising places.
        // As it is very unlikely to fail, we do it even before starting the logs.
        initProperties();
    }

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String THEME_SYSTEM_PROPERTY = "name.mlopatkin.andlogview.theme";

    public static final String APP_NAME = "AndLogView";

    private final MainFrame.Factory mainFrameProvider;
    private final CommandLine commandLine;

    public static void main(String[] args) {
        Logging.initialConfiguration();
        // This is the log-only handler. Logging depends on configuration, though.
        Thread.setDefaultUncaughtExceptionHandler(Main::uncaughtHandler);

        var configurationLoc = new ConfigurationLocation();
        var commandLine = CommandLine.fromArgs(args);

        if (commandLine.isDebug()) {
            Logging.useDebugLogging();
        } else {
            Logging.useProductionLogging();
        }

        logger.info("{} {}", APP_NAME, getVersionString());
        logger.info("Revision {}", BuildInfo.REVISION);

        Try<?> configurationState = Try.ofCallable(() -> {
            Configuration.load(configurationLoc.getLegacyConfigurationFile());
            return true;
        });

        Theme theme = initLaf();

        AppGlobals globals = DaggerAppGlobals.factory().create(configurationLoc, commandLine, theme);

        EventQueue.invokeLater(() -> globals.getMain().start(configurationState));
    }

    private static void initProperties() {
        if (SystemInfo.isMacOS) {
            // Move JMenuBar to macOS native global Menu bar.
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // Change App name in menu bar.
            System.setProperty("apple.awt.application.name", APP_NAME);
            // Force default light style even with global system dark mode to fix black-on-black text in some controls.
            System.setProperty("apple.awt.application.appearance", "NSAppearanceNameAqua");
        }
    }

    private static Theme initLaf() {
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

    public static String getVersionString() {
        if (!BuildInfo.isSnapshot()) {
            return BuildInfo.VERSION;
        }
        return BuildInfo.VERSION + " (rev " + BuildInfo.REVISION + ")";
    }

    private MainFrame createAndShowWindow() {
        MainFrame window = mainFrameProvider.get();
        window.mainFrameUi.setVisible(true);
        return window;
    }

    /** Opens the main frame. Must be called from EDT. */
    private void start(Try<?> configurationLoadingState) {
        MainFrame window = createAndShowWindow();

        // As this shows a dialog, we have to do this on the EDT.
        configurationLoadingState.handleError(e -> {
            logger.warn("Unexpected exception while parsing config file", e);
            ErrorDialogsHelper.showError(window.mainFrameUi, "Error in configuration file: " + e.getMessage());
        });

        if (commandLine.isShouldShowUsage()) {
            OptionPanes.error("Incorrect parameters")
                    .message("<html>Usage:<br>java -jar logview.jar [FILENAME]</html>")
                    .show(window.mainFrameUi);
        }
        File fileToOpen = commandLine.getFileArgument();
        if (fileToOpen != null) {
            window.openFile(fileToOpen);
        } else {
            window.tryToConnectToFirstAvailableDevice();
        }
    }

    private static void uncaughtHandler(Thread thread, Throwable throwable) {
        try {
            logger.error("Uncaught exception in {}", thread.getName(), throwable);
        } catch (Throwable ex) { // OK to catch Throwable here
            // bad idea to log something if we already failed with logging
            // logger.error("Exception in exception handler", ex);
        }
    }
}
