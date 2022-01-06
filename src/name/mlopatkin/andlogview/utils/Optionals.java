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

import java.util.Optional;
import java.util.function.Consumer;

public final class Optionals {
    private Optionals() {
    }

    /**
     * Backport of the {@code Optional.ifPresentOrElse} available in Java 9+.
     *
     * @param optional the optional
     * @param ifPresent action to execute with the Optional value if it is present
     * @param ifEmpty action to execute if the Optional is empty
     * @param <T> the type of the optional.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> void ifPresentOrElse(Optional<T> optional, Consumer<? super T> ifPresent, Runnable ifEmpty) {
        if (optional.isPresent()) {
            ifPresent.accept(optional.get());
        } else {
            ifEmpty.run();
        }
    }

    /**
     * Upcasts the return type of the Optional. Optional is return-type covariant (i.e. if A extends B then Optional[A]
     * can safely be used anywhere Optional[B] is expected). Unfortunately, Java prohibits lower bounds on generic
     * methods so this helper cannot make it into the Optional class itself.
     * <p>
     * Due to the type erasure this method is only a hint to the compiler, it does nothing.
     *
     * @param opt the optional to convert that holds some subtype of {@code T}
     * @param <T> the resulting type
     * @return the same optional but with upcasted result type
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> upcast(Optional<? extends T> opt) {
        return (Optional<T>) opt;
    }
}
