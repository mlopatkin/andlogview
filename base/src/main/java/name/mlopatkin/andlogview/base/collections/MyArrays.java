/*
 * Copyright 2024 the Andlogview authors
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

/**
 * Utility methods to simplify array building.
 */
public final class MyArrays {
    private MyArrays() {}

    /**
     * Creates an integer array of the provided values.
     * @param values the values
     * @return the new {@code int[]} array
     */
    public static int[] ints(int... values) {
        return values;
    }

    /**
     * Creates an array out of the provided Objects.
     * @param values the values
     * @return the new {@code Object[]} array
     */
    public static Object[] objects(Object... values) {
        return values;
    }
}
