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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.Ints;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Objects;
import java.util.function.Predicate;

class SyncSearchTest {
    @Test
    void canFindElementSearchingForward() {
        var s = createSearch(equalTo(1), 0, 1, 2);

        assertThat(s.search(Search.Direction.FORWARD)).contains(1);
    }

    @ParameterizedTest
    @EnumSource(value = Search.Direction.class, names = {"FORWARD", "BACKWARD"})
    void currentElementIsSkippedIfNotRequested(Search.Direction direction) {
        var s = createSearch(equalTo(1), 0, 1, 2);
        s.setPosition(1);

        assertThat(s.search(direction)).isEmpty();
    }

    @Test
    void canFindElementSearchingBackward() {
        var s = createSearch(equalTo(1), 0, 1, 2);
        s.setPosition(2);

        assertThat(s.search(Search.Direction.BACKWARD)).contains(1);
    }

    @ParameterizedTest
    @EnumSource(value = Search.Direction.class, names = {"FORWARD", "BACKWARD"})
    void canFindCurrentElement(Search.Direction direction) {
        var s = createSearch(equalTo(1), 0, 1, 2);
        s.setPosition(1);

        assertThat(s.search(direction.alsoSearchCurrent())).contains(1);
        assertThat(s.search(direction)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Search.Direction.class, names = {"FORWARD", "BACKWARD"})
    void repetitiveCurrentSearchReturnsTheSameElement(Search.Direction direction) {
        var s = createSearch(equalTo(1), 0, 1, 2);
        s.setPosition(1);

        assertThat(s.search(direction.alsoSearchCurrent())).contains(1);
        assertThat(s.search(direction.alsoSearchCurrent())).contains(1);
    }

    @Test
    void canFindLastElement() {
        var s = createSearch(equalTo(2), 0, 1, 2);

        assertThat(s.search(Search.Direction.FORWARD)).contains(2);
    }

    @Test
    void canFindFirstElement() {
        var s = createSearch(equalTo(0), 0, 1, 2);
        s.setPosition(2);

        assertThat(s.search(Search.Direction.BACKWARD)).contains(0);
    }

    @Test
    void canFindSeveralElements() {
        var s = createSearch(equalTo(1), 0, 1, 2, 1);

        assertThat(s.search(Search.Direction.FORWARD)).contains(1);
        assertThat(s.search(Search.Direction.FORWARD)).contains(3);
        assertThat(s.search(Search.Direction.FORWARD)).isEmpty();
    }

    @Test
    void newSearchPicksUpCursorLocation() {
        var cursor = cursor(0, 1, 2);
        var s = createSearch(equalTo(2), cursor);

        s.search(Search.Direction.FORWARD);

        var s2 = new SyncSearch<>(cursor, equalTo(1));
        assertThat(s2.search(Search.Direction.FORWARD)).isEmpty();
        assertThat(s2.search(Search.Direction.BACKWARD)).contains(1);
    }

    @ParameterizedTest
    @EnumSource(value = Search.Direction.class)
    void searchingEmptyCursorFindsNothing(Search.Direction direction) {
        var s = createSearch(o -> true);

        assertThat(s.search(direction)).isEmpty();
    }

    @Test
    void findingNothingDoesNotAdvanceCursor() {
        var cursor = cursor(0, 1, 2);
        var s = createSearch(equalTo(1), cursor);

        s.search(Search.Direction.FORWARD);

        assertThat(s.search(Search.Direction.FORWARD)).isEmpty();
        assertThat(cursor.getPosition()).isEqualTo(1);
    }

    private static Predicate<Integer> equalTo(int value) {
        return i -> Objects.equals(value, i);
    }

    private static SearchCursor<Integer, Integer> cursor(int... items) {
        return new ListSearchModel<>(Ints.asList(items)).newCursor();
    }

    private static SyncSearch<Integer, Integer> createSearch(Predicate<Integer> matcher,
            SearchCursor<Integer, Integer> cursor) {
        return new SyncSearch<>(cursor, matcher);
    }

    private static SyncSearch<Integer, Integer> createSearch(Predicate<Integer> matcher, int... items) {
        return createSearch(matcher, cursor(items));
    }
}
