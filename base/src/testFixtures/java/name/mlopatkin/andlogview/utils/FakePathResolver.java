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

package name.mlopatkin.andlogview.utils;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Test implementation of {@link SystemPathResolver}.
 */
public final class FakePathResolver {
    private FakePathResolver() {}

    private static class ResolverImpl extends SystemPathResolver {
        private final Predicate<? super String> accepts;

        private ResolverImpl(Predicate<? super String> accepts) {
            this.accepts = accepts;
        }

        @Override
        public Optional<File> resolveExecutablePath(String rawPath) {
            if (accepts.test(rawPath)) {
                return Optional.of(new File(rawPath));
            }

            return Optional.empty();
        }
    }


    /**
     * Creates a resolver that only accepts the given paths.
     *
     * @param validPaths the valid paths
     * @return the resolver
     */
    public static SystemPathResolver withValidPaths(String... validPaths) {
        return new ResolverImpl(ImmutableSet.copyOf(validPaths)::contains);
    }

    /**
     * Creates a resolver that accepts any paths.
     *
     * @return the resolver
     */
    public static SystemPathResolver acceptsAnything() {
        return new ResolverImpl(Predicates.alwaysTrue());
    }

    /**
     * Creates a resolver that accepts no paths.
     *
     * @return the resolver
     */
    public static SystemPathResolver acceptsNothing() {
        return new ResolverImpl(Predicates.alwaysFalse());
    }
}
