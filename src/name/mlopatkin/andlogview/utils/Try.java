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

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

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

        // This is a safe cast because the Error instance doesn't actually hold the value.
        @SuppressWarnings("unchecked")
        Try<V> r = (Try<V>) this;
        return r;
    }

    /**
     * If the exception is present then it is passed to the {@code handler}. Does nothing if the value is present.
     *
     * @param handler the handler to handle the exception
     */
    public final void handleError(Consumer<? super Throwable> handler) {
        if (!isPresent()) {
            handler.accept(getError());
        }
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

        return new Try<T>() {
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

        return new Try<T>() {
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
}
