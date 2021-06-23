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

import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbDataSource;
import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbDeviceManager;
import name.mlopatkin.andlogview.liblogcat.file.FileDataSourceFactory;
import name.mlopatkin.andlogview.liblogcat.file.UnrecognizedFormatException;
import name.mlopatkin.andlogview.utils.SystemUtils;
import name.mlopatkin.andlogview.utils.properties.IllegalConfigurationException;
import name.mlopatkin.andlogview.utils.properties.PropertyUtils;

import com.android.ddmlib.IDevice;

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

        try {
            Configuration.load(commandLine.isDebug());
        } catch (IllegalConfigurationException e) {
            logger.warn("Unexpected exception while parsing config file", e);
            ErrorDialogsHelper.showError(null, "Error in configuration file: " + e.getMessage());
        }

        AppGlobals globals = DaggerAppGlobals.factory().create(new GlobalsModule(getConfigurationDir()), commandLine);

        logger.info("AndLogView " + getVersionString());
        logger.info("Revision " + BuildInfo.REVISION);

        EventQueue.invokeLater(globals.getMain()::start);
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
        return BuildInfo.VERSION + " (build " + BuildInfo.BUILD + " " + BuildInfo.REVISION + ")";
    }

    private MainFrame createAndShowWindow() {
        MainFrame window = mainFrameProvider.get();
        window.setVisible(true);
        return window;
    }

    /** Opens the main frame. Must be called from EDT. */
    private void start() {
        MainFrame window = createAndShowWindow();

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
                IDevice device = AdbDeviceManager.getDefaultDevice();
                if (device != null) {
                    DeviceDisconnectedHandler.startWatching(window, device);
                    window.setSourceAsync(new AdbDataSource(device));
                } else {
                    window.waitForDevice();
                }
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
