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
     */
    public static <T extends Comparable<? super T>> void mergeOrdered(List<T> base, List<T> elems) {
        // handle specific cases
        if (elems.isEmpty()) {
            return;
        }
        if (base.isEmpty()) {
            base.addAll(elems);
            return;
        }

        int basePos = 0;
        int elemsPos = 0;
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
                elemsPos = elemsIntervalEndPos;
                basePos += interval.size();
            }
        }
        // note that we can have some rest elems that are greater than any
        // element of base, we should add them
        if (elemsPos < elems.size()) {
            base.addAll(elems.subList(elemsPos, elems.size()));
        }
    }
}
