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

import com.google.common.base.Preconditions;

/**
 * Base class for index-based cursor. It tracks position internally as a non-negative integer index bounded by 0
 * (inclusive) and the size of the underlying sequence (exclusive). All indices within the bounds are considered valid
 * and should have corresponding values. An array and its index are the typical example.
 *
 * @param <T> the type of values
 * @param <P> the type of positions
 */
public abstract class AbstractIndexCursor<T, P> implements SearchCursor<T, P> {
    private int index;

    /**
     * Converts an index to a position. The index is guaranteed to be in the {@code [0, size())} interval.
     *
     * @param index the valid index
     * @return the position that corresponds to the index
     */
    protected abstract P indexToPosition(int index);

    /**
     * Converts a position to an index. The returned index may be out of bounds, it is checked for validity separately.
     *
     * @param position the position
     * @return the index that corresponds to the given position
     * @throws IllegalArgumentException if the position cannot be converted to an index
     */
    protected abstract int positionToIndex(P position) throws IllegalArgumentException;

    /**
     * Returns a value at the given index. The index is guaranteed to be in the {@code [0, size())} interval.
     *
     * @param index the valid index
     * @return the value at the index.
     */
    protected abstract T getValueAtIndex(int index);

    /**
     * Returns the size of the underlying sequence. The size must be non-negative. An empty sequence has the size of 0.
     *
     * @return the size of the sequence.
     */
    protected abstract int size();


    @Override
    public final boolean advance(int increment) {
        if (isValidIndex(index + increment)) {
            index += increment;
            return true;
        }
        return false;
    }

    @Override
    public final T getValue() {
        Preconditions.checkState(isValidIndex(index), "Current index %s is out of bounds [0, %s)", index, size());
        return getValueAtIndex(index);
    }

    @Override
    public final P getPosition() {
        Preconditions.checkState(isValidIndex(index), "Current index %s is out of bounds [0, %s)", index, size());
        return indexToPosition(index);
    }

    @Override
    public final void setPosition(P newPosition) {
        var newIndex = positionToIndex(newPosition);
        Preconditions.checkArgument(isValidIndex(newIndex),
                "Position %s (index %s) is out of bounds [0, %s)", newPosition, newIndex, size());
        index = newIndex;
    }

    @Override
    public final boolean isEmpty() {
        return size() == 0;
    }

    private boolean isValidIndex(int index) {
        return 0 <= index && index < size();
    }
}
