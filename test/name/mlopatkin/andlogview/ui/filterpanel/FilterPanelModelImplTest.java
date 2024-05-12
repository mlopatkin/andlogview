/*
 * Copyright 2014 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.filterpanel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

public class FilterPanelModelImplTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private FilterPanelModel.FilterPanelModelListener<PanelFilter> listener;

    @Mock
    private PanelFilter filter;

    @Test
    public void testAddFilterNotifies() throws Exception {
        FilterPanelModelImpl model = new FilterPanelModelImpl();
        model.addListener(listener);

        model.addFilter(filter);

        verify(listener).onFilterAdded(filter);
    }

    @Test
    public void testRemoveFilterNotifies() throws Exception {
        FilterPanelModelImpl model = new FilterPanelModelImpl();
        model.addFilter(filter);
        model.addListener(listener);

        model.removeFilter(filter);

        verify(listener).onFilterRemoved(filter);
    }

    @Test
    public void testRemoveFilterThatWasntAddedDoesntNotify() throws Exception {
        FilterPanelModelImpl model = new FilterPanelModelImpl();
        model.addListener(listener);

        model.removeFilter(filter);

        verify(listener, never()).onFilterRemoved(any(PanelFilter.class));
        verify(filter, never()).delete();
    }

    @Test
    public void testReplaceFilterNotifies() throws Exception {
        FilterPanelModelImpl model = new FilterPanelModelImpl();
        model.addFilter(filter);
        model.addListener(listener);

        PanelFilter newFilter = mock(PanelFilter.class);
        model.replaceFilter(filter, newFilter);

        verify(listener).onFilterReplaced(filter, newFilter);
    }

    @Test
    public void testEnableFilter() throws Exception {
        FilterPanelModelImpl model = new FilterPanelModelImpl();
        model.addFilter(filter);
        model.addListener(listener);

        model.setFilterEnabled(filter, true);

        verify(filter).setEnabled(true);

        model.setFilterEnabled(filter, false);

        verify(filter).setEnabled(false);
    }

    @Test
    public void testRemoveFromDeleteIsNoOp() throws Exception {
        final FilterPanelModelImpl model = new FilterPanelModelImpl();
        PanelFilter filter = new PanelFilter() {
            @Override
            public void setEnabled(boolean enabled) {}

            @Override
            public void openFilterEditor() {}

            @Override
            public void delete() {
                model.removeFilter(this);
            }

            @Override
            public String getTooltip() {
                return "";
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        };

        model.addFilter(filter);
        model.addListener(listener);
        model.removeFilterForView(filter);

        // notification fires only once and nothing bad happens
        verify(listener).onFilterRemoved(filter);
    }

    @Test
    public void testGetFilters() throws Exception {
        FilterPanelModelImpl model = new FilterPanelModelImpl();

        var filters = model.getFilters();
        assertTrue(filters.isEmpty());

        model.addFilter(filter);

        assertTrue("Must be a copy, not a view", filters.isEmpty());
        filters = model.getFilters();
        assertEquals(1, filters.size());

        model.removeFilter(filter);
        assertEquals("Must be a copy, not a view", 1, filters.size());
        filters = model.getFilters();
        assertTrue(filters.isEmpty());
    }
}
