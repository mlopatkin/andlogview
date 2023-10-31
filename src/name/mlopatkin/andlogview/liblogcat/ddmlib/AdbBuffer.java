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
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.logcat.CollectingHandler;
import name.mlopatkin.andlogview.parsers.logcat.Format;
import name.mlopatkin.andlogview.parsers.logcat.LogcatParsers;
import name.mlopatkin.andlogview.utils.Threads;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class retrieves log records from the device using a background thread
 * and pushes them back to creator.
 */
class AdbBuffer {
    public interface BufferReceiver {
        void pushRecord(LogRecord record);
    }

    private static final Logger logger = Logger.getLogger(AdbBuffer.class);
    private static final Format FORMAT = Format.LONG;

    private final BufferReceiver receiver;
    private final LogRecord.Buffer buffer;
    private final Map<Integer, String> pidToProcess;
    private final ExecutorService executorService;
    private final LogcatCommand command;

    public AdbBuffer(BufferReceiver receiver, Device device, LogRecord.Buffer buffer, LogcatCommand command,
            Map<Integer, String> pidToProcess) {
        this.receiver = receiver;
        this.buffer = buffer;
        this.pidToProcess = pidToProcess;
        this.executorService = Executors.newSingleThreadExecutor(
                Threads.withName(String.format("logcat-%s-%s", buffer, device.getSerialNumber())));
        this.command = command;
    }

    public void start() {
        executorService.execute(this::executeCommand);
    }

    public void close() {
        executorService.shutdownNow();
    }

    private void executeCommand() {
        CollectingHandler parserEventsHandler = new CollectingHandler(buffer, pidToProcess::get) {
            @Override
            protected ParserControl logRecord(LogRecord record) {
                receiver.pushRecord(record);
                return ParserControl.proceed();
            }

            @Override
            public ParserControl unparseableLine(CharSequence line) {
                logger.debug("Non-parsed line: " + line);
                return ParserControl.proceed();
            }
        };
        try (var parser = LogcatParsers.withFormat(FORMAT, parserEventsHandler)) {
            command.readLogStreaming(FORMAT, parser::nextLine);
            if (Thread.currentThread().isInterrupted()) {
                logger.debug("cancelled because of interruption, stopping providing new lines");
            } else {
                // TODO(mlopatkin) if we get there without interruption then logcat has died unexpectedly. This should
                //  be handled somehow because it is not that different from device disconnect. In theory it should be
                //  possible even to recover, e.g. by restarting and waiting for the last read line to come.
                logger.error("logcat streaming completed on its own");
            }
        } catch (InterruptedException e) {
            logger.debug("interrupted, stopping providing new lines");
            Thread.currentThread().interrupt();
        } catch (DeviceGoneException | IOException e) {
            logger.error("Device is gone, closing", e);
        }
    }

    public static Optional<AdbBuffer> tryOpen(BufferReceiver receiver, Device device, LogRecord.Buffer buffer,
            Map<Integer, String> pidToProcess) {
        return LogcatCommand.tryPrepare(device, buffer)
                .filter(LogcatCommand::isBufferPresent)
                .map(command -> {
                    AdbBuffer adbBuffer = new AdbBuffer(receiver, device, buffer, command, pidToProcess);
                    adbBuffer.start();
                    return adbBuffer;
                });
    }

}
