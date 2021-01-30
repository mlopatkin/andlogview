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
    Command redirectOutput(OutputStream stdout);

    Command redirectError(OutputStream stderr);

    Result execute() throws InterruptedException, IOException, DeviceGoneException;

    interface Result {
        String getExitCode();
    }
}
