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
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.KernelLogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.RecordListener;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbBuffer.BufferReceiver;
import org.bitbucket.mlopatkin.android.logviewer.Configuration;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;

public class AdbDataSource implements DataSource, BufferReceiver {

    private static final Logger logger = Logger.getLogger(AdbDataSource.class);

    private RecordListener<LogRecord> listener;
    private RecordListener<KernelLogRecord> kernelListener;

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
        converter = new AdbPidToProcessConverter(device);
    }

    private final KernelLogReceiver kernelReceiver = new KernelLogReceiver(this);

    private void initKernelLog() {
        AdbShellCommand<IShellOutputReceiver> command = new AdbShellCommand<IShellOutputReceiver>(
                device, "cat /proc/kmsg", kernelReceiver);
        new Thread(command).start();
    }

    public AdbDataSource(final IDevice device) {
        assert device != null;
        assert device.isOnline();
        this.device = device;
        initStreams();
        initKernelLog();
        AdbDeviceManager.addDeviceChangeListener(deviceListener);
    }

    private boolean closed = false;

    @Override
    public void close() {
        for (AdbBuffer stream : buffers) {
            stream.close();
        }
        converter.close();
        kernelReceiver.stop();
        closed = true;
    }

    @Override
    public Map<Integer, String> getPidToProcessConverter() {
        return converter.getMap();
    }

    @Override
    public synchronized void setLogRecordListener(RecordListener<LogRecord> listener) {
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
        listener.addRecord(record);
    }

    public synchronized void pushRecord(final KernelLogRecord record) {
        while (kernelListener == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                return;
            }
        }
        kernelListener.addRecord(record);
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

    @Override
    public synchronized boolean setKernelLogListener(RecordListener<KernelLogRecord> listener) {
        kernelListener = listener;
        notifyAll();
        return true;
    }
}
