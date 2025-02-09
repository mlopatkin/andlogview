/*
 * Copyright 2022 Mikhail Lopatkin
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This class represents a result of computation that could throw an exception. It closely resembles a classic
 * {@code Either} monad but is a bit more limited - can only hold an exception as an alternative instead of anything.
 * Another analogy would be an Optional with the additional explanation why it is empty.
 * <p>
 * There is no exception type parameter, so the {@code Try} can store runtime exceptions that occur when applying
 * {@link #map(Function)} to the value.
 *
 * @param <T> the type of the result
 */
public abstract class Try<T> {
    private Try() {}

    /**
     * @return {@code true} if the computation was successful and this class holds a value
     */
    public abstract boolean isPresent();

    /**
     * @return a value if it is present
     * @throws IllegalStateException if the value isn't present
     */
    public abstract T get() throws IllegalStateException;

    /**
     * @return an exception if it is present instead of a value
     * @throws IllegalStateException if the value is actually present
     */
    public abstract Throwable getError() throws IllegalStateException;

    @SuppressWarnings("unchecked")
    private <R> Try<R> castIfError() {
        Preconditions.checkState(!isPresent());
        // This is a safe cast because the Error instance doesn't actually hold the value.
        return (Try<R>) this;
    }

    /**
     * Applies a function to the value if it is present and returns the result. If the value is not present then the
     * original exception is preserved in the result. If the function throws exception then the result holds this
     * exception instead of the value.
     *
     * @param function the function that converts a value to some other value
     * @param <V> the type of the result
     * @return the result of applying function to the value or an exception wrapped in {@code Try}
     */
    public final <V> Try<V> map(Function<? super T, ? extends V> function) {
        if (isPresent()) {
            return Try.ofCallable(() -> function.apply(get()));
        }

        return castIfError();
    }

    /**
     * Applies a function to the value if it is present and returns the result. If the value is not present then the
     * original exception is preserved in the result. If the function throws exception then the result holds this
     * exception instead of the value.
     *
     * @param function the function that converts a value to some other value
     * @param <V> the type of the result
     * @return the result of applying function to the value or an exception wrapped in {@code Try}
     */
    public final <V> Try<V> tryMap(TryFunction<? super T, ? extends V> function) {
        if (isPresent()) {
            return Try.ofCallable(() -> function.apply(get()));
        }

        return castIfError();
    }

    /**
     * If the exception is present then it is passed to the {@code handler}. Does nothing if the value is present.
     *
     * @param handler the handler to handle the exception
     * @return this {@code Try} for chaining
     */
    public final Try<T> handleError(Consumer<? super Throwable> handler) {
        if (!isPresent()) {
            handler.accept(getError());
        }
        return this;
    }

    /**
     * Combines two Try instances if they are present, or combines their exceptions.
     *
     * @param rhs the value to combine with
     * @param combiner the combiner function
     * @param <V> the type to combine with
     * @param <R> the result type
     * @return the combined try instance
     */
    public final <V, R> Try<R> zip(Try<V> rhs, BiFunction<? super T, ? super V, ? extends R> combiner) {
        if (isPresent() && rhs.isPresent()) {
            return Try.ofCallable(() -> combiner.apply(get(), rhs.get()));
        }
        if (rhs.isPresent()) {
            assert !isPresent();
            return castIfError();
        }
        // We're both failures.
        return Try.ofError(combineFailures(getError(), rhs.getError()));
    }

    /**
     * Returns the value is it is present. If the exception is present then wraps it in {@code E} with the
     * {@code exceptionWrapper} and throws the result.
     *
     * @param exceptionWrapper the function to wrap an exception held by this Try
     * @param <E> the type of the exception to throw
     * @return the value if this Try holds any
     * @throws E if this Try holds an exception
     */
    public final <E extends Throwable> T orElseThrow(Function<Throwable, E> exceptionWrapper) throws E {
        if (isPresent()) {
            return get();
        }
        throw exceptionWrapper.apply(getError());
    }

    /**
     * If the exception is present and is of type {@code E} then it is thrown. Does nothing if the value is present.
     *
     * @param exceptionClass the class of the exception to rethrow.
     * @param <E> the type of the exception to rethrow
     * @return this Try instance
     * @throws E if this Try holds an exception of type {@code E}
     */
    public final <E extends Throwable> Try<T> rethrowOfType(Class<E> exceptionClass) throws E {
        if (!isPresent()) {
            Throwable th = getError();
            if (exceptionClass.isInstance(th)) {
                throw exceptionClass.cast(th);
            }
        }
        return this;
    }

    /**
     * Wraps the value to Optional. If the value isn't present then empty Optional is returned.
     *
     * @return the Optional which holds the value or empty Optional if this instance holds an exception
     */
    public final Optional<T> toOptional() {
        if (isPresent()) {
            return Optional.of(get());
        }
        return Optional.empty();
    }

    /**
     * Creates an instance of {@code Try} that holds a value.
     *
     * @param value the value to hold (cannot be null)
     * @param <T> the type of the value
     * @return the {@code Try} that holds the given value
     */
    public static <T> Try<T> ofValue(T value) {
        Objects.requireNonNull(value);

        return new Try<>() {
            @Override
            public boolean isPresent() {
                return true;
            }

            @Override
            public T get() throws IllegalStateException {
                return value;
            }

            @Override
            public Throwable getError() throws IllegalStateException {
                throw new IllegalStateException("Error is not present");
            }
        };
    }

    /**
     * Creates an instance of {@code Try} that holds an exception.
     *
     * @param throwable the exception to hold (cannot be null)
     * @param <T> the type of the value
     * @return the {@code Try} that holds the given exception
     */
    public static <T> Try<T> ofError(Throwable throwable) {
        Objects.requireNonNull(throwable);

        return new Try<>() {
            @Override
            public boolean isPresent() {
                return false;
            }

            @Override
            public T get() throws IllegalStateException {
                throw new IllegalStateException(
                        "Value not available because an error " + throwable.getMessage() + " happened");
            }

            @Override
            public Throwable getError() throws IllegalStateException {
                return throwable;
            }
        };
    }

    /**
     * Executes the callable and returns the {@code Try} instance that holds the result. If the {@code action} completes
     * successfully then the resulting {@code Try} holds the returned value. If the {@code action} throws an exception
     * then the resulting {@code Try} holds this exception.
     *
     * @param action the Callable to run to produce the value
     * @param <T> the type of the value
     * @return the {@code Try} that holds the result (value/exception) of the call to {@code action}
     */
    public static <T> Try<T> ofCallable(Callable<? extends T> action) {
        try {
            return Try.ofValue(action.call());
        } catch (Throwable e) {  // OK to catch Throwable here
            return Try.ofError(e);
        }
    }

    /**
     * Collects the stream of {@code Try<T>} into a {@code Try<List<T>>}. Any failure in the stream results in the
     * overall failure.
     *
     * @param <T> the type of the elements
     * @return the collector to combine the stream elements.
     */
    public static <T> Collector<Try<T>, ?, Try<List<T>>> liftToList() {
        return Collectors.reducing(Try.ofValue(ImmutableList.of()), t -> t.map(ImmutableList::of),
                (a, b) -> a.zip(b, (l, r) -> ImmutableList.<T>builder().addAll(l).addAll(r).build()));
    }

    private static Throwable combineFailures(Throwable a, Throwable b) {
        final var cloned =
                a instanceof MultiTryException multiTryException ? multiTryException.copy() : new MultiTryException(a);
        cloned.addSuppressed(b);
        return cloned;
    }

    private static class MultiTryException extends Exception {
        public MultiTryException(Throwable cause) {
            super(cause);
        }

        public MultiTryException copy() {
            var cause = getCause();
            assert cause != null;
            var cloned = new MultiTryException(cause);
            for (var suppressed : getSuppressed()) {
                cloned.addSuppressed(suppressed);
            }
            return cloned;
        }
    }
}
