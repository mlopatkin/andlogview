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

import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class FakeCommand implements Command {
    private OutputStream stdout = ByteStreams.nullOutputStream();
    private OutputStream stderr = ByteStreams.nullOutputStream();

    private final String exitCode = "0";
    private String out = "";
    private String err = "";

    public FakeCommand() {
    }

    @Override
    public Command redirectOutput(OutputStream stdout) {
        return this;
    }

    @Override
    public Command redirectError(OutputStream stderr) {
        return this;
    }

    @Override
    public Result execute() throws IOException {
        ByteStreams.copy(new ByteArrayInputStream(out.getBytes(StandardCharsets.UTF_8)), stdout);
        ByteStreams.copy(new ByteArrayInputStream(err.getBytes(StandardCharsets.UTF_8)), stderr);
        return () -> exitCode;
    }
}
