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
package org.bitbucket.mlopatkin.android.logviewer.filters;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;
import org.bitbucket.mlopatkin.android.liblogcat.filters.LogRecordFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.SingleTagFilter;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilterChainTest {

    private static final LogRecord RECORD1 = LogRecordParser.parseThreadTime(
            Buffer.UNKNOWN,
            "08-03 16:21:35.538    98   231 V AudioFlinger: start(4117), calling thread 172",
            Collections.<Integer, String>emptyMap());

    private static final LogRecord RECORD2 = LogRecordParser.parseThreadTime(
            Buffer.UNKNOWN,
            "08-03 16:21:35.538    98   231 V NotAudioFlinger: start(4117), calling thread 172",
            Collections.<Integer, String>emptyMap());

    private static final LogRecordFilter MATCH_FIRST = new SingleTagFilter("AudioFlinger");
    private static final LogRecordFilter MATCH_SECOND = new SingleTagFilter("NotAudioFlinger");

    private static final Predicate<LogRecord> MATCH_ALL = Predicates.alwaysTrue();
    private static final Predicate<LogRecord> MATCH_NONE = Predicates.alwaysFalse();

    private FilterChain chain;

    @Before
    public void setUp() throws Exception {
        chain = new FilterChain();
    }

    @Test
    public void testDefaultModeAcceptsAll() throws Exception {
        // default mode is to accept all
        assertTrue(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    @Test
    public void testHide() throws Exception {
        chain.addFilter(FilteringMode.HIDE, MATCH_FIRST);
        assertFalse(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    @Test
    public void testShow() throws Exception {
        chain.addFilter(FilteringMode.SHOW, MATCH_FIRST);
        assertTrue(chain.shouldShow(RECORD1));
        assertFalse(chain.shouldShow(RECORD2));
    }

    @Test
    public void testHidePrecedesShow() throws Exception {
        chain.addFilter(FilteringMode.HIDE, MATCH_ALL);
        chain.addFilter(FilteringMode.SHOW, MATCH_ALL);
        assertFalse(chain.shouldShow(RECORD1));
        assertFalse(chain.shouldShow(RECORD2));
    }

    @Test
    public void testRemoveFilter() throws Exception {
        chain.addFilter(FilteringMode.HIDE, MATCH_ALL);
        chain.removeFilter(FilteringMode.HIDE, MATCH_ALL);
        assertTrue(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }
}
