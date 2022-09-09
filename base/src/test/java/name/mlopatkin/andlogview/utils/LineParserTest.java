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

package name.mlopatkin.andlogview.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class LineParserTest {
    @Test
    void pushedLinePropagatesToState() {
        List<String> items = new ArrayList<>();
        LineParser.State pushToList = line -> {
            items.add(line);
            return null;
        };

        LineParser parser = new LineParser(pushToList);
        parser.nextLine("line");

        assertThat(items, is(ImmutableList.of("line")));
    }

    @Test
    void returningNullKeepsInTheCurrentState() {
        List<String> items = new ArrayList<>();
        LineParser.State pushToList = line -> {
            items.add(line);
            return null;
        };

        LineParser parser = new LineParser(pushToList);
        parser.nextLine("line 1");
        parser.nextLine("line 2");

        assertThat(items, is(ImmutableList.of("line 1", "line 2")));
    }

    @Test
    void returningNewStateUpdatesCurrentState() {
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();

        LineParser.State pushToSecond = line -> {
            second.add(line);
            return null;
        };

        LineParser.State pushToFirst = line -> {
            first.add(line);
            return pushToSecond;
        };

        LineParser parser = new LineParser(pushToFirst);
        parser.nextLine("line 1");
        parser.nextLine("line 2");

        assertThat(first, is(ImmutableList.of("line 1")));
        assertThat(second, is(ImmutableList.of("line 2")));
    }
}
