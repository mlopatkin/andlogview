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
import java.util.concurrent.CompletableFuture;

/**
 * An ongoing search.
 */
public interface Search<P> {
    /**
     * Starts the search starting at the current position in the given direction. Currently selected item is only tested
     * if it wasn't already matched before. The search may be performed asynchronously. If the end of the cursor is
     * reached without finding any matching items, then an empty Optional is returned.
     *
     * @param direction the direction to search in
     * @return the completable future that receives the search result
     */
    CompletableFuture<Optional<P>> search(Direction direction);

    /**
     * Sets the new position from which the next search will start.
     *
     * @param position the new position
     */
    void setPosition(P position);

    /**
     * The direction of a search.
     */
    enum Direction {
        CURRENT_THEN_FORWARD(1, true),
        CURRENT_THEN_BACKWARD(-1, true),
        FORWARD(1, false, CURRENT_THEN_FORWARD),
        BACKWARD(-1, false, CURRENT_THEN_BACKWARD);

        private final int increment;
        private final boolean shouldSearchCurrent;
        private final Direction withCurrentSearch;

        Direction(int increment, boolean shouldSearchCurrent, Direction withCurrentSearch) {
            this.increment = increment;
            this.shouldSearchCurrent = shouldSearchCurrent;
            this.withCurrentSearch = withCurrentSearch;
        }

        Direction(int increment, boolean shouldSearchCurrent) {
            this.increment = increment;
            this.shouldSearchCurrent = shouldSearchCurrent;
            this.withCurrentSearch = this;
        }

        boolean tryAdvanceCursor(SearchCursor<?, ?> cursor) {
            return cursor.advance(increment);
        }

        boolean shouldSearchCurrent() {
            return shouldSearchCurrent;
        }

        /**
         * Returns the Direction constant that performs search in the same direction, but also checks the currently
         * selected item.
         *
         * @return the Direction
         */
        public Direction alsoSearchCurrent() {
            return withCurrentSearch;
        }
    }
}
