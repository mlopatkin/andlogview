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
package org.bitbucket.mlopatkin.android.liblogcat;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Kind;
import org.bitbucket.mlopatkin.android.logviewer.Configuration;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

public class AdbDataSource implements DataSource {

    private static final Logger logger = Logger.getLogger(AdbDataSource.class);

    private LogRecordDataSourceListener listener;

    private IDevice device;

    public AdbDataSource(final IDevice device) {
        this.device = device;
        for (LogRecord.Kind kind : LogRecord.Kind.values()) {
            if (kind != LogRecord.Kind.UNKNOWN) {
                setUpStream(kind);
            }
        }
    }

    @Override
    public void close() {
        for (AdbLogRecordStream stream : streams) {
            stream.close();
        }
        readingThreadsPool.shutdown();
    }

    @Override
    public PidToProcessConverter getPidToProcessConverter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized void setLogRecordListener(LogRecordDataSourceListener listener) {
        this.listener = listener;
        notifyAll();
    }

    public static AdbDataSource createAdbDataSource() {
        AndroidDebugBridge.init(false);
        AndroidDebugBridge adb = AndroidDebugBridge.createBridge();
        if (adb == null) {
            logger.error("ADB is null");
            return null;
        }
        while (!adb.hasInitialDeviceList())
            ;

        if (adb.getDevices().length > 0) {
            IDevice first = adb.getDevices()[0];
            return new AdbDataSource(first);
        } else {
            logger.info("No device detected");
            return null;
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
                listener.onNewRecord(record);
            }
        });
    }

    private String createLogcatCommandLine(String buffer) {
        StringBuilder b = new StringBuilder(Configuration.adb.commandline());
        b.append(' ').append(Configuration.adb.bufferswitch());
        b.append(' ').append(buffer);
        return b.toString();
    }

    private ExecutorService readingThreadsPool = Executors.newFixedThreadPool(LogRecord.Kind
            .values().length);

    private Set<AdbLogRecordStream> streams = new HashSet<AdbLogRecordStream>();

    private void setUpStream(LogRecord.Kind kind) {
        String bufferName = Configuration.adb.bufferName(kind);
        if (bufferName == null) {
            logger.warn("This kind of log isn't supported by adb source: " + kind);
        }
        final AdbLogRecordStream stream = new AdbLogRecordStream(kind);
        final String commandLine = createLogcatCommandLine(bufferName);
        readingThreadsPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    device.executeShellCommand(commandLine, stream, 0);
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
        });
        streams.add(stream);

    }

    private class AdbLogRecordStream implements IShellOutputReceiver {
        private PipedOutputStream out = new PipedOutputStream();
        private LogRecordStream parser;
        private LogRecord.Kind kind;

        public AdbLogRecordStream(LogRecord.Kind kind) {
            this.kind = kind;
            try {
                PipedInputStream in = new PipedInputStream(out);
                parser = new LogRecordStream(in);
                new PollingThread().start();
            } catch (IOException e) {
                logger.error("Unexpected IO exception", e);
            }
        }

        @Override
        public void addOutput(byte[] data, int offset, int length) {
            try {
                out.write(data, offset, length);
            } catch (IOException e) {
                logger.error("Unexpected IO exception", e);
            }
        }

        @Override
        public void flush() {
            try {
                out.close();
            } catch (IOException e) {
                logger.error("Unexpected IO exception", e);
            }

        }

        @Override
        public boolean isCancelled() {
            return closed;
        }

        private boolean closed = false;

        public void close() {
            closed = true;
        }

        private class PollingThread extends Thread {

            public PollingThread() {
                super("ADB-polling-" + kind);
            }

            @Override
            public void run() {
                waitForListener();
                LogRecord record = parser.next(kind);
                while (!closed && record != null) {
                    pushRecord(record);
                    record = parser.next(kind);
                }
            }
        }

    }

    @Override
    public EnumSet<Kind> getAvailableBuffers() {
        return EnumSet.of(Kind.MAIN, Kind.SYSTEM, Kind.RADIO, Kind.EVENTS);
    }
}
