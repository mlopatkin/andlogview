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

package name.mlopatkin.andlogview.ui.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.device.AdbManager;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.utils.FakePathResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class AdbServiceStarterTest {
    static final String VALID_ADB = "/usr/bin/adb";
    static final String ANOTHER_VALID_ADB = "/home/user/Android/Sdk/platform-tools/adb";
    static final String INVALID_ADB = "/usr/local/bin/invalid-adb";

    @Mock
    AdbManager adbManager;

    AdbConfigurationPref adbPref =
            new AdbConfigurationPref(new FakeInMemoryConfigStorage(),
                    FakePathResolver.withValidPaths(VALID_ADB, ANOTHER_VALID_ADB));

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(adbManager.startServer(any())).thenReturn(mock());
    }

    @Test
    void canStartFromSpecifiedLocation() throws Exception {
        withAdbLocationSpecified(VALID_ADB);

        var starter = createStarter();

        starter.startAdb();

        verify(adbManager).startServer(new File(VALID_ADB));
    }

    @Test
    void canAutoDiscoverAdb() throws Exception {
        var starter = createStarterWithAutoDiscovery(INVALID_ADB, VALID_ADB, ANOTHER_VALID_ADB);

        starter.startAdb();

        verify(adbManager).startServer(new File(VALID_ADB));
        assertThat(adbPref.getAdbLocation()).isEqualTo(VALID_ADB);
    }

    @Test
    void failsWhenNoAdbProvidedAndItCannotBeDiscovered() throws Exception {
        var starter = createStarterWithAutoDiscovery(INVALID_ADB);

        assertThatThrownBy(starter::startAdb).isInstanceOf(AdbException.class);

        verify(adbManager, never()).startServer(any());
    }

    @Test
    void noAutodiscoveryWhenAdbIsSpecified() throws Exception {
        withAdbLocationSpecified(VALID_ADB);

        var starter = createStarterWithAutoDiscovery(ANOTHER_VALID_ADB);

        starter.startAdb();

        verify(adbManager).startServer(new File(VALID_ADB));
    }

    @Test
    void stopsDiscoveryIfAdbPreferenceIsSet() throws Exception {
        var locations = Stream.of(INVALID_ADB, ANOTHER_VALID_ADB).peek(path -> {
            if (ANOTHER_VALID_ADB.equals(path)) {
                // A side effect while consuming the stream to stop iteration early.
                // Simulates a user setting up the preference.
                adbPref.trySetAdbLocation(VALID_ADB);
            }
        });

        var starter = createStarterWithAutoDiscovery(locations);

        starter.startAdb();

        // Should start the server based on the value set by the user rather than auto-discovered one.
        verify(adbManager).startServer(new File(VALID_ADB));
    }

    private void withAdbLocationSpecified(String adbLocation) {
        adbPref.trySetAdbLocation(adbLocation);
    }

    private AdbServiceStarter createStarter() {
        return createStarterWithAutoDiscovery(Stream.empty());
    }

    private AdbServiceStarter createStarterWithAutoDiscovery(String... autoDiscoveryLocations) {
        return createStarterWithAutoDiscovery(Stream.of(autoDiscoveryLocations));
    }

    private AdbServiceStarter createStarterWithAutoDiscovery(Stream<String> autoDiscoveryLocations) {
        return new AdbServiceStarter(adbManager, adbPref, () -> autoDiscoveryLocations.map(File::new));
    }
}
