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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordDataSourceListener;
import org.bitbucket.mlopatkin.android.liblogcat.ProcessListParser;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbBuffer.BufferReceiver;
import org.bitbucket.mlopatkin.android.logviewer.Configuration;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;

public class AdbDataSource implements DataSource, BufferReceiver {

    private static final Logger logger = Logger.getLogger(AdbDataSource.class);

    private LogRecordDataSourceListener listener;

    private IDevice device;
    private AdbPidToProcessConverter converter;
    private EnumSet<Buffer> availableBuffers = EnumSet.noneOf(Buffer.class);

    private void checkBuffers() {
        ShellInputStream shellIn = new ShellInputStream();
        AdbShellCommand<?> listBuffers = new AutoClosingAdbShellCommand(device, "ls /dev/log/",
                shellIn);
        listBuffers.start();
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
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.warn("Exception while closing input stream", e);
            }
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
        AdbDeviceManager.addDeviceChangeListener(deviceListener);
    }

    private boolean closed = false;

    @Override
    public void close() {
        for (AdbBuffer stream : buffers) {
            stream.close();
        }
        backgroundUpdater.shutdown();
        closed = true;
    }

    @Override
    public Map<Integer, String> getPidToProcessConverter() {
        return converter.getMap();
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
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    @Override
    public synchronized void pushRecord(final LogRecord record) {
        waitForListener();
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
        final AdbBuffer adbBuffer = new AdbBuffer(this, device, buffer, commandLine);
        buffers.add(adbBuffer);

    }

    @Override
    public EnumSet<Buffer> getAvailableBuffers() {
        return availableBuffers;
    }

    private ExecutorService backgroundUpdater = Executors.newSingleThreadExecutor();

    private class AdbPidToProcessConverter {

        private final String PS_COMMAND_LINE = Configuration.adb.psCommandLine();
        private final String NO_INFO = "No info available";

        private Map<Integer, String> processMap = new ConcurrentHashMap<Integer, String>() {
            @Override
            public String get(Object key) {
                String r = putIfAbsent((Integer) key, NO_INFO);
                if (r == null) {
                    scheduleUpdate();
                }
                return super.get(key);
            };
        };

        public Map<Integer, String> getMap() {
            return processMap;
        }

        volatile private Future<?> result;

        private synchronized void scheduleUpdate() {
            if (!backgroundUpdater.isShutdown() && (result == null || result.isDone())) {
                ShellInputStream in = new ShellInputStream();
                BackgroundUpdateTask updateTask = new BackgroundUpdateTask(in);
                AdbShellCommand<?> command = new AutoClosingAdbShellCommand(device,
                        PS_COMMAND_LINE, in);

                result = backgroundUpdater.submit(updateTask);
                command.start();
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
                        processMap.put(pid, processName);
                        line = in.readLine();
                    }
                } catch (IOException e) {
                    logger.error("Unexpected IO exception", e);
                }

            }

        }
    }

    @Override
    public boolean reset() {
        converter.getMap().clear();
        return false;
    }

    @Override
    public String toString() {
        if (!closed) {
            return "Device: " + AdbDeviceManager.getDeviceDisplayName(device);
        } else {
            return "Disconnected device: " + AdbDeviceManager.getDeviceDisplayName(device);
        }
    }

    private IDeviceChangeListener deviceListener = new AdbDeviceManager.AbstractDeviceListener() {
        @Override
        public void deviceDisconnected(IDevice device) {
            if (device == AdbDataSource.this.device) {
                close();
                AdbDeviceManager.removeDeviceChangeListener(this);
            }

        };

        @Override
        public void deviceChanged(IDevice device, int changeMask) {
            if (device == AdbDataSource.this.device && (changeMask & IDevice.CHANGE_STATE) != 0
                    && device.isOffline()) {
                close();
                AdbDeviceManager.removeDeviceChangeListener(this);
            }
        };
    };
}
