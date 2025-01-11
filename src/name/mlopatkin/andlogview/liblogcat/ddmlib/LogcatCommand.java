/*
 * Copyright 2023 the Andlogview authors
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
import name.mlopatkin.andlogview.device.Command;
import name.mlopatkin.andlogview.device.Device;
import name.mlopatkin.andlogview.device.DeviceGoneException;
import name.mlopatkin.andlogview.device.OutputTarget;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.parsers.logcat.Format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents various flavors of {@code adb logcat -b buffer} command.
 */
class LogcatCommand {
    private static final Logger logger = LoggerFactory.getLogger(LogcatCommand.class);

    private final Device device;
    private final String bufferName;

    private LogcatCommand(Device device, String bufferName) {
        this.device = device;
        this.bufferName = bufferName;
    }

    /**
     * Checks if the buffer is available on the device.
     *
     * @return {@code true} if the buffer can be opened, {@code false} otherwise
     */
    public boolean isBufferPresent() {
        if (bufferName == null) {
            return false;
        }
        try {
            // Try to dump buffer contents (-d) while filtering everything out (-s). In an essence, we only get an exit
            // code from the logcat run - 0 if the buffer is available or something else if it is not.
            Command.Result checkResult = device.command("logcat", "-b", bufferName, "-s", "-d")
                    .redirectOutput(OutputTarget.toDevNull())
                    .redirectError(OutputTarget.toDevNull())
                    .execute();
            return checkResult.isSuccessful();
        } catch (InterruptedException e) {
            // This is unlikely, but let's play safe there.
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException | DeviceGoneException e) {
            logger.error("Failed to retrieve the buffer status from the device", e);
            return false;
        }
    }

    /**
     * Starts reading the log in {@code format} format. Lines are forwarded into the provided receiver. This method
     * blocks until the current thread is interrupted or I/O error occurs.
     *
     * @param format the log format, must be known to logcat
     * @param lineReceiver the receiver to get lines, is called on this thread
     * @throws DeviceGoneException if the device is disconnected
     * @throws IOException if other I/O error happened
     * @throws InterruptedException if the thread is interrupted
     */
    public void readLogStreaming(Format format, Consumer<? super String> lineReceiver)
            throws DeviceGoneException, IOException, InterruptedException {
        device.command("logcat", "-b", bufferName, "-v", format.getCmdFormatName())
                .executeStreaming(lineReceiver::accept);
    }

    /**
     * Tries to prepare the command. May return an empty optional if the provided buffer cannot be read by logcat.
     *
     * @param device the device to get the logs from
     * @param buffer the buffer to read the logs from
     * @return optional with the prepared command
     */
    public static Optional<LogcatCommand> tryPrepare(Device device, LogRecord.Buffer buffer) {
        var bufferName = Configuration.adb.bufferName(buffer);
        if (bufferName == null) {
            return Optional.empty();
        }
        return Optional.of(new LogcatCommand(device, bufferName));
    }
}
