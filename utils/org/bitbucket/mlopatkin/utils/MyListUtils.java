/*
 * Copyright 2011 Mikhail Lopatkin
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

import java.util.Collections;
import java.util.List;

/**
 * Utility methods for {@link List}s.
 */
public class MyListUtils {
    private MyListUtils() {
    }

    private static <T extends Comparable<? super T>> boolean lessOrEq(T a, T b) {
        return a.compareTo(b) <= 0;
    }

    /**
     * Merges {@code elems} into {@code base} with respect to the sorting order.
     * 
     * @param base
     *            sorted list
     * @param elems
     *            sorted list of elements to be merged into {@code base}
     * @return index of first inserted row
     */
    public static <T extends Comparable<? super T>> int mergeOrdered(List<T> base, List<T> elems) {
        // handle specific cases
        if (elems.isEmpty()) {
            return base.size();
        }
        if (base.isEmpty()) {
            base.addAll(elems);
            return 0;
        }

        int basePos = 0;
        int elemsPos = 0;

        int firstInsertedIndex = -1;
        // assume that base element should be before equal elems element in the
        // result
        while (basePos < base.size() && elemsPos < elems.size()) {
            T baseItem = base.get(basePos);
            T elemsItem = elems.get(elemsPos);
            if (lessOrEq(baseItem, elemsItem)) {
                ++basePos;
            } else {
                // elemsItem and possibly some more should be inserted before
                // baseItem
                int elemsIntervalEndPos = elemsPos + 1;
                while (elemsIntervalEndPos < elems.size()) {
                    T elemsIntervalEndItem = elems.get(elemsIntervalEndPos);
                    if (!lessOrEq(baseItem, elemsIntervalEndItem)) {
                        // still should be inserted before
                        ++elemsIntervalEndPos;
                    } else {
                        // should be inserted after
                        break;
                    }
                }
                // now elemsIntervalEndPos is pointing to the excluded end of
                // the interval
                List<T> interval = elems.subList(elemsPos, elemsIntervalEndPos);
                base.addAll(basePos, interval);
                if (firstInsertedIndex < 0) {
                    firstInsertedIndex = basePos;
                }
                elemsPos = elemsIntervalEndPos;
                basePos += interval.size();
            }
        }
        // note that we can have some rest elems that are greater than any
        // element of base, we should add them
        if (elemsPos < elems.size()) {
            if (firstInsertedIndex < 0) {
                firstInsertedIndex = basePos;
            }
            base.addAll(elems.subList(elemsPos, elems.size()));
        }
        return firstInsertedIndex;
    }

    /**
     * Returns the position where {@code value} should be inserted into
     * {@code items}. After that {@code items} should remain sorted and
     * {@code value} should be inserted after all equivalent elements already
     * presented in the {@code items}, if any.
     * 
     * @param items
     *            non-{@code null} sorted list
     * @param value
     *            to be inserted into list
     * @return position in which {@code value} should be inserted into
     *         {@code items} using {@link List#add(int, Object)}
     */
    public static <T extends Comparable<? super T>> int getUpperBoundPos(List<T> items, T value) {
        if (items.isEmpty()) {
            return 0;
        }
        int pos = Collections.binarySearch(items, value);
        if (pos < 0) {
            return -(pos + 1);
        }
        T item = items.get(pos);
        while (value.compareTo(item) == 0) {
            ++pos;
            if (pos < items.size()) {
                item = items.get(pos);
            } else {
                break;
            }
        }
        return pos;
    }
}
