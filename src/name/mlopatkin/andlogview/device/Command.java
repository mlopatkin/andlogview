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

import java.io.IOException;
import java.io.OutputStream;

/**
 * The command that runs on the device. It is similar to the {@link Process} class but slightly optimised for common
 * uses. This API is incubating and subject to change.
 */
public interface Command {
    /**
     * A callback interface to receive an output of the command line-by-line. The methods are invoked on the thread that
     * executes {@link #executeStreaming(LineReceiver)} method.
     */
    interface LineReceiver {
        /**
         * Called every time a line is received from the command.
         *
         * @param line the line
         */
        void nextLine(String line);

        /**
         * Called when the command completes after all output has be sent.
         */
        default void complete() {
        }
    }

    /**
     * Redirects the standard output stream of the command somewhere else, e.g. to discard it or to copy it into some
     * OutputStream.
     *
     * @param target the target for redirection
     * @return this command
     */
    Command redirectOutput(OutputTarget.ForStdout target);

    /**
     * Redirects the standard output stream of the command to copy it into the OutputStream. This method doesn't close
     * the stream.
     *
     * @param stdout the stream to copy standard output stream into
     * @return this command
     */
    default Command redirectOutput(OutputStream stdout) {
        redirectOutput(OutputTarget.toOutputStream(stdout));
        return this;
    }

    /**
     * Redirects the standard error stream of the command somewhere else, e.g. to discard it or to copy it into some
     * OutputStream. It is also possible to combine it with standard output stream.
     *
     * @param target the target for redirection
     * @return this command
     */
    Command redirectError(OutputTarget.ForStderr target);

    /**
     * Redirects the standard error stream of the command to copy it into the OutputStream. This method doesn't close
     * the stream.
     *
     * @param stderr the stream to copy standard error stream into
     * @return this command
     */
    default Command redirectError(OutputStream stderr) {
        redirectError(OutputTarget.toOutputStream(stderr));
        return this;
    }

    /**
     * Executes a command and returns a return code. This method blocks until the command completes.
     *
     * @return the return code in {@code Result} object
     * @throws InterruptedException if the thread is interrupted while running a command
     * @throws IOException if the command failed to write something when redirecting the output
     * @throws DeviceGoneException when device connection is broken (device disconnected or ADB server died)
     */
    Result execute() throws InterruptedException, IOException, DeviceGoneException;

    /**
     * Executes a command and pushes its output into the provided receiver. This method blocks until the command
     * completes. The redirections are still respected, so beware of accidentally redirecting output somewhere else.
     * Note that you have to use {@link Thread#interrupt()} from some other thread to stop a command that never stops on
     * its own.
     *
     * @param receiver the receiver to get command's output line-by-line
     *
     * @throws InterruptedException if the thread is interrupted while running a command
     * @throws IOException if the command failed to write something when redirecting the output
     * @throws DeviceGoneException when device connection is broken (device disconnected or ADB server died)
     */
    void executeStreaming(LineReceiver receiver) throws InterruptedException, IOException, DeviceGoneException;

    /**
     * The process' exit code
     */
    interface Result {
        /**
         * @return the raw exit code as string
         */
        String getExitCode();

        /**
         * @return {@code true} if the process completed normally (i.e. with exit code 0)
         */
        default boolean isSuccessful() {
            return "0".endsWith(getExitCode().trim());
        }
    }
}
