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

import name.mlopatkin.andlogview.device.Device;
import name.mlopatkin.andlogview.device.DeviceGoneException;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.ps.PsParseEventsHandler;
import name.mlopatkin.andlogview.parsers.ps.PsPushParser;
import name.mlopatkin.andlogview.thirdparty.device.AndroidVersionCodes;
import name.mlopatkin.andlogview.utils.Threads;

import com.google.errorprone.annotations.concurrent.GuardedBy;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class AdbPidToProcessConverter {
    private static final Logger logger = Logger.getLogger(AdbPidToProcessConverter.class);

    private static final String[] PS_COMMAND_LINE = {"ps"};
    private static final String[] PS_COMMAND_LINE_API_26 = {"ps", "-A"};
    private static final String NO_INFO = "No info available";

    private final ExecutorService backgroundUpdater;
    private final Device device;
    private final String[] psCmdline;
    @GuardedBy("this")
    private @Nullable Future<?> result;

    private final ConcurrentHashMap<Integer, String> processMap = new ConcurrentHashMap<>() {
        @Override
        public @Nullable String get(Object key) {
            String r = putIfAbsent((Integer) key, NO_INFO);
            if (r == null) {
                scheduleUpdate();
            }
            return super.get(key);
        }
    };

    AdbPidToProcessConverter(Device device) {
        this.device = device;
        if (device.getApiLevel() >= AndroidVersionCodes.O) {
            psCmdline = PS_COMMAND_LINE_API_26;
        } else {
            psCmdline = PS_COMMAND_LINE;
        }
        backgroundUpdater =
                Executors.newSingleThreadExecutor(Threads.withName("ps-reader-" + device.getSerialNumber()));
    }

    public Map<Integer, String> getMap() {
        return processMap;
    }

    private synchronized void scheduleUpdate() {
        if (!backgroundUpdater.isShutdown() && (result == null || result.isDone())) {
            result = backgroundUpdater.submit(this::update);
        }
    }

    private void update() {
        PsParseEventsHandler eventsHandler = new PsParseEventsHandler() {
            @Override
            public ParserControl processLine(int pid, String processName) {
                processMap.put(pid, processName);
                return ParserControl.proceed();
            }

            @Override
            public ParserControl unparseableLine(CharSequence line) {
                logger.debug("Failed to parse line: " + line);
                return ParserControl.proceed();
            }
        };

        try (PsPushParser<?> pushParser = new PsPushParser<>(eventsHandler)) {
            device.command(psCmdline).executeStreaming(pushParser::nextLine);
        } catch (DeviceGoneException | IOException e) {
            logger.error("Unexpected IO exception", e);
        } catch (InterruptedException e) {
            // do nothing, just return.
        }
    }

    public void close() {
        backgroundUpdater.shutdown();
    }
}
