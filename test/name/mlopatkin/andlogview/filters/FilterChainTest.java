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
import static name.mlopatkin.andlogview.filters.ToggleFilter.index;
import static name.mlopatkin.andlogview.filters.ToggleFilter.show;
import static name.mlopatkin.andlogview.test.TestData.MATCH_ALL;
import static name.mlopatkin.andlogview.test.TestData.MATCH_FIRST;
import static name.mlopatkin.andlogview.test.TestData.RECORD1;
import static name.mlopatkin.andlogview.test.TestData.RECORD2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class FilterChainTest {
    private final MutableFilterModel model = MutableFilterModel.create();
    private final FilterChain chain = new FilterChain(model);

    @Test
    public void testDefaultModeAcceptsAll() throws Exception {
        // default mode is to accept all
        assertTrue(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    @Test
    public void testHide() throws Exception {
        model.addFilter(hide(MATCH_FIRST));
        assertFalse(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    @Test
    public void testShow() throws Exception {
        model.addFilter(show(MATCH_FIRST));
        assertTrue(chain.shouldShow(RECORD1));
        assertFalse(chain.shouldShow(RECORD2));
    }

    @Test
    public void testHidePrecedesShow() throws Exception {
        model.addFilter(hide(MATCH_ALL));
        model.addFilter(show(MATCH_ALL));
        assertFalse(chain.shouldShow(RECORD1));
        assertFalse(chain.shouldShow(RECORD2));
    }

    @Test
    public void testRemoveFilter() throws Exception {
        var filter = hide(MATCH_ALL);
        model.addFilter(filter);
        model.removeFilter(filter);
        assertTrue(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    @Test
    public void testReplace() throws Exception {
        var filter1 = hide(MATCH_ALL);
        model.addFilter(filter1);
        model.replaceFilter(filter1, hide(MATCH_FIRST));

        assertFalse(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    @Test
    public void testSetEnabled() throws Exception {
        var filter1 = hide(MATCH_FIRST);
        var filter2 = filter1.disabled();
        model.addFilter(filter1);
        model.replaceFilter(filter1, filter2);

        assertTrue(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
        model.replaceFilter(filter2, filter1);
        assertFalse(chain.shouldShow(RECORD1));
        assertTrue(chain.shouldShow(RECORD2));
    }

    @Test
    public void doesNotifyWhenShowFilterAdded() {
        FilterChain.Observer obs = mock();
        chain.asObservable().addObserver(obs);
        model.addFilter(show(MATCH_FIRST));

        verify(obs).onFiltersChanged();
    }

    @Test
    public void doesNotifyWhenHideFilterAdded() {
        FilterChain.Observer obs = mock();
        chain.asObservable().addObserver(obs);
        model.addFilter(hide(MATCH_FIRST));

        verify(obs).onFiltersChanged();
    }

    @Test
    public void doesNotNotifyWhenUnrelatedFilterChanges() {
        FilterChain.Observer obs = mock();
        chain.asObservable().addObserver(obs);
        model.addFilter(index(MATCH_FIRST));

        verify(obs, never()).onFiltersChanged();
    }
}
