/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.utils.SystemPathResolver;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.util.Optional;

class AdbConfigurationPrefTest {
    private final ConfigStorage storage = new FakeInMemoryConfigStorage();

    @Test
    void canCheckInvalidPath() {
        SystemPathResolver resolver = mock();
        when(resolver.resolveExecutablePath(anyString())).thenReturn(Optional.of(new File("adb")));
        when(resolver.resolveExecutablePath("invalid path")).thenThrow(InvalidPathException.class);

        var pref = createPref(resolver);

        assertThat(pref.checkAdbLocation("valid path")).isTrue();
        assertThat(pref.checkAdbLocation("invalid path")).isFalse();
    }

    private AdbConfigurationPref createPref(SystemPathResolver resolver) {
        return new AdbConfigurationPref(storage, resolver);
    }
}
