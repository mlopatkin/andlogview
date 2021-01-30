/*
 * Copyright 2020 Mikhail Lopatkin
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

import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.google.common.io.ByteStreams;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

class CommandImpl implements Command {
    private static final Logger logger = Logger.getLogger(CommandImpl.class);

    private final IDevice device;
    private final List<String> commandLine;

    private OutputStream stdout = ByteStreams.nullOutputStream();
    private OutputStream stderr = ByteStreams.nullOutputStream();

    public CommandImpl(IDevice device, List<String> commandLine) {
        this.device = device;
        this.commandLine = commandLine;
    }

    @Override
    public Command.Result execute() throws IOException, InterruptedException, DeviceGoneException {
        try (DeviceTempFile tempStdout = new DeviceTempFile(device);
                DeviceTempFile tempStderr = new DeviceTempFile(device)) {
            // TODO(mlopatkin) Try to implement shell api v2 on top of ddmlib. Shell v2 allows to get stdout and stderr
            //   along with the exit code without the need to resort to the shell.
            // TODO(mlopatkin) Add shell escaping here
            // TODO(mlopatkin) Add timeouts API here
            String commandLineWithRedirects =
                    String.format("(%s) >%s 2>%s; echo $?", String.join(" ", commandLine), tempStdout.getPath(),
                            tempStderr.getPath());
            CollectingOutputReceiver exitCodeReceiver = new CollectingOutputReceiver();
            DeviceUtils.executeShellCommand(device, commandLineWithRedirects, exitCodeReceiver);
            String exitCode = exitCodeReceiver.getOutput().trim();
            logger.debug("exit code output=" + exitCode);
            tempStdout.copyContentsTo(stdout);
            tempStderr.copyContentsTo(stderr);

            return new Result(exitCode);
        }
    }

    @Override
    public Command redirectOutput(OutputStream stdout) {
        this.stdout = stdout;
        return this;
    }

    @Override
    public Command redirectError(OutputStream stderr) {
        this.stderr = stderr;
        return this;
    }

    private static class Result implements Command.Result {
        private final String exitCode;

        public Result(String exitCode) {
            this.exitCode = exitCode;
        }

        @Override
        public String getExitCode() {
            return exitCode;
        }
    }
}
