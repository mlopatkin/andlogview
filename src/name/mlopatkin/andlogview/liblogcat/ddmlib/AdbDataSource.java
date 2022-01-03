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
package name.mlopatkin.andlogview.liblogcat.ddmlib;

import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.liblogcat.DataSource;
import name.mlopatkin.andlogview.liblogcat.Field;
import name.mlopatkin.andlogview.liblogcat.LogRecord;
import name.mlopatkin.andlogview.liblogcat.LogRecord.Buffer;
import name.mlopatkin.andlogview.liblogcat.RecordListener;
import name.mlopatkin.andlogview.liblogcat.SourceMetadata;
import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbBuffer.BufferReceiver;

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class AdbDataSource implements DataSource, BufferReceiver {
    private static final Logger logger = Logger.getLogger(AdbDataSource.class);

    private @Nullable RecordListener<LogRecord> listener;

    private final AdbDeviceManager deviceManager;
    private final IDevice device;
    private final AdbPidToProcessConverter converter;
    private final EnumSet<Buffer> availableBuffers = EnumSet.noneOf(Buffer.class);
    private final SourceMetadata sourceMetadata;

    public AdbDataSource(AdbDeviceManager deviceManager, IDevice device) {
        this.deviceManager = deviceManager;
        assert device != null;
        assert device.isOnline();
        this.device = device;
        converter = new AdbPidToProcessConverter(this.device);
        for (Buffer buffer : Buffer.values()) {
            setUpStream(buffer);
        }
        deviceManager.addDeviceChangeListener(deviceListener);
        sourceMetadata = new AdbSourceMetadata(device);
    }

    private boolean closed = false;

    @Override
    public void close() {
        for (AdbBuffer stream : buffers) {
            stream.close();
        }
        converter.close();
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
        assert listener != null;
        listener.addRecord(record);
    }

    private String createLogcatCommandLine(String buffer) {
        StringBuilder b = new StringBuilder(Configuration.adb.commandline());
        b.append(' ').append(Configuration.adb.bufferswitch());
        b.append(' ').append(buffer);
        return b.toString();
    }

    private Set<AdbBuffer> buffers = new HashSet<>();

    private boolean isBufferHere(String bufferName) {
        String cmd = "logcat -b " + bufferName + " -s -d  > /dev/null 2> /dev/null || echo 0";
        return SyncAdbShellCommand.execute(device, cmd).isEmpty();
    }

    private void setUpStream(LogRecord.Buffer buffer) {
        String bufferName = Configuration.adb.bufferName(buffer);
        if (bufferName == null) {
            logger.warn("This kind of log isn't supported by adb source: " + buffer);
        }

        // check buffer for existence first
        if (!isBufferHere(bufferName)) {
            return;
        }
        availableBuffers.add(buffer);
        final String commandLine = createLogcatCommandLine(bufferName);
        final AdbBuffer adbBuffer = new AdbBuffer(this, device, buffer, commandLine, getPidToProcessConverter());
        buffers.add(adbBuffer);
    }

    @Override
    public EnumSet<Buffer> getAvailableBuffers() {
        return availableBuffers;
    }

    @Override
    public Set<Field> getAvailableFields() {
        return EnumSet.allOf(Field.class);
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

    @Override
    public SourceMetadata getMetadata() {
        return sourceMetadata;
    }

    private IDeviceChangeListener deviceListener = new AdbDeviceManager.AbstractDeviceListener() {
        @Override
        public void deviceDisconnected(IDevice device) {
            if (device == AdbDataSource.this.device) {
                close();
                deviceManager.removeDeviceChangeListener(this);
            }
        }

        @Override
        public void deviceChanged(IDevice device, int changeMask) {
            if (device == AdbDataSource.this.device && (changeMask & IDevice.CHANGE_STATE) != 0 && device.isOffline()) {
                close();
                deviceManager.removeDeviceChangeListener(this);
            }
        }
    };
}
