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
package name.mlopatkin.andlogview.filters;

import static name.mlopatkin.andlogview.test.TestData.MATCH_ALL;
import static name.mlopatkin.andlogview.test.TestData.MATCH_FIRST;
import static name.mlopatkin.andlogview.test.TestData.RECORD1;
import static name.mlopatkin.andlogview.test.TestData.RECORD2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import org.junit.Before;
import org.junit.Test;

import java.util.function.Predicate;

public class FilterChainTest {
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
        chain.addFilter(FilteringMode.HIDE, filter(MATCH_FIRST));
        assertFalse(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    @Test
    public void testShow() throws Exception {
        chain.addFilter(FilteringMode.SHOW, filter(MATCH_FIRST));
        assertTrue(chain.shouldShow(RECORD1));
        assertFalse(chain.shouldShow(RECORD2));
    }

    @Test
    public void testHidePrecedesShow() throws Exception {
        chain.addFilter(FilteringMode.HIDE, filter(MATCH_ALL));
        chain.addFilter(FilteringMode.SHOW, filter(MATCH_ALL));
        assertFalse(chain.shouldShow(RECORD1));
        assertFalse(chain.shouldShow(RECORD2));
    }

    @Test
    public void testRemoveFilter() throws Exception {
        chain.addFilter(FilteringMode.HIDE, filter(MATCH_ALL));
        chain.removeFilter(FilteringMode.HIDE, filter(MATCH_ALL));
        assertTrue(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    @Test
    public void testReplace() throws Exception {
        chain.addFilter(FilteringMode.HIDE, filter(MATCH_ALL));
        chain.replaceFilter(FilteringMode.HIDE, filter(MATCH_ALL), filter(MATCH_FIRST));

        assertFalse(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    @Test
    public void testSetEnabled() throws Exception {
        chain.addFilter(FilteringMode.HIDE, filter(MATCH_FIRST));
        chain.setFilterEnabled(FilteringMode.HIDE, filter(MATCH_FIRST), false);

        assertTrue(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
        chain.setFilterEnabled(FilteringMode.HIDE, filter(MATCH_FIRST), true);
        assertFalse(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    private static Filter filter(Predicate<LogRecord> p) {
        class StubFilter implements Filter {
            final Predicate<LogRecord> predicate = p;

            @Override
            public FilteringMode getMode() {
                // TODO(mlopatkin) wut??
                return FilteringMode.SHOW;
            }

            @Override
            public boolean test(LogRecord logRecord) {
                return predicate.test(logRecord);
            }

            @Override
            public int hashCode() {
                return predicate.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof StubFilter filter) {
                    return predicate.equals(filter.predicate);
                }
                return false;
            }
        }

        return new StubFilter();
    }
}
