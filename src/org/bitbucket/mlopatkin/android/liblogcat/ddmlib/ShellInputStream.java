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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.log4j.Logger;

import com.android.ddmlib.IShellOutputReceiver;

/**
 * This class represents the output of the running adb shell command.
 * Note that attempt to read shell command output in the same thread that
 * started it will lead to deadlock.
 */
class ShellInputStream extends PipedInputStream implements IShellOutputReceiver {
    private static final Logger logger = Logger.getLogger(ShellInputStream.class);

    private PipedOutputStream out = new PipedOutputStream();
    private boolean closed = false;

    public ShellInputStream() {
        try {
            out.connect(this);
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

    @Override
    public void close() {
        closed = true;
        try {
            super.close();
            out.close();
        } catch (IOException e) {
            logger.error("Unexpected IO exception", e);
        }
    }
}
