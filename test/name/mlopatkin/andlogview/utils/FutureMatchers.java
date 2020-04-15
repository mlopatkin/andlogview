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

package name.mlopatkin.andlogview.utils;

import com.google.common.util.concurrent.MoreExecutors;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * Hamcrest {@link Matcher}s that deal with {@link Future}s and related classes.
 */
public class FutureMatchers {
    public static <T> Matcher<CompletionStage<T>> notCompleted() {
        return new TypeSafeMatcher<CompletionStage<T>>() {
            @Override
            protected boolean matchesSafely(CompletionStage<T> stage) {
                return !ExecutionResult.check(stage).isCompleted();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is not yet completed");
            }

            @Override
            protected void describeMismatchSafely(CompletionStage<T> stage, Description mismatchDescription) {
                mismatchDescription.appendValue(stage).appendText(" is already completed");
            }
        };
    }

    public static <T> Matcher<CompletionStage<T>> completedWithResult(Matcher<? super T> matcher) {
        return new TypeSafeMatcher<CompletionStage<T>>() {
            @Override
            protected boolean matchesSafely(CompletionStage<T> stage) {
                ExecutionResult<T> result = ExecutionResult.check(stage);
                return result.isCompleted() && !result.hasException() && matcher.matches(result.getResult());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("completed and result is ").appendDescriptionOf(matcher);
            }

            @Override
            protected void describeMismatchSafely(CompletionStage<T> stage, Description mismatchDescription) {
                mismatchDescription.appendValue(stage);
                ExecutionResult<T> result = ExecutionResult.check(stage);
                if (!result.isCompleted()) {
                    mismatchDescription.appendText(" is not completed");
                } else if (result.hasException()) {
                    mismatchDescription.appendText(" failed with ").appendValue(result.exception.get());
                } else {
                    mismatchDescription.appendText(" completed with result=").appendValue(result.getResult());
                }
            }
        };
    }

    private static class ExecutionResult<T> implements BiFunction<T, Throwable, Void> {
        private final AtomicBoolean isCompleted = new AtomicBoolean(false);
        private final AtomicReference<T> result = new AtomicReference<>();
        private final AtomicReference<Throwable> exception = new AtomicReference<>();

        @Override
        public Void apply(T t, Throwable throwable) {
            isCompleted.set(true);
            result.set(t);
            exception.set(throwable);
            return null;
        }

        boolean isCompleted() {
            return isCompleted.get();
        }

        boolean hasException() {
            assert isCompleted();
            return exception.get() != null;
        }

        @SuppressWarnings("NullAway")
        T getResult() {
            assert isCompleted() && !hasException();
            return result.get();
        }

        public static <T> ExecutionResult<T> check(CompletionStage<T> stage) {
            ExecutionResult<T> r = new ExecutionResult<>();
            // If the CompletionStage is completed then handleAsync should invoke callback immediately
            stage.handleAsync(r, MoreExecutors.directExecutor());
            return r;
        }
    }
}
