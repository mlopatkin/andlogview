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

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

public final class MyStreams {
    private MyStreams() {}

    /**
     * Filters out null values in the stream of nullable values. Takes care of types.
     *
     * @param stream the stream of nullable values to filter
     * @return a filtered stream
     * @param <T> the non-nullable type of values.
     */
    @SuppressWarnings({"NullAway", "NullableProblems"})
    public static <T> Stream<T> withoutNulls(Stream<@Nullable T> stream) {
        return stream.filter(Objects::nonNull);
    }
}
