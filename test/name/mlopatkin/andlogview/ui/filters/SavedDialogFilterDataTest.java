/*
 * Copyright 2024 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.filters;

import static org.assertj.core.api.Assertions.assertThat;

import name.mlopatkin.andlogview.config.Utils;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.filters.PredicateFilter;
import name.mlopatkin.andlogview.search.RequestCompilationException;
import name.mlopatkin.andlogview.test.TestData;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialogData;

import com.google.gson.Gson;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;

class SavedDialogFilterDataTest {
    private static final Gson GSON = Utils.createConfigurationGson();

    @ParameterizedTest
    @CsvSource({
            "true, AudioFlinger, true",
            "false, NotAudioFlinger, false"
    })
    void canRoundTripTheFilter(boolean isFilterEnabled, String tagPattern, boolean expectedMatch) throws Exception {
        var original = createFilter(isFilterEnabled, tagPattern);

        var deserialized = roundTrip(original);

        assertThat(deserialized).isEqualTo(original);
        assertThat(deserialized.isEnabled()).isEqualTo(isFilterEnabled);
        assertThat(deserialized.getData().getTags()).singleElement().isEqualTo(tagPattern);

        assertThat(((PredicateFilter) deserialized).test(TestData.RECORD1)).isEqualTo(expectedMatch);
    }

    private FilterFromDialog createFilter(boolean isEnabled, String tagPattern) throws RequestCompilationException {
        var filterData = new FilterFromDialogData();
        filterData.setMode(FilteringMode.getDefaultMode());
        filterData.setTags(Collections.singletonList(tagPattern));
        filterData.compile();

        return filterData.toFilter(isEnabled);
    }

    private FilterFromDialog roundTrip(FilterFromDialog original) throws Exception {
        // TODO(mlopatkin) this is a somewhat meaningless test now.
        var originalData = new SavedDialogFilterData(original);
        return GSON.fromJson(GSON.toJson(originalData), SavedDialogFilterData.class).fromSerializedForm();
    }
}
