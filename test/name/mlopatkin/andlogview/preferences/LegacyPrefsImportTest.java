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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.sdkrepo.AdbLocationDiscovery;
import name.mlopatkin.andlogview.utils.FakePathResolver;
import name.mlopatkin.andlogview.utils.LazyInstance;
import name.mlopatkin.andlogview.utils.SystemPathResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class LegacyPrefsImportTest {
    final LegacyConfiguration.Adb adb = mock(LegacyConfiguration.Adb.class);
    final LegacyConfiguration.Ui ui = mock(LegacyConfiguration.Ui.class);
    final LegacyConfiguration legacyConfiguration = mock();

    final FakeInMemoryConfigStorage configStorage = new FakeInMemoryConfigStorage();

    @BeforeEach
    void setUp() {
        lenient().when(legacyConfiguration.adb()).thenReturn(adb);
        lenient().when(legacyConfiguration.ui()).thenReturn(ui);
    }

    @Test
    void autoDiscoveryIsNotAllowedIfLegacyAdbIsAvailable() {
        var adbFile = Path.of("foo", "bar", "adb.exe").toFile();
        when(adb.executable()).thenReturn(adbFile.getPath());

        var adbPref = createAdbPref(FakePathResolver.acceptsAnything());
        var importer = createImport(adbPref);

        importer.importLegacyPreferences();

        assertThat(adbPref.getAdbLocation()).isEqualTo(adbFile.getPath());
        assertThat(adbPref.hasValidAdbLocation()).isTrue();
        assertThat(adbPref.getExecutable()).contains(adbFile);
        assertThat(adbPref.isAdbAutoDiscoveryAllowed()).isFalse();
    }

    @Test
    void autoDiscoveryIsAllowedIfLegacyAdbIsAvailableButInvalid() {
        var adbFile = Path.of("foo", "bar", "adb.exe").toFile();
        when(adb.executable()).thenReturn(adbFile.getPath());

        var adbPref = createAdbPref(FakePathResolver.acceptsNothing());
        var importer = createImport(adbPref);

        importer.importLegacyPreferences();

        assertThat(adbPref.hasValidAdbLocation()).isFalse();
        assertThat(adbPref.getAdbLocation()).isEqualTo(AdbLocationDiscovery.ADB_EXECUTABLE);
        assertThat(adbPref.isAdbAutoDiscoveryAllowed()).isTrue();
    }


    LegacyPrefsImport createImport(AdbConfigurationPref adbPref) {
        return new LegacyPrefsImport(
                legacyConfiguration,
                LazyInstance.of(configStorage),
                LazyInstance.of(new WindowsPositionsPref(configStorage)),
                LazyInstance.of(adbPref)
        );
    }

    AdbConfigurationPref createAdbPref(SystemPathResolver pathResolver) {
        return new AdbConfigurationPref(configStorage, pathResolver);
    }
}
