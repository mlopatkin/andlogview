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

import org.assertj.core.api.AbstractAssert;
import org.junit.jupiter.api.Assertions;

public class TryAssert<T> extends AbstractAssert<TryAssert<T>, Try<T>> {
    private final Try<T> t;

    public TryAssert(Try<T> t) {
        super(t, TryAssert.class);
        this.t = t;
    }

    @SuppressWarnings("unchecked")
    public static <T> TryAssert<T> assertThat(Try<? extends T> t) {
        return new TryAssert<>((Try<T>) t);
    }

    public TryAssert<T> hasValue() {
        Assertions.assertTrue(t.isPresent(), () -> "Try instance holds error " + t.getError());
        return this;
    }

    public TryAssert<T> hasValue(T value) {
        hasValue();
        Assertions.assertEquals(value, t.get());
        return this;
    }

    public TryAssert<T> hasError() {
        Assertions.assertFalse(t.isPresent(), () -> "Try instance holds value " + t.get());
        return this;
    }

    public TryAssert<T> hasError(Throwable th) {
        hasError();
        Assertions.assertEquals(th, t.getError());
        return this;
    }
}
