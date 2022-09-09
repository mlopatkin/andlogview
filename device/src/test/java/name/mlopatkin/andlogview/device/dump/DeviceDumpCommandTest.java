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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class DeviceDumpCommandTest {
    private final Gson gson = new Gson();

    @Test
    public void deserializationTest() {
        DeviceDumpCommand command = gson.fromJson("{\n"
                + "      \"baseOutputName\": \"logcat\",\n"
                + "      \"commandLine\": [\"logcat\", \"-d\"]\n"
                + "    }", DeviceDumpCommand.class);

        assertEquals("logcat", command.baseOutputName);
        assertEquals(Arrays.asList("logcat", "-d"), command.commandLine);
    }
}
