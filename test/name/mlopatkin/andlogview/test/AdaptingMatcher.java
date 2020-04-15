/*
 * Copyright 2018 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.test;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.function.Function;

/**
 * Utility class that helps to write custom matchers for object properties. If you have a method of an object that
 * returns a particular type for which a built-in matcher is available then you can combine the method reference and
 * this built-in matcher in this class.
 * <p/>
 * There is equivalence between {@code assertThat(adapter(foo), matchesSomething())} and
 * {@code assertThat(foo, new AdaptingMatcher<>(adapter, matchesSomething()))} but the latter is more versatile: it
 * can be used as an argument for another matcher.
 *
 * @param <T> the type to run matcher on
 * @param <R> the type of the method
 */
public class AdaptingMatcher<T, R> extends TypeSafeMatcher<T> {
    private final Function<T, R> adapter;
    private final Matcher<? super R> matcher;

    /**
     * Creates a delegating matcher that basically {@code matcher.matches(adapter(t))}.
     *
     * @param adapter the function that converts available type to something the matcher can consume
     * @param matcher the matcher that does actual work
     */
    public AdaptingMatcher(Function<T, R> adapter, Matcher<? super R> matcher) {
        this.adapter = adapter;
        this.matcher = matcher;
    }

    @Override
    protected boolean matchesSafely(T item) {
        return matcher.matches(adapter.apply(item));
    }

    @Override
    public void describeTo(Description description) {
        matcher.describeTo(description);
    }

    @Override
    protected void describeMismatchSafely(T item, Description mismatchDescription) {
        matcher.describeMismatch(adapter.apply(item), mismatchDescription);
    }
}
