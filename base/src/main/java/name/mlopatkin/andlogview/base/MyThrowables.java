/*
 * Copyright 2023 the Andlogview authors
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

package name.mlopatkin.andlogview.base;

import com.google.common.base.Throwables;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * Additional utility methods to deal with Throwables.
 */
public final class MyThrowables {
    private MyThrowables() {}

    /**
     * Unwraps the causal chain until an exception of type {@code clazz} can be found. Returns empty {@link Optional}
     * if there is no such exception. Doesn't look into suppressed exceptions. Only the first occurence is returned.
     *
     * @param th the exception to investigate
     * @param clazz the exception class to look up for
     * @param <E> the type of the exception to look for
     * @return the exception of the given type that was found in the causal chain or an empty optional
     */
    public static <E extends Throwable> Optional<E> findCause(Throwable th, Class<E> clazz) {
        return Throwables.getCausalChain(th).stream().filter(clazz::isInstance).map(clazz::cast).findFirst();
    }

    /**
     * Unwraps the given exception if it is one of the common wrapper types, like {@link CompletionException} or
     * {@link InvocationTargetException}.
     *
     * @param th the exception to unwrap
     * @return the unwrapped cause
     */
    public static Throwable unwrapUninteresting(Throwable th) {
        Throwable result = th;
        while (result.getCause() != null && isWrapperException(result)) {
            result = result.getCause();
        }
        if (result != th) {
            // Keep the information about this exception for further reference, but only append it once.
            try {
                if (!hasSuppressed(result, th)) {
                    result.addSuppressed(th);
                }
            } catch (OutOfMemoryError ignored) { // ok to catch OutOfMemoryError there
                // Additional allocation errors may happen while unwrapping, we're not interested in them.
            }
        }
        return result;
    }

    private static boolean isWrapperException(Throwable th) {
        // Check if the given throwable is just a wrapper for some other exception. In most cases, the wrapper itself
        // isn't interesting.
        return (th instanceof ExecutionException || th instanceof CompletionException
                                         || th instanceof InvocationTargetException);
    }

    private static boolean hasSuppressed(Throwable th, Throwable maybeSuppressed) {
        for (Throwable suppressed : th.getSuppressed()) {
            if (maybeSuppressed == suppressed) {
                return true;
            }
        }
        return false;
    }
}
