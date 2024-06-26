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

import static name.mlopatkin.andlogview.filters.ToggleFilter.hide;
import static name.mlopatkin.andlogview.filters.ToggleFilter.show;
import static name.mlopatkin.andlogview.test.TestData.MATCH_ALL;
import static name.mlopatkin.andlogview.test.TestData.MATCH_FIRST;
import static name.mlopatkin.andlogview.test.TestData.RECORD1;
import static name.mlopatkin.andlogview.test.TestData.RECORD2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

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
        chain.addFilter(hide(MATCH_FIRST));
        assertFalse(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    @Test
    public void testShow() throws Exception {
        chain.addFilter(show(MATCH_FIRST));
        assertTrue(chain.shouldShow(RECORD1));
        assertFalse(chain.shouldShow(RECORD2));
    }

    @Test
    public void testHidePrecedesShow() throws Exception {
        chain.addFilter(hide(MATCH_ALL));
        chain.addFilter(show(MATCH_ALL));
        assertFalse(chain.shouldShow(RECORD1));
        assertFalse(chain.shouldShow(RECORD2));
    }

    @Test
    public void testRemoveFilter() throws Exception {
        chain.addFilter(hide(MATCH_ALL));
        chain.removeFilter(hide(MATCH_ALL));
        assertTrue(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    @Test
    public void testReplace() throws Exception {
        chain.addFilter(hide(MATCH_ALL));
        chain.replaceFilter(hide(MATCH_ALL), hide(MATCH_FIRST));

        assertFalse(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    @Test
    public void testSetEnabled() throws Exception {
        chain.addFilter(hide(MATCH_FIRST));
        chain.replaceFilter(hide(MATCH_FIRST), hide(MATCH_FIRST).disabled());

        assertTrue(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
        chain.replaceFilter(hide(MATCH_FIRST).disabled(), hide(MATCH_FIRST));
        assertFalse(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

}
