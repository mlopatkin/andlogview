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

package name.mlopatkin.andlogview.search;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * A search implementation that performs search synchronously.
 */
class SyncSearch<T, P> {
    private final SearchCursor<T, P> cursor;
    private final Predicate<? super T> matcher;

    SyncSearch(SearchCursor<T, P> cursor, Predicate<? super T> matcher) {
        this.cursor = cursor;
        this.matcher = matcher;
    }

    public Optional<P> search(Search.Direction direction) {
        if (cursor.isEmpty()) {
            return notFound();
        }

        var initialPosition = cursor.getPosition();
        if (direction.shouldSearchCurrent()) {
            if (matchesCurrent()) {
                return Optional.of(initialPosition);
            }
        }

        while (direction.tryAdvanceCursor(cursor)) {
            if (matchesCurrent()) {
                return Optional.of(cursor.getPosition());
            }
        }

        // Revert cursor's position. This helps in the following case:
        // 1. find something
        // 2. try search forward, find nothing
        // 3. try search backward
        // On step (3) we should not find the item from the step (1) once more, because it is likely to be currently
        // selected in the search UI.
        cursor.setPosition(initialPosition);
        return notFound();
    }

    public void setPosition(P position) {
        cursor.setPosition(position);
    }

    private boolean matchesCurrent() {
        return matcher.test(cursor.getValue());
    }

    private Optional<P> notFound() {
        return Optional.empty();
    }
}
