/*
 * Copyright 2014 Mikhail Lopatkin
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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fluent implementation of the Predicate.
 */
public abstract class FluentPredicate<T> implements Predicate<T> {
    // don't try to use #from there
    private static final FluentPredicate<Object> ALWAYS_TRUE =
            new FluentPredicateWrapper<Object>(Predicates.alwaysTrue());

    public static <T> FluentPredicate<T> alwaysTrue() {
        return ALWAYS_TRUE.narrow();
    }

    public static <T> FluentPredicate<T> from(@Nullable Predicate<T> inner) {
        if (inner instanceof FluentPredicate) {
            return (FluentPredicate<T>) inner;
        }
        if (inner == null) {
            return ALWAYS_TRUE.narrow();
        }
        return new FluentPredicateWrapper<T>(inner);
    }

    public FluentPredicate<T> and(@Nullable Predicate<? super T> other) {
        if (other == null) {
            return this;
        }
        return FluentPredicate.from(Predicates.and(getPredicate(), other)).narrow();
    }

    public FluentPredicate<T> or(@Nullable Predicate<? super T> other) {
        if (other == null) {
            return this;
        }
        return FluentPredicate.from(Predicates.or(getPredicate(), other)).narrow();
    }

    public FluentPredicate<T> not() {
        return FluentPredicate.from(Predicates.not(getPredicate())).narrow();
    }

    @SuppressWarnings("unchecked")
    <U extends T> FluentPredicate<U> narrow() {
        return (FluentPredicate<U>) this;
    }

    Predicate<T> getPredicate() {
        return this;
    }

    private static class FluentPredicateWrapper<T> extends FluentPredicate<T> {
        private final Predicate<T> inner;

        protected FluentPredicateWrapper(@Nonnull Predicate<T> inner) {
            this.inner = Preconditions.checkNotNull(inner);
        }

        @Override
        public boolean apply(@Nullable T t) {
            return inner.apply(t);
        }

        @Override
        Predicate<T> getPredicate() {
            return inner;
        }
    }
}
