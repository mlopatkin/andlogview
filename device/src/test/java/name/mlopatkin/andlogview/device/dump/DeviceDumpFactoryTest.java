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

import name.mlopatkin.andlogview.device.FakeDevice;

import com.google.common.io.ByteSink;
import com.google.common.io.ByteStreams;

import org.junit.jupiter.api.Test;

import java.io.OutputStream;

class DeviceDumpFactoryTest {

    @Test
    void sanityTest() throws Exception {
        FakeDevice fakeDevice = new FakeDevice("fingerprint", "product", "Z");
        DeviceDumpFactory factory = new DeviceDumpFactory();

        factory.collect(fakeDevice, new ByteSink() {
            @Override
            public OutputStream openStream() {
                return ByteStreams.nullOutputStream();
            }
        });
    }
}
