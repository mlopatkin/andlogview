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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.ProcessListParser;
import org.bitbucket.mlopatkin.android.logviewer.Configuration;

import com.android.ddmlib.IDevice;

class AdbPidToProcessConverter {

    private static final Logger logger = Logger.getLogger(AdbPidToProcessConverter.class);

    private final ExecutorService backgroundUpdater = Executors.newSingleThreadExecutor();

    private final IDevice device;

    private final String PS_COMMAND_LINE = Configuration.adb.psCommandLine();
    private static final String NO_INFO = "No info available";

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

    AdbPidToProcessConverter(IDevice device) {
        this.device = device;
    }

    public Map<Integer, String> getMap() {
        return processMap;
    }

    private volatile Future<?> result;

    private synchronized void scheduleUpdate() {
        if (!backgroundUpdater.isShutdown() && (result == null || result.isDone())) {
            ShellInputStream in = new ShellInputStream();
            BackgroundUpdateTask updateTask = new BackgroundUpdateTask(in);
            AdbShellCommand<?> command = new AutoClosingAdbShellCommand(device, PS_COMMAND_LINE, in);

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

    public void close() {
        backgroundUpdater.shutdown();
    }
}