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

import name.mlopatkin.andlogview.base.AppResources;
import name.mlopatkin.andlogview.device.Device;
import name.mlopatkin.andlogview.utils.MyFutures;

import com.google.common.io.ByteSink;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import javax.inject.Inject;

/**
 * Factory to prepare a device dump.
 * <p>
 * The device dump is a collection of logs in various formats, device properties, outputs of some commands that
 * AndLogView uses internally to gather additional information (e.g. output of the {@code ps} command that provides info
 * about process names).
 */
public class DeviceDumpFactory {
    @Inject
    DeviceDumpFactory() {
    }

    /**
     * @return the provisional file name for a dump of the device based on certain device info
     */
    public String getProvisionalOutputFileName(Device device) {
        return String.format("%s.sdk-%s.dump.zip", device.getProduct(), device.getApiString());
    }

    // I'm not particularly proud of how this class retrieves JSON but whatever. It isn't in the constructor because
    // it is hard to handle exception thrown from the constructor.
    private List<DeviceDumpCommand> getCommands() {
        try (Reader r = AppResources.getResource("dumpcommands.json")
                .asCharSource(StandardCharsets.UTF_8)
                .openBufferedStream()) {
            Gson gson = new Gson();
            return gson.fromJson(r, new TypeToken<List<DeviceDumpCommand>>() {}.getType());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open resources", e);
        }
    }

    /**
     * Collects device dump from the given device and stores it into the provided byte sink. The dump
     * result may be incomplete if the exception is thrown. This method blocks until the dump is complete.
     *
     * @param device the device to gather dump from
     * @param zipOutput the location for the zip archive with the dump results
     * @throws IOException if something goes wrong during dumping/writing dump
     * @throws InterruptedException if the thread is interrupted
     */
    public void collect(Device device, ByteSink zipOutput) throws IOException, InterruptedException {
        try (OutputStream outputStream = zipOutput.openBufferedStream();
                DeviceDumpBuilder dumpBuilder = new DeviceDumpBuilder(device, outputStream)) {
            for (DeviceDumpCommand command : getCommands()) {
                dumpBuilder.collectCommandOutput(command);
            }
        }
    }

    /**
     * Collects device dump on the provided executor and stores it into the provided byte sink. The dump result may be
     * incomplete if the resulting future fails. This method (obviously) doesn't block unless something like
     * directExecutor is used.
     *
     * @param device the device to gather dump from
     * @param zipOutput the location for the zip archive with the dump results
     * @param executor the executor to run the dumping tasks on
     * @return the CompletionStage to be notified about dumping completion/failure
     */
    public CompletionStage<Void> collectAsync(Device device, ByteSink zipOutput, Executor executor) {
        return MyFutures.runAsync(() -> collect(device, zipOutput), executor);
    }
}
