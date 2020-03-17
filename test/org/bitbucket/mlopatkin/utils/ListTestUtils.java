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

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Generic routines to help testing {@link MyListUtils}.
 */
class ListTestUtils {
    private ListTestUtils() {
    }

    @SafeVarargs
    static <T> ArrayList<T> list(T... args) {
        return new ArrayList<>(Arrays.asList(args));
    }

    static <T> ArrayList<T> list() {
        return new ArrayList<>();
    }

}
