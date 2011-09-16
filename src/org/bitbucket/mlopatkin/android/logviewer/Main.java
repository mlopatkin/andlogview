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

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbConnectionManager;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDataSource;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDeviceManager;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbException;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.DdmlibUnsupportedException;
import org.bitbucket.mlopatkin.android.liblogcat.file.FileDataSourceFactory;
import org.bitbucket.mlopatkin.android.liblogcat.file.UnrecognizedFormatException;

import com.android.ddmlib.IDevice;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    public static final String APP_VERSION = "0.17.1";

    private DataSource initialSource;
    private MainFrame window;

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        Configuration.forceInit();
        logger.info("Android Log Viewer " + APP_VERSION);
        if (args.length == 0) {
            // ADB mode
            new Main().start();
        } else if (args.length == 1) {
            // File mode
            new Main(new File(args[0])).start();
        } else {
            // Error
            showUsage();
        }
    }

    Main(File file) {
        createAndShowWindow();
        try {
            initialSource = FileDataSourceFactory.createDataSource(file);
            window.setRecentDir(file.getAbsoluteFile().getParentFile());
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
        try {
            AdbConnectionManager.init();

            IDevice device = AdbDeviceManager.getDefaultDevice();
            if (device != null) {
                DeviceDisconnectedNotifier.startWatching(device);
                initialSource = new AdbDataSource(device);
            } else {
                window.waitForDevice();
            }
        } catch (AdbException e) {
            logger.warn("Cannot start in ADB mode", e);
            window.disableAdbCommandsAsync();
            ErrorDialogsHelper.showAdbNotFoundError(window);
        } catch (DdmlibUnsupportedException e) {
            logger.error("Cannot work with DDMLIB supplied", e);
            window.disableAdbCommandsAsync();
            ErrorDialogsHelper.showError(window, e.getMessage());
        }
    }

    private void createAndShowWindow() {
        window = new MainFrame();
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                window.setVisible(true);
            }
        });
    }

    void start() {
        if (initialSource != null) {
            window.setSourceAsync(initialSource);
        }
    }

    private static void showUsage() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null,
                        "<html>Usage:<br>java -jar logview.jar [FILENAME]</html>",
                        "Incorrect parameters", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static UncaughtExceptionHandler exceptionHandler = new UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            try {
                logger.error("Uncaught exception in " + t.getName(), e);
            } catch (Throwable ex) {
                // bad idea to log something if we already failed with logging
                // logger.error("Exception in exception handler", ex);
            }

        }
    };
}
