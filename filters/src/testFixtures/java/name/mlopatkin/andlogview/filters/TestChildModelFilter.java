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

import com.google.common.collect.ImmutableList;

import java.util.Collection;

public class TestChildModelFilter extends AbstractFilter<TestChildModelFilter> implements ChildModelFilter {
    private final MutableFilterModel children = MutableFilterModel.create();

    public TestChildModelFilter() {
        this(ImmutableList.of(), true);
    }

    TestChildModelFilter(Collection<? extends Filter> children, boolean enabled) {
        super(FilteringMode.WINDOW, enabled);
        children.forEach(this.children::addFilter);
    }

    @Override
    public MutableFilterModel getChildren() {
        return children;
    }

    @Override
    protected TestChildModelFilter copy(boolean enabled) {
        return new TestChildModelFilter(children.getFilters(), enabled);
    }
}
