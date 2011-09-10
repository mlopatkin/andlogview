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
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordDataSourceListener;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordStream;
import org.bitbucket.mlopatkin.android.liblogcat.PidToProcessConverter;
import org.bitbucket.mlopatkin.android.liblogcat.ProcessListParser;
import org.bitbucket.mlopatkin.android.logviewer.Configuration;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

public class AdbDataSource implements DataSource {

    private static final Logger logger = Logger.getLogger(AdbDataSource.class);

    private LogRecordDataSourceListener listener;

    private IDevice device;
    private AdbPidToProcessConverter converter;
    private EnumSet<Buffer> availableBuffers = EnumSet.noneOf(Buffer.class);

    private void checkBuffers() {
        ShellInputStream shellIn = new ShellInputStream();
        AdbShellCommand listBuffers = new AdbShellCommand("ls /dev/log/", shellIn);
        shellCommandExecutor.execute(listBuffers);
        BufferedReader in = new BufferedReader(new InputStreamReader(shellIn));
        try {
            String line = in.readLine();
            while (line != null) {
                for (Buffer buffer : Buffer.values()) {
                    if (line.equalsIgnoreCase(Configuration.adb.bufferName(buffer))) {
                        availableBuffers.add(buffer);
                    }
                }
                line = in.readLine();
            }
        } catch (IOException e) {
            logger.warn("Exception while reading buffers list", e);
        }
    }

    private void initStreams() {
        checkBuffers();
        for (LogRecord.Buffer buffer : availableBuffers) {
            setUpStream(buffer);
        }
        converter = new AdbPidToProcessConverter();
    }

    public AdbDataSource(final IDevice device) {
        assert device != null;
        assert device.isOnline();
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

    private synchronized void waitForListener() {
        while (listener == null) {
            try {
                wait();
            } catch (InterruptedException e) { // $codepro.audit.disable
                // emptyCatchClause
                // ignore
            }
        }
    }

    private synchronized void pushRecord(final LogRecord record) {
        listener.onNewRecord(record);
    }

    private String createLogcatCommandLine(String buffer) {
        StringBuilder b = new StringBuilder(Configuration.adb.commandline());
        b.append(' ').append(Configuration.adb.bufferswitch());
        b.append(' ').append(buffer);
        return b.toString();
    }

    private Set<AdbBuffer> buffers = new HashSet<AdbBuffer>();

    private void setUpStream(LogRecord.Buffer buffer) {
        String bufferName = Configuration.adb.bufferName(buffer);
        if (bufferName == null) {
            logger.warn("This kind of log isn't supported by adb source: " + buffer);
        }

        final String commandLine = createLogcatCommandLine(bufferName);
        final AdbBuffer adbBuffer = new AdbBuffer(buffer, commandLine);
        buffers.add(adbBuffer);

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
            logger.debug("The command '" + command + "' sucessfully terminated");
        }
    }

    private class AdbBuffer {
        private ShellInputStream shellInput = new ShellInputStream();
        private LogRecordStream in;
        private LogRecord.Buffer buffer;
        private PollingThread pollingThread;
        private Thread shellExecutor;

        public AdbBuffer(LogRecord.Buffer buffer, String commandLine) {
            this.buffer = buffer;
            in = new LogRecordStream(shellInput);
            shellExecutor = new Thread(new AdbShellCommand(commandLine, shellInput),
                    "Shell-reader-" + buffer);
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
                super("ADB-polling-" + buffer);
            }

            @Override
            public void run() {
                waitForListener();
                LogRecord record = in.next(buffer);
                while (!closed && record != null) {
                    pushRecord(record);
                    record = in.next(buffer);
                }
                if (closed) {
                    logger.debug(getName() + " successfully ended");
                } else {
                    logger.warn(getName() + " ends due to null record");
                }

            }

            private boolean closed = false;

            public void close() {
                closed = true;
            }
        }

    }

    @Override
    public EnumSet<Buffer> getAvailableBuffers() {
        return availableBuffers;
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

    @Override
    public String toString() {
        return "ADB connected: " + AdbDeviceManager.getDeviceDisplayName(device);
    }
}
