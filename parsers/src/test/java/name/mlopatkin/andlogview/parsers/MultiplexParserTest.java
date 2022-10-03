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

package name.mlopatkin.andlogview.parsers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class MultiplexParserTest {
    @Test
    void emptyParserStopsImmediately() {
        try (MultiplexParser<BasePushParser> parser = new MultiplexParser<>()) {
            assertThat(parser.nextLine("1")).isFalse();
        }
    }

    @Test
    void parserWithSingleChildForwardsInput() {
        BasePushParser child = mockChild();
        try (MultiplexParser<BasePushParser> parser = new MultiplexParser<>(child)) {
            assertThat(parser.nextLine("1")).isTrue();
        }

        verify(child).nextLine("1");
        verify(child).close();
    }

    @Test
    void parserWithSingleChildStopsWhenChildStops() {
        BasePushParser child = mockChild();
        when(child.nextLine(any())).thenReturn(true, false);
        try (MultiplexParser<BasePushParser> parser = new MultiplexParser<>(child)) {
            assertThat(parser.nextLine("1")).isTrue();
            assertThat(parser.nextLine("2")).isFalse();
            assertThat(parser.nextLine("3")).isFalse();
        }

        InOrder order = inOrder(child);
        order.verify(child).nextLine("1");
        order.verify(child).nextLine("2");
        order.verify(child).close();
        order.verifyNoMoreInteractions();
    }

    @Test
    void parserForwardsInputToChildren() {
        BasePushParser child1 = mockChild();
        BasePushParser child2 = mockChild();

        try (MultiplexParser<BasePushParser> parser = new MultiplexParser<>(child1, child2)) {
            parser.nextLine("1");
            parser.nextLine("2");
        }

        verify(child1).nextLine("1");
        verify(child2).nextLine("1");

        verify(child1).nextLine("2");
        verify(child2).nextLine("2");
    }

    @Test
    void parserForwardsCloseToChildren() {
        BasePushParser child1 = mockChild();
        BasePushParser child2 = mockChild();

        try (MultiplexParser<BasePushParser> parser = new MultiplexParser<>(child1, child2)) {
            parser.nextLine("1");
            parser.nextLine("2");
        }

        verify(child1).close();
        verify(child2).close();
    }

    @Test
    void parserStopsWhenAllChildrenStop() {
        BasePushParser child1 = mockChild();
        when(child1.nextLine(any())).thenReturn(true, false);
        BasePushParser child2 = mockChild();
        when(child2.nextLine(any())).thenReturn(true, true, false);

        try (MultiplexParser<BasePushParser> parser = new MultiplexParser<>(child1, child2)) {
            assertThat(parser.nextLine("1")).isTrue();
            assertThat(parser.nextLine("2")).isTrue();
            assertThat(parser.nextLine("3")).isFalse();
        }
    }

    @Test
    void stoppedChildrenReceiveNoInput() {
        BasePushParser child1 = mockChild();
        when(child1.nextLine(any())).thenReturn(false);
        BasePushParser child2 = mockChild();

        try (MultiplexParser<BasePushParser> parser = new MultiplexParser<>(child1, child2)) {
            parser.nextLine("1");
            parser.nextLine("2");
        }

        verify(child1).nextLine("1");
        verify(child1, never()).nextLine("2");
    }

    @Test
    void stoppedChildrenReceivesClose() {
        BasePushParser child1 = mockChild();
        when(child1.nextLine(any())).thenReturn(false);
        BasePushParser child2 = mockChild();

        try (MultiplexParser<BasePushParser> parser = new MultiplexParser<>(child1, child2)) {
            parser.nextLine("1");
            parser.nextLine("2");
            // Do not close the child before the parser itself is closed, even if it is exhausted.
            verify(child1, never()).close();
        }

        verify(child1).close();
    }

    private BasePushParser mockChild() {
        BasePushParser child = mock(BasePushParser.class);
        when(child.nextLine(any())).thenReturn(true);
        return child;
    }
}
