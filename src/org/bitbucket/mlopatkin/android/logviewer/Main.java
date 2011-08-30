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

import javax.swing.JOptionPane;

import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDataSource;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDeviceManager;
import org.bitbucket.mlopatkin.android.liblogcat.file.FileDataSourceFactory;
import org.bitbucket.mlopatkin.android.liblogcat.file.UnrecognizedFormatException;

import com.android.ddmlib.IDevice;

public class Main {

    private DataSource initialSource;
    private MainFrame window;

    public static void main(String[] args) {
        Configuration.forceInit();
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
        window = new MainFrame();
        try {
            initialSource = FileDataSourceFactory.createDataSource(file);
        } catch (UnrecognizedFormatException e) {
            ErrorDialogsHelper.showError("Unrecognized file format for " + file);
        } catch (IOException e) {
            ErrorDialogsHelper.showError("Cannot read " + file);
        }
    }

    Main() {
        window = new MainFrame();
        IDevice device = AdbDeviceManager.getDefaultDevice();
        if (device != null) {
            DeviceDisconnectedNotifier.startWatching(device);
            initialSource = new AdbDataSource(device);
        }
    }

    void start() {
        if (initialSource != null) {
            window.setSource(initialSource);
        } else {
            window.waitForDevice();
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                window.setVisible(true);
            }
        });
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
}
