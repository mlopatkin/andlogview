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

import com.android.ddmlib.IDevice;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDataSource;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDeviceManager;
import org.bitbucket.mlopatkin.android.liblogcat.file.FileDataSourceFactory;
import org.bitbucket.mlopatkin.android.liblogcat.file.UnrecognizedFormatException;
import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;
import org.bitbucket.mlopatkin.utils.SystemUtils;
import org.bitbucket.mlopatkin.utils.properties.IllegalConfigurationException;
import org.bitbucket.mlopatkin.utils.properties.PropertyUtils;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;

import javax.swing.JOptionPane;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    private static final String SHORT_APP_NAME = "logview";

    private @MonotonicNonNull DataSource initialSource;
    private MainFrame window;

    public static File getConfigurationDir() {
        return PropertyUtils.getAppConfigDir(SHORT_APP_NAME);
    }

    public static void main(String[] args) {
        Configuration.init();
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        OptionParser parser = new OptionParser("d");
        OptionSet result = parser.parse(args);
        boolean debug = result.has("d");
        try {
            Configuration.load(debug);
        } catch (IllegalConfigurationException e) {
            logger.warn("Unxpected exception while parsing config file", e);
            ErrorDialogsHelper.showError(null, "Error in configuration file: " + e.getMessage());
        }

        logger.info("AndLogView " + getVersionString());
        logger.info("Revision " + BuildInfo.REVISION);
        @SuppressWarnings("unchecked")
        List<String> files = (List<String>) result.nonOptionArguments();
        if (files.size() == 0) {
            // ADB mode
            EventQueue.invokeLater(() -> new Main().start());
        } else if (files.size() == 1) {
            // File mode
            EventQueue.invokeLater(() -> new Main(new File(files.get(0))).start());
        } else {
            // Error
            showUsage();
        }
    }

    Main(File file) {
        createAndShowWindow();
        try {
            initialSource = FileDataSourceFactory.createDataSource(file);
            // TODO(mlopatkin) Handle null parent file here
            File baseDir = file.getAbsoluteFile().getParentFile();
            window.setRecentDir(baseDir);
        } catch (UnrecognizedFormatException e) {
            ErrorDialogsHelper.showError(window, "Unrecognized file format for " + file);
            logger.error("Exception while reading the file", e);
        } catch (IOException e) {
            ErrorDialogsHelper.showError(window, "Cannot read " + file);
            logger.error("Exception while reading the file", e);
        }
    }

    Main() {
        createAndShowWindow();
        if (window.tryInitAdbBridge()) {
            IDevice device = AdbDeviceManager.getDefaultDevice();
            if (device != null) {
                DeviceDisconnectedHandler.startWatching(window, device);
                initialSource = new AdbDataSource(device);
            } else {
                window.waitForDevice();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static String getVersionString() {
        if (!BuildInfo.VERSION.endsWith("SNAPSHOT")) {
            return BuildInfo.VERSION;
        }
        return BuildInfo.VERSION + " (build " + BuildInfo.BUILD + " " + BuildInfo.REVISION + ")";
    }

    private void createAndShowWindow() {
        AppGlobals globals = DaggerAppGlobals.builder().globalsModule(new GlobalsModule(getConfigurationDir())).build();
        window = new MainFrame(globals);
        window.setVisible(true);
    }

    void start() {
        if (initialSource != null) {
            window.setSourceAsync(initialSource);
        }
    }

    private static void showUsage() {
        EventQueue.invokeLater(
                () -> JOptionPane.showMessageDialog(null, "<html>Usage:<br>java -jar logview.jar [FILENAME]</html>",
                        "Incorrect parameters", JOptionPane.ERROR_MESSAGE));
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
