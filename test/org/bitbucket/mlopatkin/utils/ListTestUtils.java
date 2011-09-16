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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generic routines to help testing {@link MyListUtils}.
 */
class ListTestUtils {
    private ListTestUtils() {
    }

    static <T> ArrayList<T> list(T... args) {
        return new ArrayList<T>(Arrays.asList(args));
    }

    static <T> ArrayList<T> list() {
        return new ArrayList<T>();
    }

    static <T> void assertListEquals(List<T> expected, List<T> actual) {
        if (expected == null) {
            if (actual == null) {
                return;
            } else {
                fail("null expected but found non-null");
            }
        }
        if (expected != null && actual == null) {
            fail("Non-null expected but found null");
        }
        Object[] expecteds = expected.toArray(), actuals = actual.toArray();
        assertArrayEquals(expecteds, actuals);
    }

}
