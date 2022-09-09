/*
 * Copyright 2022 the Andlogview authors
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

import static name.mlopatkin.andlogview.utils.TryAssert.assertThat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

class TryTest {
    private static final Integer VALUE = 1;
    private static final Exception EXCEPTION = new IOException("Some exception");

    @ParameterizedTest
    @MethodSource("triesWithValues")
    void tryWithValueIsPresent(Try<Integer> t) {
        assertThat(t).hasValue(VALUE);
    }

    @ParameterizedTest
    @MethodSource("triesWithErrors")
    void tryWithErrorIsNotPresent(Try<Integer> t) {
        assertThat(t).hasError(EXCEPTION);
    }

    @ParameterizedTest
    @MethodSource("triesWithErrors")
    void tryWithErrorThrowsWhenValueRequested(Try<Integer> t) {
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(t::get);
    }

    @ParameterizedTest
    @MethodSource("triesWithValues")
    void tryWithValueThrowsWhenExceptionRequested(Try<Integer> t) {
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(t::getError);
    }

    @ParameterizedTest
    @MethodSource("triesWithValues")
    void tryWithValueMappedToNewValue(Try<Integer> t) {
        assertThat(t.map(v -> v + 1)).hasValue(VALUE + 1);
    }

    @ParameterizedTest
    @MethodSource("triesWithValues")
    void tryWithValueMappedToErrorIfMappingThrows(Try<Integer> t) {
        RuntimeException ex = new RuntimeException("Other exception");
        assertThat(t.map(v -> {
            throw ex;
        })).hasError(ex);
    }

    @ParameterizedTest
    @MethodSource("triesWithErrors")
    void tryWithErrorMappedToSameError(Try<Integer> t) {
        assertThat(t.map(v -> v + 1)).hasError(EXCEPTION);
    }

    @ParameterizedTest
    @MethodSource("triesWithErrors")
    void tryWithErrorMappedToSameErrorIfMappingThrows(Try<Integer> t) {
        RuntimeException ex = new RuntimeException("Other exception");
        assertThat(t.map(v -> {
            throw ex;
        })).hasError(EXCEPTION);
    }

    @ParameterizedTest
    @MethodSource("triesWithValues")
    void tryWithValueDoesNotCallHandleError(Try<Integer> t) {
        @SuppressWarnings("unchecked")
        Consumer<Throwable> handler = mock(Consumer.class);

        t.handleError(handler);

        verifyNoInteractions(handler);
    }

    @ParameterizedTest
    @MethodSource("triesWithErrors")
    void tryWithErrorCallsHandleError(Try<Integer> t) {
        @SuppressWarnings("unchecked")
        Consumer<Throwable> handler = mock(Consumer.class);

        t.handleError(handler);

        verify(handler).accept(EXCEPTION);
    }

    @ParameterizedTest
    @MethodSource("triesWithValues")
    void tryWithValueDoesNotThrowInOrElseThrow(Try<Integer> t) {
        assertThatNoException().isThrownBy(() -> t.orElseThrow(Function.identity()));

        assertThat(t.orElseThrow(AssertionError::new)).isEqualTo(VALUE);
    }

    @ParameterizedTest
    @MethodSource("triesWithErrors")
    void tryWithErrorThrowsInOrElseThrow(Try<Integer> t) {
        assertThatThrownBy(() -> t.orElseThrow(RuntimeException::new))
                .isInstanceOf(RuntimeException.class)
                .hasCause(EXCEPTION);
    }

    @ParameterizedTest
    @MethodSource("triesWithValues")
    void tryWithValueDoesRethrow(Try<Integer> t) throws IOException {
        assertThatNoException().isThrownBy(() -> t.rethrowOfType(IOException.class));

        assertThat(t.rethrowOfType(IOException.class).get()).isEqualTo(VALUE);
    }

    @ParameterizedTest
    @MethodSource("triesWithErrors")
    void tryWithErrorRethrowsIfCorrectType(Try<Integer> t) {
        assertThatThrownBy(() -> t.rethrowOfType(IOException.class)).isEqualTo(EXCEPTION);
    }

    @ParameterizedTest
    @MethodSource("triesWithErrors")
    void tryWithErrorDoesNotRethrowIfOtherType(Try<Integer> t) {
        assertThatNoException().isThrownBy(() -> t.rethrowOfType(RuntimeException.class));
    }

    @ParameterizedTest
    @MethodSource("triesWithValues")
    void tryWithValueConvertsToOptionalWithValue(Try<Integer> t) {
        assertThat(t.toOptional()).hasValue(VALUE);
    }

    @ParameterizedTest
    @MethodSource("triesWithErrors")
    void tryWithErrorConvertsToEmptyOptional(Try<Integer> t) {
        assertThat(t.toOptional()).isEmpty();
    }

    public static Stream<Try<Integer>> triesWithValues() {
        return Stream.of(Try.ofValue(VALUE), Try.ofCallable(() -> VALUE));
    }

    public static Stream<Try<Integer>> triesWithErrors() {
        return Stream.of(Try.ofError(EXCEPTION), Try.ofCallable(() -> {
            throw EXCEPTION;
        }));
    }
}
