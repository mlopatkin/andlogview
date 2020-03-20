/*
 * Copyright 2020 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.utils;

import com.google.common.base.Predicates;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Compatibility classes
 */
@SuppressWarnings("Guava")
public class MorePredicates {
    private MorePredicates() {}

    /**
     * Adapter for {@link Predicates#and(Iterable)} that can handle j.u.f.Predicate.
     */
    public static <T> Predicate<T> and(Collection<? extends Predicate<? super T>> predicates) {
        return Predicates.and(
                predicates.stream().<com.google.common.base.Predicate<T>>map(p -> p::test).collect(
                        Collectors.toList()));
    }

    /**
     * Adapter for {@link Predicates#or(Iterable)} that can handle j.u.f.Predicate.
     */
    public static <T> Predicate<T> or(Collection<? extends Predicate<? super T>> predicates) {
        return Predicates.or(
                predicates.stream().<com.google.common.base.Predicate<T>>map(p -> p::test).collect(
                        Collectors.toList()));
    }
}
