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

import java.util.Objects;

/**
 * Helpers to create filters in tests.
 */
public final class Filters {
    private Filters() {}

    public static TestChildModelFilter childModelFilter() {
        return new TestChildModelFilter();
    }

    public static TestChildModelFilter childModelFilter(Filter... children) {
        return new TestChildModelFilter(ImmutableList.copyOf(children), true);
    }

    /**
     * Creates an enabled filter with a nice {@code toString} representation based on {@code name}. Name is also used
     * for equality checks, alongside enabled status. The returned filter doesn't implement any other interfaces.
     *
     * @param name the name of the filter
     * @return the named filter
     */
    public static Filter named(String name) {
        return new NamedFilter(name, true);
    }

    private static class NamedFilter extends AbstractFilter<NamedFilter> {
        private final String name;

        protected NamedFilter(String name, boolean enabled) {
            super(FilteringMode.HIDE, enabled);
            this.name = name;
        }

        @Override
        protected NamedFilter copy(boolean enabled) {
            return new NamedFilter(name, enabled);
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof NamedFilter named && (named.isEnabled() == isEnabled())
                    && Objects.equals(name, named.name));
        }

        @Override
        public int hashCode() {
            return Objects.hash(isEnabled(), name);
        }
    }
}
