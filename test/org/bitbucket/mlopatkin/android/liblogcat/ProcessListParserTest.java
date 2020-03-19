/*
 * Copyright 2011 Mikhail Lopatkin
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
package org.bitbucket.mlopatkin.android.liblogcat;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

import java.util.regex.Matcher;

import static org.junit.Assert.assertThat;

public class ProcessListParserTest {
    private static final String PS_LINE_1 = "root      1     0     244    228   fg  ffffffff 00000000 S /init";
    private static final String PS_LINE_TWO_WORDS_PNAME =
            "root      279   2     0      0     fg  ffffffff 00000000 S Two words";
    private static final String PS_NO_PCY_LINE =
            "root      12    2     0      0         ffffffff 00000000 S sync_supers";
    private static final String PS_NO_NAME_LINE = "root      626   2     0      0     fg  ffffffff 00000000 S ";
    private static final String PS_WCHAN_SYMBOL_LINE =
            "root      29392 2     0      0     fg  cpu_stoppe 00000000 S migration/3";
    private static final String PS_NO_WCHAN_LINE =
            "u0_a251   4851  216   884420 35064 bg             00000000 R com.mapswithme.maps.pro";

    @Test
    public void testParseProcessListLine() {
        assertThat(ProcessListParser.parseProcessListLine(PS_LINE_1), hasPidAndAppName(1, "/init"));
    }

    @Test
    public void testParseProcessNameWithSpaces() {
        assertThat(ProcessListParser.parseProcessListLine(PS_LINE_TWO_WORDS_PNAME), hasPidAndAppName(279, "Two words"));
    }

    @Test
    public void testParseProcessListNoPCY() {
        assertThat(ProcessListParser.parseProcessListLine(PS_NO_PCY_LINE), hasPidAndAppName(12, "sync_supers"));
    }

    @Test
    public void testParseProcessListNoName() {
        assertThat(ProcessListParser.parseProcessListLine(PS_NO_NAME_LINE), hasPidAndAppName(626, ""));
    }

    @Test
    public void testParseProcessListSymbolicWchan() throws Exception {
        assertThat(
                ProcessListParser.parseProcessListLine(PS_WCHAN_SYMBOL_LINE), hasPidAndAppName(29392, "migration/3"));
    }

    @Test
    public void testParseProcessListMissingWchan() throws Exception {
        assertThat(ProcessListParser.parseProcessListLine(PS_NO_WCHAN_LINE),
                hasPidAndAppName(4851, "com.mapswithme.maps.pro"));
    }

    public static org.hamcrest.Matcher<Matcher> hasPidAndAppName(int expectedPid, String expectedAppname) {
        return new TypeSafeDiagnosingMatcher<Matcher>() {
            @Override
            protected boolean matchesSafely(Matcher item, Description mismatchDescription) {
                if (!item.matches()) {
                    mismatchDescription.appendText("hasn't matched regexp");
                    return false;
                }
                int actualPid = ProcessListParser.getPid(item);
                String actualAppname = ProcessListParser.getProcessName(item);
                boolean result = true;
                if (actualPid != expectedPid) {
                    mismatchDescription.appendText("actual pid=").appendValue(actualPid);
                    result = false;
                }
                if (!expectedAppname.equals(actualAppname)) {
                    if (!result) {
                        mismatchDescription.appendText(" and ");
                    }
                    mismatchDescription.appendText("actual app name=").appendValue(actualAppname);
                    result = false;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with pid=")
                        .appendValue(expectedPid)
                        .appendText(" and app name=")
                        .appendValue(expectedAppname);
            }
        };
    }
}
