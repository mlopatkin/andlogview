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

package name.mlopatkin.andlogview.filters;

import static name.mlopatkin.andlogview.filters.FilterModelAssert.assertThatFilters;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

class CompoundFilterModelTest {
    private final MutableFilterModel parent = MutableFilterModel.create();
    private final TestFilter testFilter = filter();

    @BeforeEach
    void setUp() {
        parent.addFilter(testFilter);
    }

    @Test
    void filtersAddedAfterIndexDoNotShowInTheModel() {
        var model = new CompoundFilterModel(parent, testFilter);
        var hideFilter = ToggleFilter.hide(Predicates.alwaysFalse());

        parent.addFilter(hideFilter);

        assertThatFilters(model).doesNotContain(hideFilter);
    }

    private static TestFilter filter() {
        return new TestFilter();
    }

    private static class TestFilter extends AbstractFilter<TestFilter> implements ChildModelFilter {
        private final MutableFilterModel children = MutableFilterModel.create();

        public TestFilter() {
            this(ImmutableList.of(), true);
        }

        protected TestFilter(Collection<? extends Filter> children, boolean enabled) {
            super(FilteringMode.WINDOW, enabled);
            children.forEach(this.children::addFilter);
        }

        @Override
        public MutableFilterModel getChildren() {
            return children;
        }

        @Override
        protected TestFilter copy(boolean enabled) {
            return new TestFilter(children.getFilters(), enabled);
        }
    }
}
