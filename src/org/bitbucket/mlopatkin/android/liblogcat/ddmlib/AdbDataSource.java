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
package org.bitbucket.mlopatkin.android.liblogcat.ddmlib;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Kind;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordDataSourceListener;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordStream;
import org.bitbucket.mlopatkin.android.liblogcat.PidToProcessConverter;
import org.bitbucket.mlopatkin.android.liblogcat.ProcessListParser;
import org.bitbucket.mlopatkin.android.logviewer.Configuration;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

public class AdbDataSource implements DataSource {

    private static final Logger logger = Logger.getLogger(AdbDataSource.class);

    private LogRecordDataSourceListener listener;

    private IDevice device;
    private AdbPidToProcessConverter converter;

    public AdbDataSource() {
        AdbDeviceManager.addDeviceChangeListener(changeListener);
    }

    private IDeviceChangeListener changeListener = new AdbDeviceManager.AbstractDeviceListener() {

        @Override
        public void deviceConnected(IDevice device) {
            if (AdbDataSource.this.device == null) {
                AdbDataSource.this.device = device;
                initStreams();
            }
        }
    };

    private void initStreams() {
        for (LogRecord.Kind kind : LogRecord.Kind.values()) {
            if (kind != LogRecord.Kind.UNKNOWN) {
                setUpStream(kind);
            }
        }
        converter = new AdbPidToProcessConverter();
    }

    public AdbDataSource(final IDevice device) {
        this.device = device;
        initStreams();
    }

    @Override
    public void close() {
        for (AdbBuffer stream : buffers) {
            stream.close();
        }
        backgroundUpdater.shutdown();
        shellCommandExecutor.shutdown();
    }

    @Override
    public PidToProcessConverter getPidToProcessConverter() {
        return converter;
    }

    @Override
    public synchronized void setLogRecordListener(LogRecordDataSourceListener listener) {
        this.listener = listener;
        notifyAll();
    }

    public static AdbDataSource createAdbDataSource() {
        IDevice device = AdbDeviceManager.getDefaultDevice();
        if (device != null) {
            return new AdbDataSource(device);
        } else {
            logger.info("No device detected");
            return new AdbDataSource();
        }
    }

    private synchronized void waitForListener() {
        while (listener == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private synchronized void pushRecord(final LogRecord record) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                listener.onNewRecord(record, true);
            }
        });
    }

    private String createLogcatCommandLine(String buffer) {
        StringBuilder b = new StringBuilder(Configuration.adb.commandline());
        b.append(' ').append(Configuration.adb.bufferswitch());
        b.append(' ').append(buffer);
        return b.toString();
    }

    private Set<AdbBuffer> buffers = new HashSet<AdbBuffer>();

    private void setUpStream(LogRecord.Kind kind) {
        String bufferName = Configuration.adb.bufferName(kind);
        if (bufferName == null) {
            logger.warn("This kind of log isn't supported by adb source: " + kind);
        }

        final String commandLine = createLogcatCommandLine(bufferName);
        final AdbBuffer buffer = new AdbBuffer(kind, commandLine);
        buffers.add(buffer);

    }

    private class AdbShellCommand implements Runnable {

        private String command;
        private IShellOutputReceiver receiver;
        private int timeOut = 0;

        AdbShellCommand(String commandLine, IShellOutputReceiver outputReceiver) {
            this.command = commandLine;
            this.receiver = outputReceiver;
        }

        @Override
        public void run() {
            try {
                device.executeShellCommand(command, receiver, timeOut);
            } catch (TimeoutException e) {
                logger.warn("Connection to adb failed due to timeout", e);
            } catch (AdbCommandRejectedException e) {
                logger.warn("Adb rejected command", e);
            } catch (ShellCommandUnresponsiveException e) {
                logger.warn("Shell command unresponsive", e);
            } catch (IOException e) {
                logger.warn("IO exception", e);
            }
        }
    }

    private class AdbBuffer {
        private ShellInputStream shellInput = new ShellInputStream();
        private LogRecordStream in;
        private LogRecord.Kind kind;
        private PollingThread pollingThread;
        private Thread shellExecutor;

        public AdbBuffer(LogRecord.Kind kind, String commandLine) {
            this.kind = kind;
            in = new LogRecordStream(shellInput);
            shellExecutor = new Thread(new AdbShellCommand(commandLine, shellInput),
                    "Shell-reader-" + kind);
            pollingThread = new PollingThread();
            shellExecutor.start();
            pollingThread.start();
        }

        void close() {
            pollingThread.close();
            shellInput.close();
        }

        private class PollingThread extends Thread {

            public PollingThread() {
                super("ADB-polling-" + kind);
            }

            @Override
            public void run() {
                waitForListener();
                LogRecord record = in.next(kind);
                while (!closed && record != null) {
                    pushRecord(record);
                    record = in.next(kind);
                }

            }

            private boolean closed = false;

            public void close() {
                closed = true;
            }
        }

    }

    @Override
    public EnumSet<Kind> getAvailableBuffers() {
        return EnumSet.of(Kind.MAIN, Kind.SYSTEM, Kind.RADIO, Kind.EVENTS);
    }

    private ExecutorService backgroundUpdater = Executors.newSingleThreadExecutor();
    private ExecutorService shellCommandExecutor = Executors.newSingleThreadExecutor();

    private class AdbPidToProcessConverter extends PidToProcessConverter {

        private final String PS_COMMAND_LINE = Configuration.adb.psCommandLine();

        @Override
        public synchronized String getProcessName(int pid) {
            String name = super.getProcessName(pid);
            if (name == null) {
                scheduleUpdate();
            }
            return name;
        }

        private Future<?> result;

        private void scheduleUpdate() {
            if (result == null || result.isDone()) {
                ShellInputStream in = new ShellInputStream();
                BackgroundUpdateTask updateTask = new BackgroundUpdateTask(in);
                AdbShellCommand command = new AdbShellCommand(PS_COMMAND_LINE, in);
                result = backgroundUpdater.submit(updateTask);
                shellCommandExecutor.execute(command);
            }
        }

        private class BackgroundUpdateTask implements Runnable {

            private BufferedReader in;

            BackgroundUpdateTask(InputStream in) {
                this.in = new BufferedReader(new InputStreamReader(in));
            }

            @Override
            public void run() {
                try {
                    String line = in.readLine();

                    if (!ProcessListParser.isProcessListHeader(line)) {
                        return;
                    }
                    line = in.readLine();
                    while (line != null) {
                        Matcher m = ProcessListParser.parseProcessListLine(line);
                        String processName = ProcessListParser.getProcessName(m);
                        int pid = ProcessListParser.getPid(m);
                        put(pid, processName);
                        line = in.readLine();
                    }
                } catch (IOException e) {
                    logger.error("Unexpected IO exception", e);
                }

            }

        }
    }

    @Override
    public void reset() {
        // do nothing
    }

}
