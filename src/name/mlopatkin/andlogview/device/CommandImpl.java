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

import name.mlopatkin.andlogview.device.OutputTarget.ForStderr;
import name.mlopatkin.andlogview.device.OutputTarget.ForStdout;
import name.mlopatkin.andlogview.device.OutputTarget.StdStream;

import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.List;

class CommandImpl implements Command {
    private static final Logger logger = Logger.getLogger(CommandImpl.class);

    private final IDevice device;
    private final List<String> commandLine;

    private @Nullable ForStdout configuredStdout;
    private @Nullable ForStderr configuredStderr;

    public CommandImpl(IDevice device, List<String> commandLine) {
        this.device = device;
        this.commandLine = commandLine;
    }

    @Override
    public Command.Result execute() throws IOException, InterruptedException, DeviceGoneException {
        // The only way for the user to grab the output at this point is to supply an output stream for us. Therefore,
        // if nothing is set then the output is discarded, probably the user wants a side effect.
        ForStdout stdout = getRedirectWithDefault(this.configuredStdout, OutputTarget.toDevNull());
        ForStderr stderr = getRedirectWithDefault(this.configuredStderr, OutputTarget.toDevNull());

        try (OutputTarget.OutputHandle tempStdout = stdout.openOutput(device);
                OutputTarget.OutputHandle tempStderr = stderr.openOutput(device)) {
            // TODO(mlopatkin) Try to implement shell api v2 on top of ddmlib. Shell v2 allows to get stdout and stderr
            //   along with the exit code without the need to resort to the shell.
            // TODO(mlopatkin) Add shell escaping here
            // TODO(mlopatkin) Add timeouts API here
            String commandLineWithRedirects =
                    String.format("(%s) %s %s; echo $?",
                            formatCommandLine(),
                            tempStdout.getRedirectString(StdStream.STDOUT),
                            tempStderr.getRedirectString(StdStream.STDERR));
            CollectingOutputReceiver exitCodeReceiver = new CollectingOutputReceiver();
            DeviceUtils.executeShellCommand(device, commandLineWithRedirects, exitCodeReceiver);
            String exitCode = exitCodeReceiver.getOutput().trim();
            logger.debug("exit code output=" + exitCode);

            return new Result(exitCode);
        }
    }

    @Override
    public void executeStreaming(LineReceiver receiver) throws InterruptedException, IOException, DeviceGoneException {
        // We grab both stdout and stderr unless the user specified anything else. They might redirect the output as
        // they want but the receiver will receive nothing in this case. However, this might be useful to discard stderr
        // while keeping stdout.
        ForStdout stdout = getRedirectWithDefault(this.configuredStdout, OutputTarget.noRedirection());
        // I vaguely remember that ddmlib provides stderr by default as well. Adding an explicit "toStdout" redirection
        // makes it impossible to discard stdout while keeping stderr.
        ForStderr stderr = getRedirectWithDefault(this.configuredStderr, OutputTarget.noRedirection());

        try (OutputTarget.OutputHandle tempStdout = stdout.openOutput(device);
                OutputTarget.OutputHandle tempStderr = stderr.openOutput(device)) {
            // TODO(mlopatkin) Try to implement shell api v2 on top of ddmlib. Shell v2 allows to get stdout and stderr
            //   along with the exit code without the need to resort to the shell.
            String commandLineWithRedirects =
                    String.format("(%s) %s %s",
                            formatCommandLine(),
                            tempStdout.getRedirectString(StdStream.STDOUT),
                            tempStderr.getRedirectString(StdStream.STDERR));
            MultiLineReceiver outputReceiver = new MultiLineReceiver() {
                @Override
                public void processNewLines(String[] lines) {
                    for (String line : lines) {
                        receiver.nextLine(line);
                    }
                }

                @Override
                public boolean isCancelled() {
                    return false;
                }
            };
            DeviceUtils.executeShellCommand(device, commandLineWithRedirects, outputReceiver);
            receiver.complete();
        }
    }

    @Override
    public Command redirectOutput(ForStdout target) {
        configuredStdout = target;
        return this;
    }

    @Override
    public Command redirectError(ForStderr target) {
        configuredStderr = target;
        return this;
    }

    private String formatCommandLine() {
        // TODO(https://github.com/mlopatkin/andlogview/issues/168) Add shell escaping here
        return String.join(" ", commandLine);
    }

    private <T extends ForStderr> T getRedirectWithDefault(@Nullable T redirect, T defRedirect) {
        if (redirect != null) {
            return redirect;
        }
        return defRedirect;
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
