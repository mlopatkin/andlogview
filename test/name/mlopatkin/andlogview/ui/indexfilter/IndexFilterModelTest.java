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

package name.mlopatkin.andlogview.ui.indexfilter;

import static name.mlopatkin.andlogview.filters.FilterModelAssert.assertThat;
import static name.mlopatkin.andlogview.filters.FilterModelAssert.assertThatFilters;

import name.mlopatkin.andlogview.filters.Filter;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.filters.TestFilterModel;
import name.mlopatkin.andlogview.filters.ToggleFilter;

import org.junit.jupiter.api.Test;

class IndexFilterModelTest {

    @Test
    void indexModelAddsOneFilter() {
        var parent = new TestFilterModel();
        var indexFilter = createFilter();
        parent.addFilter(indexFilter);

        var indexFilterModel = IndexFilterModel.createIndexFilterModel(parent, indexFilter);

        assertThatFilters(indexFilterModel).hasSize(2);
    }

    @Test
    void indexModelUnsubscribesItselfFromParentWhenFilterRemoved() {
        var parent = new TestFilterModel();
        var indexFilter = createFilter();
        parent.addFilter(indexFilter);

        var indexFilterModel = IndexFilterModel.createIndexFilterModel(parent, indexFilter);
        parent.removeFilter(indexFilter);

        assertThatFilters(indexFilterModel).hasSize(1);
        assertThat(parent).hasNoObservers();
    }

    private Filter createFilter() {
        return new ToggleFilter(FilteringMode.WINDOW, true, r -> true);
    }
}
