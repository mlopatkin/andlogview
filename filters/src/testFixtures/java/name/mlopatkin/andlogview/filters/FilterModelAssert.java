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

package name.mlopatkin.andlogview.filters;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.util.CheckReturnValue;

import java.util.Collection;

public class FilterModelAssert extends AbstractAssert<FilterModelAssert, TestFilterModel> {
    protected FilterModelAssert(TestFilterModel testFilterModel) {
        super(testFilterModel, FilterModelAssert.class);
    }

    public FilterModelAssert hasNoObservers() {
        if (actual.hasObservers()) {
            throw failure("still has observers");
        }
        return this;
    }

    @CheckReturnValue
    public static FilterModelAssert assertThat(TestFilterModel filterModel) {
        return new FilterModelAssert(filterModel);
    }

    @CheckReturnValue
    public static AbstractCollectionAssert<
            ?, Collection<? extends Filter>, Filter, ObjectAssert<Filter>
            > assertThatFilters(FilterModel model) {
        return Assertions.assertThat(model.getFilters());
    }
}
