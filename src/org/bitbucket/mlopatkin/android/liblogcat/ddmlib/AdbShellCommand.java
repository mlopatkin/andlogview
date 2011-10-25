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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

/**
 * This class is a wrapper around the
 * {@link IDevice#executeShellCommand(String, IShellOutputReceiver)} method.
 * It allows to detect when the command is finished or failed.
 * <p>
 * Note that the {@link #run()} method will block until the command finishes.
 * Objects of this class are intended to be used in background threads.
 * Alternatively you can use {@link #start()} to run the command in background.
 * <p>
 * This class is immutable.
 * 
 * @param <T>
 *            Subclass of {@link IShellOutputReceiver} to be passed into
 *            template methods to avoid unnecessary casts.
 */
class AdbShellCommand<T extends IShellOutputReceiver> implements Runnable {

    private static final Logger logger = Logger.getLogger(AdbShellCommand.class);

    private final String command;
    private final T receiver;
    private final int timeOut = 0;
    private final IDevice device;

    AdbShellCommand(IDevice device, String commandLine, T outputReceiver) {
        this.device = device;
        this.command = commandLine;
        this.receiver = outputReceiver;
    }

    /**
     * Starts the execution of the command on the device. This method will block
     * until the command finishes.
     */
    @Override
    public void run() {
        try {
            device.executeShellCommand(command, receiver, timeOut);
        } catch (TimeoutException e) {
            logger.warn("Connection to adb failed due to timeout", e);
            onException(e, receiver);
        } catch (AdbCommandRejectedException e) {
            logger.warn("Adb rejected command", e);
            onException(e, receiver);
        } catch (ShellCommandUnresponsiveException e) {
            logger.warn("Shell command unresponsive", e);
            onException(e, receiver);
        } catch (IOException e) {
            logger.warn("IO exception", e);
            onException(e, receiver);
        }
        onCommandFinished(receiver);
        logger.debug("The command '" + command + "' sucessfully terminated");
    }

    private static final Executor backgroundShellCommandExecutor = Executors
            .newSingleThreadExecutor();

    /**
     * Executes the command in the background using internal thread pool.
     */
    public void start() {
        backgroundShellCommandExecutor.execute(this);
    }

    /**
     * Called when one of exceptions occured during shell command's execution.
     * 
     * @param e
     *            exception (one of {@link TimeoutException},
     *            {@link AdbCommandRejectedException},
     *            {@link ShellCommandUnresponsiveException}, {@link IOException}
     *            )
     * @param outputReceiver
     *            {@link IShellOutputReceiver} previously passed to constructor
     */
    protected void onException(Exception e, T outputReceiver) {
    }

    /**
     * Called when execution of the command finishes.
     * 
     * @param outputReceiver
     *            {@link IShellOutputReceiver} previously passed to constructor
     */
    protected void onCommandFinished(T outputReceiver) {
    }
}

/**
 * This subclass closes its outputReceiver on errors. Closing will cause
 * {@link IOException} in a {@link ShellInputStream}'s client.
 */
class AutoClosingAdbShellCommand extends AdbShellCommand<ShellInputStream> {

    AutoClosingAdbShellCommand(IDevice device, String commandLine, ShellInputStream outputReceiver) {
        super(device, commandLine, outputReceiver);
    }

    @Override
    protected void onException(Exception e, ShellInputStream outputReceiver) {
        super.onException(e, outputReceiver);
        outputReceiver.close();
    }
}