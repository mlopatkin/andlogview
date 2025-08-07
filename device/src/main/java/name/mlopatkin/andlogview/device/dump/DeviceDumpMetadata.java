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

package name.mlopatkin.andlogview.device.dump;

import com.google.common.collect.ImmutableList;

import org.jspecify.annotations.Nullable;

import java.util.List;

/** Serialized form of meta-information about the dump. */
class DeviceDumpMetadata {
    final String name;
    final String product;
    final String buildFingerprint;
    final String apiVersion;
    // Other device metadata? Model/Builder?
    final List<CommandOutput> commandOutputs;

    public DeviceDumpMetadata(String name, String product, String buildFingerprint, String apiVersion,
            List<CommandOutput> commandOutputs) {
        this.name = name;
        this.product = product;
        this.buildFingerprint = buildFingerprint;
        this.apiVersion = apiVersion;
        this.commandOutputs = ImmutableList.copyOf(commandOutputs);
    }

    /** Serialized form of meta-information about the command execution */
    static class CommandOutput {
        final List<String> command;
        final String stdoutFileName;
        final @Nullable String stderrFileName;
        final @Nullable String exitCode;

        public CommandOutput(List<String> command, String stdoutFileName, @Nullable String stderrFileName,
                @Nullable String exitCode) {
            this.command = command;
            this.stdoutFileName = stdoutFileName;
            this.stderrFileName = stderrFileName;
            this.exitCode = exitCode;
        }
    }
}
