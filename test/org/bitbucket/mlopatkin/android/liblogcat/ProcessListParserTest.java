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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProcessListParserTest {

    private static final String PS_LINE_1 = "root      1     0     244    228   fg  ffffffff 00000000 S /init";

    @Test
    public void testParseProcessListLine() {
        assertTrue(ProcessListParser.parseProcessListLine(PS_LINE_1).matches());
    }

}
