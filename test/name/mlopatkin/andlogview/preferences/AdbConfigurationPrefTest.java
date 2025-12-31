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

import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.sdkrepo.AdbLocationDiscovery;
import name.mlopatkin.andlogview.utils.FakePathResolver;
import name.mlopatkin.andlogview.utils.SystemPathResolver;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.util.Optional;

class AdbConfigurationPrefTest {
    private final FakeInMemoryConfigStorage storage = new FakeInMemoryConfigStorage();

    @Test
    void canCheckInvalidPath() {
        SystemPathResolver resolver = mock();
        when(resolver.resolveExecutablePath(anyString())).thenReturn(Optional.of(new File("adb")));
        when(resolver.resolveExecutablePath("invalid path")).thenThrow(InvalidPathException.class);

        var pref = createPref(resolver);

        assertThat(pref.checkAdbLocation("valid path")).isTrue();
        assertThat(pref.checkAdbLocation("invalid path")).isFalse();
    }

    @Test
    void changingFlagsDoesNotWriteAdbPath() {
        var resolver = mockResolver();
        var pref = createPref(resolver);

        pref.setShowAdbAutostartFailures(false);

        assertThat(storage.getJsonData("adb", "location")).isNull();
    }

    @Test
    void canCreateAdbPreferenceWhenOnlyLocationIsProvidedInConfig() {
        storage.setJsonData("adb", """
                {
                  "location": "adb.exe"
                }""");

        var resolver = mockResolver();
        var pref = createPref(resolver);

        assertThat(pref.getExecutable()).contains(new File("adb.exe"));
        assertThat(pref.isAutoReconnectEnabled()).isTrue();
        assertThat(pref.shouldShowAutostartFailures()).isTrue();
    }

    @Test
    void canResolveDefaultAdbLocation() {
        var resolver = mockResolver();
        var pref = createPref(resolver);

        assertThat(pref.hasValidAdbLocation()).isTrue();
        assertThat(pref.getExecutable()).contains(new File(AdbLocationDiscovery.ADB_EXECUTABLE));
    }

    @Test
    void autoDiscoveryIsAllowedByDefault() {
        var pref = createPref(mockResolver());

        assertThat(pref.isAdbAutoDiscoveryAllowed()).isTrue();
    }

    @Test
    void canCommitAutoDiscoveredLocation() {
        var pref = createPref(mockResolver());
        var autoDiscoveredPath = "/usr/bin/adb";

        var committedAutoDiscovered = pref.trySetAutoDiscoveredLocation(autoDiscoveredPath);

        assertThat(committedAutoDiscovered).isTrue();
        assertThat(pref.getAdbLocation()).isEqualTo(autoDiscoveredPath);
        assertThat(pref.getExecutable()).contains(new File(autoDiscoveredPath));
    }

    @Test
    void autoDiscoveredLocationDoesNotOverrideExplicit() {
        var pref = createPref(mockResolver());
        var explicitPath = "/home/user/bin/adb";
        var autoDiscoveredPath = "/usr/bin/adb";

        pref.trySetAdbLocation(explicitPath);

        var committedAutoDiscovered = pref.trySetAutoDiscoveredLocation(autoDiscoveredPath);

        assertThat(committedAutoDiscovered).isFalse();
        assertThat(pref.getAdbLocation()).isEqualTo(explicitPath);
        assertThat(pref.getExecutable()).contains(new File(explicitPath));
    }

    @Test
    void committingAutoDiscoveredLocationStillAllowsAutoDiscovery() {
        var pref = createPref(mockResolver());
        var autoDiscoveredPath = "/usr/bin/adb";

        pref.trySetAutoDiscoveredLocation(autoDiscoveredPath);

        assertThat(pref.isAdbAutoDiscoveryAllowed()).isTrue();
    }

    @Test
    void canOverrideAutoDiscoveredLocationWithExplicit() {
        var pref = createPref(mockResolver());
        var autoDiscoveredPath = "/usr/bin/adb";
        var explicitPath = "/home/user/bin/adb";

        pref.trySetAutoDiscoveredLocation(autoDiscoveredPath);

        var committedExplicit = pref.trySetAdbLocation(explicitPath);

        assertThat(committedExplicit).isTrue();
        assertThat(pref.getAdbLocation()).isEqualTo(explicitPath);
        assertThat(pref.getExecutable()).contains(new File(explicitPath));
    }

    @Test
    void canOverrideExplicitLocationWithAnotherExplicit() {
        var pref = createPref(mockResolver());
        var explicitPath = "/usr/bin/adb";
        var anotherExplicitPath = "/home/user/bin/adb";

        pref.trySetAdbLocation(explicitPath);

        var committedExplicit = pref.trySetAdbLocation(anotherExplicitPath);

        assertThat(committedExplicit).isTrue();
        assertThat(pref.getAdbLocation()).isEqualTo(anotherExplicitPath);
        assertThat(pref.getExecutable()).contains(new File(anotherExplicitPath));
    }

    @Test
    void canOverrideAutoDiscoveredLocationWithAnotherAutoDiscovery() {
        var pref = createPref(mockResolver());
        var autoDiscoveredPath = "/usr/bin/adb";
        var anotherDiscoveredPath = "/home/user/bin/adb";

        pref.trySetAutoDiscoveredLocation(autoDiscoveredPath);

        var committedAnother = pref.trySetAutoDiscoveredLocation(anotherDiscoveredPath);

        assertThat(committedAnother).isTrue();
        assertThat(pref.getAdbLocation()).isEqualTo(anotherDiscoveredPath);
        assertThat(pref.getExecutable()).contains(new File(anotherDiscoveredPath));
    }

    private AdbConfigurationPref createPref(SystemPathResolver resolver) {
        return new AdbConfigurationPref(storage, resolver);
    }

    private SystemPathResolver mockResolver() {
        return FakePathResolver.acceptsAnything();
    }
}
