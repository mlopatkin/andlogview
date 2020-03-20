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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.function.Predicate;

/**
 * Collection of {@link Matcher}s for {@link Predicate}s.
 */
public final class PredicateMatchers {
    private PredicateMatchers() {}

    public static <T> Matcher<? extends Predicate<? super T>> accepts(T value) {
        return new TypeSafeMatcher<Predicate<T>>() {
            @Override
            protected boolean matchesSafely(Predicate<T> item) {
                return item.test(value);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("accepts value=").appendValue(value);
            }

            @Override
            protected void describeMismatchSafely(Predicate<T> item, Description mismatchDescription) {
                mismatchDescription.appendText("value=").appendValue(value).appendText(" was rejected");
            }
        };
    }

    public static <T> Matcher<? extends Predicate<T>> rejects(T value) {
        return new TypeSafeMatcher<Predicate<T>>() {
            @Override
            protected boolean matchesSafely(Predicate<T> item) {
                return !item.test(value);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("rejects value=").appendValue(value);
            }

            @Override
            protected void describeMismatchSafely(Predicate<T> item, Description mismatchDescription) {
                mismatchDescription.appendText("it was accepted");
            }
        };
    }
}
