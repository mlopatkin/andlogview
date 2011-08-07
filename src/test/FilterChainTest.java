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
package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordFilter;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;
import org.bitbucket.mlopatkin.android.liblogcat.SingleTagFilter;
import org.bitbucket.mlopatkin.android.logviewer.FilterChain;
import org.bitbucket.mlopatkin.android.logviewer.FilteringMode;
import org.junit.Test;

public class FilterChainTest {

    private static final LogRecord RECORD1 = LogRecordParser
            .createThreadtimeRecord(LogRecordParser
                    .parseLogRecordLine("08-03 16:21:35.538    98   231 V AudioFlinger: start(4117), calling thread 172"));

    private static final LogRecord RECORD2 = LogRecordParser
            .createThreadtimeRecord(LogRecordParser
                    .parseLogRecordLine("08-03 16:21:35.538    98   231 V NotAudioFlinger: start(4117), calling thread 172"));

    private static final LogRecordFilter TAG_FILTER_MATCH = new SingleTagFilter("AudioFlinger");

    @Test
    public void testDefaultFiltering() {
        FilterChain chain = new FilterChain();
        for (FilteringMode mode : FilteringMode.values()) {
            assertEquals("Default filtering value failed for " + mode, mode.getDefaultResult(),
                    chain.checkFilter(mode, RECORD1));
        }
    }

    private void helperTestModeShow(FilteringMode mode) {
        FilterChain chain = new FilterChain();
        chain.addFilter(mode, TAG_FILTER_MATCH);
        assertTrue(chain.checkFilter(mode, RECORD1));
        assertFalse(chain.checkFilter(mode, RECORD2));
    }

    @Test
    public void testIncludeShow() {
        helperTestModeShow(FilteringMode.SHOW);
    }

    @Test
    public void testIncludeHide() {
        helperTestModeShow(FilteringMode.HIDE);
    }

    @Test
    public void testIncludeHighlight() {
        helperTestModeShow(FilteringMode.HIGHLIGHT);
    }
}
