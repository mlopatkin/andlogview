/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.base.collections;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;

import org.jspecify.annotations.Nullable;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class MyIterables {
    private MyIterables() {}

    /**
     * Returns the iterable that produces items from {@code items} while {@code predicate} returns {@code true} for
     * them.
     *
     * @param items the source of elements
     * @param predicate the predicate to test items.
     * @param <T> the type of elements
     * @return the new iterator
     */
    public static <T> Iterable<T> takeWhile(Iterable<? extends T> items, Predicate<? super T> predicate) {
        return new Iterable<>() {
            @Override
            public Iterator<T> iterator() {
                return takeWhile(items.iterator(), predicate);
            }
        };
    }


    /**
     * Returns the iterator that produces items from {@code iter} while {@code predicate} returns {@code true} for them.
     *
     * @param iter the source of elements
     * @param predicate the predicate to test items.
     * @param <T> the type of elements
     * @return the new iterator
     */
    public static <T> Iterator<T> takeWhile(Iterator<? extends T> iter, Predicate<? super T> predicate) {
        return new AbstractIterator<>() {
            @Override
            protected @Nullable T computeNext() {
                if (iter.hasNext()) {
                    var next = iter.next();
                    return predicate.test(next) ? next : endOfData();
                }
                return endOfData();
            }
        };
    }

    /**
     * Allows using {@link Enumeration} in foreach loops
     *
     * @param enumeration the supplier of enumerations
     * @param <T> the element type
     * @return the iterable that builds its iterable out of the supplier
     */
    public static <T> Iterable<T> forEnumeration(Supplier<? extends Enumeration<T>> enumeration) {
        return () -> Iterators.forEnumeration(enumeration.get());
    }
}
