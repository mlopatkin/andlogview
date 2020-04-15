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

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FilterPanelModelTest {
    @Mock
    private FilterPanelModel.FilterPanelModelListener listener;

    @Mock
    private PanelFilter filter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddFilterNotifies() throws Exception {
        FilterPanelModel model = new FilterPanelModel();
        model.addListener(listener);

        model.addFilter(filter);

        verify(listener).onFilterAdded(filter);
    }

    @Test
    public void testRemoveFilterNotifies() throws Exception {
        FilterPanelModel model = new FilterPanelModel();
        model.addFilter(filter);
        model.addListener(listener);

        model.removeFilter(filter);

        verify(listener).onFilterRemoved(filter);
        verify(filter).delete();
    }

    @Test
    public void testRemoveFilterThatWasntAddedDoesntNotify() throws Exception {
        FilterPanelModel model = new FilterPanelModel();
        model.addListener(listener);

        model.removeFilter(filter);

        verify(listener, never()).onFilterRemoved(any(PanelFilter.class));
        verify(filter, never()).delete();
    }

    @Test
    public void testReplaceFilterNotifies() throws Exception {
        FilterPanelModel model = new FilterPanelModel();
        model.addFilter(filter);
        model.addListener(listener);

        PanelFilter newFilter = mock(PanelFilter.class);
        model.replaceFilter(filter, newFilter);

        verify(listener).onFilterReplaced(filter, newFilter);
    }

    @Test
    public void testEnableFilter() throws Exception {
        FilterPanelModel model = new FilterPanelModel();
        model.addFilter(filter);
        model.addListener(listener);

        model.setFilterEnabled(filter, true);

        verify(filter).setEnabled(true);
        verify(listener).onFilterEnabled(filter, true);

        model.setFilterEnabled(filter, false);

        verify(filter).setEnabled(false);
        verify(listener).onFilterEnabled(filter, false);
    }

    @Test
    public void testRemoveFromDeleteIsNoOp() throws Exception {
        final FilterPanelModel model = new FilterPanelModel();
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
        model.removeFilter(filter);

        // notification fires only once and nothing bad happens
        verify(listener).onFilterRemoved(filter);
    }

    @Test
    public void testGetFilters() throws Exception {
        FilterPanelModel model = new FilterPanelModel();

        ImmutableList<PanelFilterView> filters = model.getFilters();
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
