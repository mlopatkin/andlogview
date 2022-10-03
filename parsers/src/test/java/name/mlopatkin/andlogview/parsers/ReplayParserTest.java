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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class ReplayParserTest {

    @Test
    void replayParserForwardsInputToDelegate() {
        BasePushParser delegate = createMockParser();
        try (ReplayParser<BasePushParser> r = new ReplayParser<>(delegate)) {
            r.nextLine("1");
            r.nextLine("2");
        }

        InOrder order = inOrder(delegate);
        order.verify(delegate).nextLine("1");
        order.verify(delegate).nextLine("2");
        order.verify(delegate).close();
    }

    @Test
    void replayingForwardsInputToTheArgument() {
        try (BasePushParser target = createMockParser()) {
            try (ReplayParser<BasePushParser> r = new ReplayParser<>(createMockParser())) {
                r.nextLine("1");
                r.nextLine("2");
                r.replayInto(target);
            }
            InOrder order = inOrder(target);
            order.verify(target).nextLine("1");
            order.verify(target).nextLine("2");
        }
    }

    @Test
    void cannotReplayAfterClosing() {
        try (BasePushParser target = createMockParser()) {
            ReplayParser<BasePushParser> r = new ReplayParser<>(createMockParser());
            try {
                r.nextLine("1");
                r.nextLine("2");
            } finally {
                r.close();
            }

            r.replayInto(target);
            verifyNoInteractions(target);
        }
    }

    @Test
    void replayStopsWhenTargetStops() {
        try (BasePushParser target = createMockParser()) {
            when(target.nextLine(any())).thenReturn(true, false);
            try (ReplayParser<BasePushParser> r = new ReplayParser<>(createMockParser())) {
                r.nextLine("1");
                r.nextLine("2");
                r.nextLine("3");
                assertThat(r.replayInto(target)).isFalse();
            }
            InOrder order = inOrder(target);
            order.verify(target).nextLine("1");
            order.verify(target).nextLine("2");
            order.verifyNoMoreInteractions();
        }
    }


    private BasePushParser createMockParser() {
        BasePushParser parser = mock(BasePushParser.class);
        when(parser.nextLine(any())).thenReturn(true);
        return parser;
    }
}
