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

import org.bitbucket.mlopatkin.android.liblogcat.KernelLogParser;
import org.bitbucket.mlopatkin.android.liblogcat.KernelLogRecord;

import com.android.ddmlib.MultiLineReceiver;

/**
 * Receiver for the 'cat /proc/kmsg' output.
 */
public class KernelLogReceiver extends MultiLineReceiver {

    private final AdbDataSource owner;

    public KernelLogReceiver(AdbDataSource owner) {
        this.owner = owner;
    }

    @Override
    public void processNewLines(String[] lines) {
        for (String line : lines) {
            KernelLogRecord record = KernelLogParser.parseRecord(line);
            if (record != null) {
                owner.pushRecord(record);
            }
        }
    }

    private volatile boolean cancelled;

    public void stop() {
        cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

}
