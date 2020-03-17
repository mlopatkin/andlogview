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

import com.android.ddmlib.IDevice;
import com.android.sdklib.AndroidVersion;
import com.google.common.io.CharStreams;
import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.ProcessListParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;

class AdbPidToProcessConverter {

    private static final Logger logger = Logger.getLogger(AdbPidToProcessConverter.class);

    private static final String PS_COMMAND_LINE = "ps";
    private static final String PS_COMMAND_LINE_API_26 = "ps -A";
    private static final String NO_INFO = "No info available";

    private final ExecutorService backgroundUpdater = Executors.newSingleThreadExecutor();
    private final IDevice device;
    private final String psCmdline;

    private Map<Integer, String> processMap = new ConcurrentHashMap<Integer, String>() {
        @Override
        public String get(Object key) {
            String r = putIfAbsent((Integer) key, NO_INFO);
            if (r == null) {
                scheduleUpdate();
            }
            return super.get(key);
        }
    };

    AdbPidToProcessConverter(IDevice device) {
        this.device = device;
        if (getAndroidVersionWithRetries(device, 10).getApiLevel() >= AndroidVersion.VersionCodes.O) {
            psCmdline = PS_COMMAND_LINE_API_26;
        } else {
            psCmdline = PS_COMMAND_LINE;
        }
    }

    public Map<Integer, String> getMap() {
        return processMap;
    }

    private volatile Future<?> result;

    private synchronized void scheduleUpdate() {
        if (!backgroundUpdater.isShutdown() && (result == null || result.isDone())) {
            ShellInputStream in = new ShellInputStream();
            BackgroundUpdateTask updateTask = new BackgroundUpdateTask(in);
            AdbShellCommand<?> command = new AutoClosingAdbShellCommand(device, psCmdline, in);

            result = backgroundUpdater.submit(updateTask);
            command.start();
        }
    }

    private class BackgroundUpdateTask implements Runnable {

        private BufferedReader in;

        BackgroundUpdateTask(InputStream in) {
            this.in = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        }

        @Override
        public void run() {
            try {
                String line = in.readLine();

                if (!ProcessListParser.isProcessListHeader(line)) {
                    logger.warn("Can't parse header");
                    CharStreams.exhaust(in);
                    return;
                }
                line = in.readLine();
                while (line != null) {
                    Matcher m = ProcessListParser.parseProcessListLine(line);
                    if (m.matches()) {
                        String processName = ProcessListParser.getProcessName(m);
                        int pid = ProcessListParser.getPid(m);
                        processMap.put(pid, processName);
                    } else {
                        logger.debug("Failed to parse line " + line);
                    }
                    line = in.readLine();
                }
            } catch (IOException e) {
                logger.error("Unexpected IO exception", e);
            }

        }

    }

    public void close() {
        backgroundUpdater.shutdown();
    }

    private static AndroidVersion getAndroidVersionWithRetries(IDevice device, int retryCount) {
        AndroidVersion version;
        int numRetry = 0;
        do {
            version = device.getVersion();
        } while (AndroidVersion.DEFAULT.compareTo(version) == 0 && numRetry++ < retryCount);
        logger.debug("Got version " + version + " with " + numRetry + " retries");
        return version;
    }
}
