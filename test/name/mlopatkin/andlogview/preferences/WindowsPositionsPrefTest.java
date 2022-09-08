/*
 * Copyright 2022 the Andlogview authors
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

import static name.mlopatkin.andlogview.ui.FrameDimensionsAssert.assertThat;
import static name.mlopatkin.andlogview.ui.FrameLocationAssert.assertThat;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.preferences.WindowsPositionsPref.Frame;
import name.mlopatkin.andlogview.test.DefaultConfigurationExtension;
import name.mlopatkin.andlogview.ui.FrameDimensions;
import name.mlopatkin.andlogview.ui.FrameLocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DefaultConfigurationExtension.class)
class WindowsPositionsPrefTest {
    private final FakeInMemoryConfigStorage configStorage = new FakeInMemoryConfigStorage();
    private final WindowsPositionsPref pref = new WindowsPositionsPref(configStorage);

    @Test
    void setPreferenceValuesAreAvailable() {
        pref.setFrameInfo(Frame.MAIN, new FrameLocation(10, 10), new FrameDimensions(1024, 768));
        assertThat(pref.getFrameLocation(Frame.MAIN)).hasValueSatisfying(
                location -> assertThat(location).isAt(10, 10)
        );
        assertThat(pref.getFrameDimensions(Frame.MAIN)).hasDimensions(1024, 768);
    }

    @Test
    void preferenceCanBeSerializedAndDeserialized() {
        pref.setFrameInfo(Frame.MAIN, new FrameLocation(10, 10), new FrameDimensions(1024, 768));

        WindowsPositionsPref newPref = new WindowsPositionsPref(configStorage);

        assertThat(newPref.getFrameLocation(Frame.MAIN)).hasValueSatisfying(
                location -> assertThat(location).isAt(10, 10)
        );
        assertThat(newPref.getFrameDimensions(Frame.MAIN)).hasDimensions(1024, 768);
    }

    @Test
    @SuppressWarnings("deprecation")
    void preferenceFallsBackToConfiguration() {
        assertThat(pref.getFrameDimensions(Frame.MAIN)).hasDimensions(Configuration.ui.mainWindowWidth(),
                Configuration.ui.mainWindowHeight());
    }

    @Test
    void defaultLocationIsNotPresent() {
        assertThat(pref.getFrameLocation(Frame.MAIN)).isNotPresent();
    }

    @Test
    void preferenceCanBeRestoredFromFrozenSerializedForm() {
        configStorage.setJsonData("windows", "{ \"main\": { \"width\": 1024, \"height\": 768, \"x\": 12, \"y\": 21} }");

        assertThat(pref.getFrameDimensions(Frame.MAIN)).hasDimensions(1024, 768);
        assertThat(pref.getFrameLocation(Frame.MAIN)).hasValueSatisfying(
                location -> assertThat(location).isAt(12, 21)
        );
    }

    @Test
    void preferenceCanBeRestoredFromSerializedFormWithoutLocation() {
        configStorage.setJsonData("windows", "{ \"main\": { \"width\": 1024, \"height\": 768} }");

        assertThat(pref.getFrameDimensions(Frame.MAIN)).hasDimensions(1024, 768);
        assertThat(pref.getFrameLocation(Frame.MAIN)).isNotPresent();
    }

    @Test
    @SuppressWarnings("deprecation")
    void invalidWidthFallbacksToDefaultSizeAndPos() {
        configStorage.setJsonData("windows",
                "{ \"main\": { \"width\": -1024, \"height\": 768, \"x\": 12, \"y\": 21} }");

        assertThat(pref.getFrameDimensions(Frame.MAIN)).hasDimensions(Configuration.ui.mainWindowWidth(),
                Configuration.ui.mainWindowHeight());
        assertThat(pref.getFrameLocation(Frame.MAIN)).isNotPresent();
    }

    @Test
    @SuppressWarnings("deprecation")
    void invalidHeightFallbacksToDefaultSizeAndPos() {
        configStorage.setJsonData("windows",
                "{ \"main\": { \"width\": 1024, \"height\": -768, \"x\": 12, \"y\": 21} }");

        assertThat(pref.getFrameDimensions(Frame.MAIN)).hasDimensions(Configuration.ui.mainWindowWidth(),
                Configuration.ui.mainWindowHeight());
        assertThat(pref.getFrameLocation(Frame.MAIN)).isNotPresent();
    }

    @Test
    @SuppressWarnings("deprecation")
    void invalidJsonFallbacksToDefaultSizeAndPos() {
        configStorage.setJsonData("windows", "[ ]");

        assertThat(pref.getFrameDimensions(Frame.MAIN)).hasDimensions(Configuration.ui.mainWindowWidth(),
                Configuration.ui.mainWindowHeight());
        assertThat(pref.getFrameLocation(Frame.MAIN)).isNotPresent();
    }
}
