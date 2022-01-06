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
import name.mlopatkin.andlogview.device.AdbDevice;
import name.mlopatkin.andlogview.device.AdbDeviceList;
import name.mlopatkin.andlogview.device.DeviceChangeObserver;
import name.mlopatkin.andlogview.liblogcat.DataSource;
import name.mlopatkin.andlogview.liblogcat.Field;
import name.mlopatkin.andlogview.liblogcat.LogRecord;
import name.mlopatkin.andlogview.liblogcat.LogRecord.Buffer;
import name.mlopatkin.andlogview.liblogcat.RecordListener;
import name.mlopatkin.andlogview.liblogcat.SourceMetadata;
import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbBuffer.BufferReceiver;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.ScopedObserver;
import name.mlopatkin.andlogview.utils.events.Subject;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class AdbDataSource implements DataSource, BufferReceiver {
    /**
     * The reason for the data source to become invalid.
     */
    public enum InvalidationReason {
        OFFLINE, DISCONNECT
    }

    /**
     * The interface receives updates about the state of this data source.
     */
    public interface StateObserver {
        /**
         * Called when the data source is no longer valid and will not produce any useful data anymore. The invalidation
         * doesn't include calling {@link AdbDataSource#close()} as this is a part of the normal lifecycle. There is no
         * need to close the data source after invalidation, it closes itself automatically.
         *
         * @param reason the reason for invalidation
         */
        void onDataSourceInvalidated(InvalidationReason reason);

        /**
         * Called when the data source is closed whether normally or because of invalidation.
         */
        default void onDataSourceClosed() {
        }
    }

    private static final Logger logger = Logger.getLogger(AdbDataSource.class);

    private final AdbDevice device;

    private final AdbPidToProcessConverter converter;
    private final EnumSet<Buffer> availableBuffers = EnumSet.noneOf(Buffer.class);
    private final SourceMetadata sourceMetadata;
    private final ScopedObserver deviceChangeObserver;
    private final Subject<StateObserver> stateObservers = new Subject<>();

    private @Nullable RecordListener<LogRecord> listener;
    private boolean closed = false;

    public AdbDataSource(AdbDevice device, AdbDeviceList deviceList) {
        assert device != null;
        assert device.isOnline();
        this.device = device;
        converter = new AdbPidToProcessConverter(this.device.getIDevice());
        for (Buffer buffer : Buffer.values()) {
            setUpStream(buffer);
        }
        sourceMetadata = new AdbSourceMetadata(device.getIDevice());
        deviceChangeObserver = deviceList.asObservable().addScopedObserver(new DeviceChangeObserver() {
            @Override
            public void onDeviceDisconnected(AdbDevice device) {
                logger.debug("Device " + device.getSerialNumber() + " was disconnected, closing the source");
                invalidateAndClose(InvalidationReason.DISCONNECT);
            }

            @Override
            public void onDeviceChanged(AdbDevice device) {
                if (!device.isOnline()) {
                    logger.debug("Device " + device.getSerialNumber() + " is offline, closing the source");
                    invalidateAndClose(InvalidationReason.OFFLINE);
                }
            }
        }.scopeToSingleDevice(device));
    }

    @Override
    public void close() {
        for (AdbBuffer stream : buffers) {
            stream.close();
        }
        converter.close();
        deviceChangeObserver.close();
        closed = true;
        for (StateObserver stateObserver : stateObservers) {
            stateObserver.onDataSourceClosed();
        }
    }

    private void invalidateAndClose(InvalidationReason reason) {
        for (StateObserver stateObserver : stateObservers) {
            stateObserver.onDataSourceInvalidated(reason);
        }
        close();
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
        return SyncAdbShellCommand.execute(device.getIDevice(), cmd).isEmpty();
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
        final AdbBuffer adbBuffer =
                new AdbBuffer(this, device.getIDevice(), buffer, commandLine, getPidToProcessConverter());
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
        String deviceDisplayName = device.getDisplayName();
        if (!closed) {
            return "Device: " + deviceDisplayName;
        } else {
            return "Disconnected device: " + deviceDisplayName;
        }
    }

    @Override
    public SourceMetadata getMetadata() {
        return sourceMetadata;
    }

    public Observable<StateObserver> asStateObservable() {
        return stateObservers.asObservable();
    }
}
