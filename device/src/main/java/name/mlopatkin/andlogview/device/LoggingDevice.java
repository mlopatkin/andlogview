/*
 * Copyright 2021 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.device;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Delegating IDevice wrapper that logs essential commands.
 */
class LoggingDevice extends DelegatingDevice {
    private static final Logger logger = Logger.getLogger(LoggingDevice.class);

    private final String logPrefix;

    public LoggingDevice(IDevice inner) {
        super(inner);
        logPrefix = "[" + inner.getSerialNumber() + "]: ";
    }

    @Override
    @Deprecated
    public void executeShellCommand(String command, IShellOutputReceiver receiver, int maxTimeToOutputResponse)
            throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        debug(command);
        super.executeShellCommand(command, receiver, maxTimeToOutputResponse);
    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver)
            throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        debug(command);
        super.executeShellCommand(command, receiver);
    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver, long maxTimeToOutputResponse,
            TimeUnit maxTimeUnits)
            throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        debug(command);
        super.executeShellCommand(command, receiver, maxTimeToOutputResponse, maxTimeUnits);
    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver, long maxTimeout,
            long maxTimeToOutputResponse, TimeUnit maxTimeUnits)
            throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        debug(command);
        super.executeShellCommand(command, receiver, maxTimeout, maxTimeToOutputResponse, maxTimeUnits);
    }

    private void debug(String message) {
        logger.debug(logPrefix + message);
    }
}
