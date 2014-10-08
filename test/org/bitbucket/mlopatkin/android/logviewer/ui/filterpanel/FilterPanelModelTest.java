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

package org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
    }

    @Test
    public void testRemoveFilterThatWasntAddedDoesntNotify() throws Exception {
        FilterPanelModel model = new FilterPanelModel();
        model.addListener(listener);

        model.removeFilter(filter);

        verify(listener, never()).onFilterRemoved(any(PanelFilter.class));
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

}