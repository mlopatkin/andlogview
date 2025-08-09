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

package name.mlopatkin.andlogview.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class MyStringUtilsAbbreviateMiddleTest {
    @SuppressWarnings("NotNullFieldNotInitialized")
    @Parameterized.Parameter
    public String stringToAbbreviate;

    @Parameterized.Parameter(1)
    public char replacement;

    @Parameterized.Parameter(2)
    public int maxLength;

    @Parameterized.Parameter(3)
    public int prefixLength;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @Parameterized.Parameter(4)
    public String expectedResult;

    @Parameterized.Parameters(name = "{index}: '{0}' => '{4}'")
    public static List<Object[]> getParams() {
        return Arrays.asList(new Object[][] {
                {"hello", '*', 3, 1, "h*o"},
                {"hello", '*', 4, 1, "h*lo"},
                {"hello", '*', 4, 2, "he*o"},
                {"hello", '*', 5, 1, "hello"},
                {"hello", '*', 5, 2, "hello"},
                {"hello", '*', 5, 3, "hello"},
                {"hello", '.', 10, 1, "hello"},
                {"hello world", '.', 5, 1, "h.rld"},
                {"hello world", '.', 3, 1, "h.d"},
                {"hello world", '!', 5, 3, "hel!d"},
                {"h", '.', 3, 1, "h"},
                {"", '.', 3, 1, ""},
                });
    }


    @Test
    public void abbreviateMiddleTest() {
        assertEquals(expectedResult,
                MyStringUtils.abbreviateMiddle(stringToAbbreviate, replacement, maxLength, prefixLength));
    }
}
