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

/**
 * The cursor represents a position in a sequence of items.
 *
 * @param <T> the type of the searchable items
 * @param <P> the type of the model position
 */
public interface SearchCursor<T, P> {
    /**
     * Tries to advance the cursor for {@code increment} positions. The negative increment indicates that the
     * advancement should be done in reverse direction. Cursor position doesn't change if the incremented position is
     * outside the sequence.
     *
     * @param increment the number of positions to advance, can be negative
     * @return {@code true} if the advancement succeeded, {@code false} if the advanced position is outside the
     *         sequence
     */
    boolean advance(int increment);

    /**
     * Returns the value of the item the cursor points too. Throws an exception if the cursor is empty.
     *
     * @return the value of the current item
     */
    T getValue();

    /**
     * Returns the position of the item the cursor points too. Throws an exception if the cursor is empty.
     *
     * @return the position of the current item
     */
    P getPosition();

    /**
     * Updates the position of the cursor. Throws {@link IllegalArgumentException} if the position is invalid, in
     * particular if it is outside the sequence bounds.
     *
     * @param newPosition the new position
     * @throws IllegalArgumentException if the position is not valid
     */
    void setPosition(P newPosition);

    /**
     * Returns {@code true} if the underlying sequence is empty. The empty cursor cannot be moved, its value or position
     * cannot be accessed. The position of the empty cursor cannot be updated.
     *
     * @return {@code true} if the underlying sequence is empty, {@code false} otherwise
     */
    boolean isEmpty();
}
