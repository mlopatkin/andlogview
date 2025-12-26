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

import static name.mlopatkin.andlogview.ui.device.AdbServiceStarterTest.AdbServerAssert.assertThat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import name.mlopatkin.andlogview.base.concurrent.SequentialExecutor;
import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.device.AdbDeviceList;
import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.device.AdbManager;
import name.mlopatkin.andlogview.device.AdbServer;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.utils.FakePathResolver;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
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

    final FakeInMemoryConfigStorage configStorage = new FakeInMemoryConfigStorage();

    @Mock
    AdbManager adbManager;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(adbManager.startServer(any())).thenAnswer(args -> new MockAdbServer(args.getArgument(0)));
    }

    @Test
    void canStartFromSpecifiedLocation() throws Exception {
        var starter = createStarter(withAdbLocationSpecified(VALID_ADB), noAutoDiscovery());

        assertThat(starter.startAdb()).hasAdbLocation(VALID_ADB);
    }

    @Test
    void canAutoDiscoverAdb() throws Exception {
        var adbPref = adbPrefThatAccepts(VALID_ADB, ANOTHER_VALID_ADB);
        var starter = createStarter(
                adbPref,
                autoDiscovery(INVALID_ADB, VALID_ADB, ANOTHER_VALID_ADB)
        );

        assertThat(starter.startAdb()).hasAdbLocation(VALID_ADB);
        assertThat(adbPref.getAdbLocation()).isEqualTo(VALID_ADB);
    }

    @Test
    void failsWhenNoAdbProvidedAndItCannotBeDiscovered() throws Exception {
        var starter = createStarter(adbPrefThatAccepts(VALID_ADB), autoDiscovery(INVALID_ADB));

        assertThatThrownBy(starter::startAdb).isInstanceOf(AdbException.class);
        verify(adbManager, never()).startServer(any());
    }

    @Test
    void noAutodiscoveryWhenAdbIsSpecified() throws Exception {
        withAdbLocationSpecified(VALID_ADB);

        var starter = createStarter(adbPrefThatAccepts(VALID_ADB, ANOTHER_VALID_ADB), autoDiscovery(ANOTHER_VALID_ADB));

        assertThat(starter.startAdb()).hasAdbLocation(VALID_ADB);
    }

    @Test
    void stopsDiscoveryIfAdbPreferenceIsSet() throws Exception {
        var adbPref = adbPrefThatAccepts(VALID_ADB, ANOTHER_VALID_ADB);
        var locations = Stream.of(INVALID_ADB, ANOTHER_VALID_ADB).peek(path -> {
            if (ANOTHER_VALID_ADB.equals(path)) {
                // A side effect while consuming the stream to stop iteration early.
                // Simulates a user setting up the preference.
                adbPref.trySetAdbLocation(VALID_ADB);
            }
        });

        var starter = createStarter(adbPref, locations);

        // Should start the server based on the value set by the user rather than auto-discovered one.
        assertThat(starter.startAdb()).hasAdbLocation(VALID_ADB);
    }

    @Test
    void canRerunAutoDiscoveryIfPreviouslyDiscoveredBecomesInvalid() throws Exception {
        var previouslyValidAdb = "/this/was/valid/some/time/ago";
        var initialStarter = createStarter(
                adbPrefThatAccepts(previouslyValidAdb),
                autoDiscovery(previouslyValidAdb)
        );
        initialStarter.startAdb(); // Runs the auto-discovery process

        var newStarter = createStarter(
                adbPrefThatAccepts(VALID_ADB),
                autoDiscovery(previouslyValidAdb, VALID_ADB)
        );

        assertThat(newStarter.startAdb()).hasAdbLocation(VALID_ADB);
    }

    @Test
    void doesNotRunAutoDiscoveryIfPreviouslyCommittedAdbBecomesInvalid() throws Exception {
        var previouslyValidAdb = "/this/was/valid/some/time/ago";
        withAdbLocationSpecified(previouslyValidAdb);

        var newStarter = createStarter(
                adbPrefThatAccepts(VALID_ADB),
                autoDiscovery(VALID_ADB)
        );

        assertThatThrownBy(newStarter::startAdb).isInstanceOf(AdbException.class);
    }

    private AdbConfigurationPref withAdbLocationSpecified(String adbLocation) {
        var pref = adbPrefThatAccepts(adbLocation);
        if (!pref.trySetAdbLocation(adbLocation)) {
            throw new AssertionError("Adb location is not set");
        }
        return pref;
    }

    private AdbConfigurationPref adbPrefThatAccepts(String... validAdbLocations) {
        return new AdbConfigurationPref(configStorage, FakePathResolver.withValidPaths(validAdbLocations));
    }

    private AdbServiceStarter createStarter(AdbConfigurationPref adbPref, Stream<String> autoDiscoveryLocations) {
        return new AdbServiceStarter(adbManager, adbPref, () -> autoDiscoveryLocations.map(File::new));
    }

    private static Stream<String> autoDiscovery(String... locations) {
        return Stream.of(locations);
    }

    private static Stream<String> noAutoDiscovery() {
        return Stream.empty();
    }

    static class MockAdbServer implements AdbServer {
        final File adbLocation;

        public MockAdbServer(File adbLocation) {
            this.adbLocation = adbLocation;
        }

        @Override
        public AdbDeviceList getDeviceList(SequentialExecutor listenerExecutor) {
            throw new UnsupportedOperationException();
        }
    }

    static class AdbServerAssert extends AbstractAssert<AdbServerAssert, AdbServer> {
        protected AdbServerAssert(AdbServer adbServer) {
            super(adbServer, AdbServerAssert.class);
        }

        public AdbServerAssert hasAdbLocation(String adbLocation) {
            Assertions.assertThat(actual).as("Must be mock server").isInstanceOf(MockAdbServer.class);
            Assertions.assertThat(((MockAdbServer) actual).adbLocation.getPath()).isEqualTo(adbLocation);
            return this;
        }

        static AdbServerAssert assertThat(AdbServer actual) {
            return new AdbServerAssert(actual);
        }
    }
}
