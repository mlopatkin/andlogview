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

package name.mlopatkin.andlogview.device.dump;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import name.mlopatkin.andlogview.device.FakeDevice;
import name.mlopatkin.andlogview.utils.ScopedTempFile;

import com.google.common.io.Files;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.zip.ZipFile;

public class DeviceDumpBuilderTest {

    @Test
    void dumpBuilderCapturesCommandOutputs() throws IOException, InterruptedException {
        FakeDevice fakeDevice = new FakeDevice("fingerprint", "product", "21");

        try (ScopedTempFile tempFile = new ScopedTempFile("tmp", ".zip")) {
            try (OutputStream outputStream = Files.asByteSink(tempFile.getFile()).openBufferedStream();
                    DeviceDumpBuilder dumpBuilder = new DeviceDumpBuilder(fakeDevice, outputStream)) {
                dumpBuilder.collectCommandOutput(new DeviceDumpCommand("test", Collections.singletonList("logcat")));
            }

            try (ZipFile zipFile = new ZipFile(tempFile.getFile())) {
                assertNotNull(zipFile.getEntry("test/stdout.txt"));
                assertNotNull(zipFile.getEntry("test/stderr.txt"));
                assertNotNull(zipFile.getEntry("metadata.json"));
            }
        }
    }
}
