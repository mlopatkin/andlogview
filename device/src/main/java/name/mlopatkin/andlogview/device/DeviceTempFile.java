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

import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a temporary file on the connected device. Unlike typical APIs for temp files management on host
 * this class doesn't actually create a temp file when it is created, and there are no atomicity guarantees. This class
 * merely provides a shortcut to generate a unique name and simplify (with try-with-resources)
 * removal of the file with this name.
 */
class DeviceTempFile implements AutoCloseable {
    private static final String DEFAULT_TEMP_DIR = "/data/local/tmp";

    private final IDevice device;
    private final String path;

    public DeviceTempFile(IDevice device) {
        this(device, DEFAULT_TEMP_DIR);
    }

    public DeviceTempFile(IDevice device, String basePath) {
        this.device = device;
        this.path = DevicePaths.join(basePath, "tmp" + UUID.randomUUID() + ".tmp");
    }

    @Override
    public void close() throws DeviceGoneException {
        try {
            DeviceUtils.executeShellCommand(device, String.format("rm %s", getPath()), NullOutputReceiver.getReceiver(),
                    1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Retrieves content of the file and writes it to the provided output stream. This method blocks until read is
     * complete. This method may fail if the temp file hasn't been created.
     *
     * @param output the output stream to write file content to
     * @throws DeviceGoneException if device is disconnected or the adb connection is broken
     * @throws InterruptedException if the thread is interrupted
     * @throws IOException if the output stream throws
     */
    public void copyContentsTo(OutputStream output) throws DeviceGoneException, InterruptedException, IOException {
        StreamingOutputReceiver receiver = new StreamingOutputReceiver(output);
        DeviceUtils.executeShellCommand(device, String.format("cat %s", getPath()), receiver);
        if (receiver.hasException()) {
            throw receiver.getException();
        }
    }

    public String getPath() {
        return path;
    }
}
