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

import name.mlopatkin.andlogview.device.AdbDevice;
import name.mlopatkin.andlogview.device.Command;
import name.mlopatkin.andlogview.device.DeviceGoneException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * An utility class that invokes a series of commands upon the given device and stores stdout and stderr of these
 * commands in a ZIP archive along with a metadata file.
 */
class DeviceDumpBuilder implements Closeable {
    private final AdbDevice device;
    private final ZipOutputStream zipOutput;
    private final List<DeviceDumpMetadata.CommandOutput> commandOutputs = new ArrayList<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private boolean closed;

    /**
     * The constructor. The resulting objects takes ownership of the provided output stream.
     *
     * @param device the device to get the dump from
     * @param zipOutput the output stream to write zip archive into
     */
    public DeviceDumpBuilder(AdbDevice device, OutputStream zipOutput) {
        this.device = device;
        this.zipOutput = new ZipOutputStream(zipOutput);
    }

    /**
     * Run the given command on the device and collect its outputs. This method blocks until the command completes.
     *
     * @param command the command to run
     * @throws IOException if something goes wrong while communicating with the device or writing output data
     * @throws InterruptedException if the thread is interrupted
     */
    public void collectCommandOutput(DeviceDumpCommand command) throws IOException, InterruptedException {
        commandOutputs.add(collectCommandDump(command));
    }

    private DeviceDumpMetadata.CommandOutput collectCommandDump(DeviceDumpCommand command)
            throws IOException, InterruptedException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();


        Command.Result cmdResult;
        try {
            cmdResult = device.command(command.commandLine)
                    .redirectOutput(stdout)
                    .redirectError(stderr)
                    .execute();
        } catch (DeviceGoneException e) {
            throw new IOException("Device is unresponsive", e);
        }

        String exitCode = cmdResult.getExitCode();
        // Output files may still contain something useful even if exit code != 0, especially stderr.
        String stdoutFilePath = joinZipPath(command.baseOutputName, "stdout.txt");
        saveOutputToZipFile(stdoutFilePath, stdout);

        String stderrFilePath = joinZipPath(command.baseOutputName, "stderr.txt");
        saveOutputToZipFile(stderrFilePath, stderr);
        return new DeviceDumpMetadata.CommandOutput(command.commandLine, stdoutFilePath, stderrFilePath, exitCode);
    }

    private void saveOutputToZipFile(String path, ByteArrayOutputStream outputContent)
            throws IOException {
        try (OutputStream entry = zipEntry(path)) {
            outputContent.writeTo(entry);
        }
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;

        saveMetadata();
        zipOutput.close();
    }

    private void saveMetadata() throws IOException {
        // TODO(mlopatkin) requireNonNull here is a bit error-prone, it should be fixed
        DeviceDumpMetadata metadata =
                new DeviceDumpMetadata(
                        device.getName(),
                        Objects.requireNonNull(device.getProduct()),
                        Objects.requireNonNull(device.getBuildFingerprint()),
                        device.getApiString(),
                        commandOutputs);
        try (OutputStream entry = zipEntry("metadata.json");
                OutputStreamWriter writer = new OutputStreamWriter(entry, StandardCharsets.UTF_8)) {
            gson.toJson(metadata, writer);
        }
    }

    private OutputStream zipEntry(String zipPath) throws IOException {
        zipOutput.putNextEntry(new ZipEntry(zipPath));
        return new FilterOutputStream(zipOutput) {
            @Override
            public void close() throws IOException {
                zipOutput.closeEntry();
                // Do not close the wrapped stream though
            }
        };
    }

    private static String joinZipPath(String... pathElements) {
        // Note that ZIP files always use '/' as a path separator.
        return String.join("/", pathElements);
    }
}
