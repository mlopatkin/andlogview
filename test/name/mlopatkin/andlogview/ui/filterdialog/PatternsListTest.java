/*
 * Copyright 2020 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.filterdialog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

class PatternsListTest {
    static Stream<Arguments> getValidSplitTestCases() {
        return Stream.of(
                arguments("", splits()),
                arguments("a", splits("a")),
                arguments("hello", splits("hello")),
                arguments("  hello ", splits("hello")),
                arguments(" , hello  ,  ", splits("hello")),

                arguments("a,b", splits("a", "b")),
                arguments(",a,b,", splits("a", "b")),
                arguments("a, ,c", splits("a", "c")),
                arguments("hello, there", splits("hello", "there")),
                arguments(" ,  , hello ,there ,", splits("hello", "there")),

                arguments("a,b,c", splits("a", "b", "c")),
                arguments("hello, there, general kenobi  ", splits("hello", "there", "general kenobi")),
                arguments("  a  , , , b, c", splits("a", "b", "c")),

                arguments("//", splits("//")),  // Invalid search pattern but should be properly handled
                arguments("/hello/", splits("/hello/")),
                arguments("/hello///there/", splits("/hello///there/")),
                arguments("/hello/ /there/", splits("/hello/ /there/")),
                // Compatibility tests
                arguments("/not a regular, expression/", splits("/not a regular", "expression/")),
                arguments("/regular/expression middle//slashes/", splits("/regular/expression middle//slashes/")),

                arguments("/hello/, there", splits("/hello/", "there")),
                arguments("  ,/ hello /, /there / ,", splits("/ hello /", "/there /")),

                // Quoting tests
                arguments("`hello`", splits("hello")),
                arguments("hel/lo", splits("hel/lo")),
                arguments("hel`lo", splits("hel`lo")),
                arguments("`hel,lo`", splits("hel,lo")),
                arguments("`hel,lo`,  `olleh `", splits("hel,lo", "olleh ")),
                arguments("  `  `  ,  `\t\t`  ", splits("  ", "\t\t")),
                arguments("  /  /  ,  /\t\t/  ", splits("/  /", "/\t\t/")),
                arguments("`quote1`,  /quote2/  ", splits("quote1", "/quote2/")),
                arguments("``", splits("")),
                arguments("//,/``/", splits("//", "/``/")),
                arguments("`hello``there`", splits("hello`there")),
                arguments("/hello``there/", splits("/hello``there/")),
                arguments("`hello/there`", splits("hello/there")),
                arguments("/hello`there/", splits("/hello`there/")),
                arguments("`hello````there`", splits("hello``there")),
                arguments("`hello``there``general`", splits("hello`there`general")),
                arguments("```hello```", splits("`hello`")),
                arguments("///hello///", splits("///hello///")),
                arguments("````", splits("`")),

                // Separator escaping tests
                arguments(",", splits()),
                arguments(",,", splits(",")),
                arguments(",,,", splits(",")),
                arguments("`,,`", splits(",,")),
                arguments("/,,/", splits("/,/")),
                arguments("a,,b,,", splits("a,b,")),
                arguments("a`,,`b", splits("a`,`b")),
                arguments(",,a,,", splits(",a,")),
                arguments("  ,,a,,  ", splits(",a,")),
                arguments("  ,,a,,,`,`", splits(",a,", ",")),
                arguments("`quoted`,,double comma", splits("quoted", "double comma")),
                arguments("double comma,,`quoted`", splits("double comma,`quoted`")),

                arguments("", splits())
        );
    }

    @ParameterizedTest(name = "[{index}]: {0}")
    @MethodSource("getValidSplitTestCases")
    void validPatternListIsSplit(String toSplit, List<String> expectedResult) throws Exception {
        assertEquals(expectedResult, PatternsList.split(toSplit), toSplit);
    }

    @ParameterizedTest(name = "[{index}]: {0}")
    @ValueSource(strings = {
            "`",
            "`he",
            "`he/",
            "`hello```there`",
            "`hello` `there`",
            "`hello``",
            "hello,`there",
            "`/hello/`",
    })
    void invalidPatternListCausesException(String toSplit) throws Exception {
        assertThrows(PatternsList.FormatException.class, () -> PatternsList.split(toSplit), toSplit);
    }

    static Stream<Arguments> getJoinParameters() {
        return Stream.of(
                arguments(splits(""), "``"),
                arguments(splits(" "), "` `"),
                arguments(splits("\t\n"), "`\t\n`"),
                arguments(splits("a"), "a"),
                arguments(splits("`"), "````"),
                arguments(splits(" ` "), "` `` `"),
                arguments(splits("a ` b"), "a ` b"),
                arguments(splits("a b"), "a b"),
                arguments(splits("a", "b"), "a, b"),
                arguments(splits("a, b", "c"), "`a, b`, c"),
                arguments(splits("a, b", "`c, d`"), "`a, b`, ```c, d```"),
                arguments(splits("/a/"), "/a/"),
                arguments(splits("//"), "//"),
                arguments(splits("/a,b/"), "/a,,b/"),
                arguments(splits("/a/b/"), "/a/b/"),
                arguments(splits(" /a/ "), "` /a/ `"),
                arguments(splits("/"), "/"),
                arguments(splits("aaaa/"), "aaaa/"),
                arguments(splits(" /"), "` /`"),
                arguments(splits(" // "), "` // `"),
                arguments(splits("/`"), "/`"),
                arguments(splits("`/"), "```/`"),
                arguments(splits("a`/b"), "a`/b"),
                arguments(splits("/what/,is/", "`this,`", "that,stands,,before", "me"),
                        "/what/,,is/, ```this,```, `that,stands,,before`, me"),
                arguments(splits(), "")
        );
    }

    @ParameterizedTest(name = "[{index}]: {0}")
    @MethodSource("getJoinParameters")
    public void joiningIsCorrect(List<String> toJoin, String expectedResult) throws Exception {
        assertEquals(expectedResult, PatternsList.join(toJoin), expectedResult);
    }

    @ParameterizedTest(name = "[{index}]: {0}")
    @MethodSource("getJoinParameters")
    public void joiningIsSane(List<String> toJoin, String expectedJoin) throws Exception {
        assertEquals(toJoin, PatternsList.split(PatternsList.join(toJoin)), expectedJoin);
    }

    private static List<String> splits(String... parts) {
        return Arrays.asList(parts);
    }
}
