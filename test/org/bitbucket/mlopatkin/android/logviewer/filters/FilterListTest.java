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

package org.bitbucket.mlopatkin.android.logviewer.filters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

public class FilterListTest {

    public static final Predicate<LogRecord> PREDICATE = Predicates.alwaysTrue();
    private final FilterList filterList = new FilterList();

    private static class BaseFilter extends Filter {

        public BaseFilter() {
            super(PREDICATE);
        }

        @Override
        public void toJson(JSONObject obj) throws JSONException {
        }
    }

    private static class Filter1 extends BaseFilter {

    }

    private static class Filter2 extends Filter1 {

    }

    private static class Filter3 extends BaseFilter {

    }

    private final Filter1 filter1 = new Filter1();
    private final Filter2 filter2 = new Filter2();
    private final Filter3 filter3 = new Filter3();

    @Test
    public void testGetFiltersFor_returnsEmptyList() throws Exception {
        FilterList filterList = new FilterList();

        assertEquals(Collections.<Filter1>emptyList(),
                filterList.getFiltersFor(Filter1.class));
        assertEquals(Collections.<Filter2>emptyList(),
                filterList.getFiltersFor(Filter2.class));
        assertEquals(Collections.<Filter>emptyList(),
                filterList.getFiltersFor(Filter.class));
    }

    @Test
    public void testGetFiltersFor() throws Exception {
        filterList.addFilter(filter1, Filter.class, Filter1.class);
        filterList.addFilter(filter2, Filter.class, Filter1.class, Filter2.class);
        filterList.addFilter(filter3, Filter.class, Filter3.class);

        assertEquals(Arrays.<Filter>asList(filter1, filter2, filter3),
                filterList.getFiltersFor(Filter.class));
        assertEquals(Arrays.asList(filter1, filter2),
                filterList.getFiltersFor(Filter1.class));
        assertEquals(Arrays.asList(filter2),
                filterList.getFiltersFor(Filter2.class));
        assertEquals(Arrays.asList(filter3),
                filterList.getFiltersFor(Filter3.class));
    }

    @Test
    public void testGetFiltersFor_DoesntFlattenClasses() throws Exception {
        filterList.addFilter(filter1, Filter.class, Filter1.class);
        filterList.addFilter(filter2, Filter1.class, Filter2.class);
        filterList.addFilter(filter3, Filter.class, Filter3.class);

        assertEquals(Arrays.<Filter>asList(filter1, filter3),
                filterList.getFiltersFor(Filter.class));
        assertEquals(Arrays.asList(filter1, filter2),
                filterList.getFiltersFor(Filter1.class));
        assertEquals(Arrays.asList(filter2),
                filterList.getFiltersFor(Filter2.class));
        assertEquals(Arrays.asList(filter3),
                filterList.getFiltersFor(Filter3.class));
    }

    @Test
    public void testGetFiltersFor_returnsView() throws Exception {
        filterList.addFilter(filter1, Filter.class, Filter1.class);

        List<Filter> filters = filterList.getFiltersFor(Filter.class);
        assertEquals("Sanity check", Arrays.<Filter>asList(filter1), filters);

        filterList.addFilter(filter2, Filter.class, Filter1.class);
        assertEquals(Arrays.<Filter>asList(filter1, filter2),
                filters);
    }

    @Test
    public void testAddFilter_invokesCallbacks() throws Exception {
        @SuppressWarnings("unchecked")
        FilterList.FilterListChangedListener<Filter1> filter1Listener = mock(
                FilterList.FilterListChangedListener.class);

        filterList.addListener(filter1Listener, Filter1.class);
        filterList.addFilter(filter1, Filter1.class);

        verify(filter1Listener).onFilterAdded(filter1);
    }

    @Test
    public void testAddFilter_invokesCallbacksOnce() throws Exception {
        @SuppressWarnings("unchecked")
        FilterList.FilterListChangedListener<Filter> filter1Listener = mock(
                FilterList.FilterListChangedListener.class);

        filterList.addListener(filter1Listener, Filter1.class);
        filterList.addListener(filter1Listener, Filter.class);
        filterList.addFilter(filter1, Filter1.class, Filter.class);

        verify(filter1Listener).onFilterAdded(filter1);
    }

    @Test
    public void testRemoveListener() throws Exception {
        @SuppressWarnings("unchecked")
        FilterList.FilterListChangedListener<Filter1> filter1Listener = mock(
                FilterList.FilterListChangedListener.class);

        filterList.addListener(filter1Listener, Filter1.class);
        filterList.removeListener(filter1Listener);
        filterList.addFilter(filter1, Filter1.class);

        verifyZeroInteractions(filter1Listener);
    }

    @Test
    public void testRemoveListener_removesAllBindings() throws Exception {
        @SuppressWarnings("unchecked")
        FilterList.FilterListChangedListener<Filter> filter1Listener = mock(
                FilterList.FilterListChangedListener.class);

        filterList.addListener(filter1Listener, Filter1.class);
        filterList.addListener(filter1Listener, Filter.class);
        filterList.removeListener(filter1Listener);
        filterList.addFilter(filter1, Filter1.class, Filter.class);

        verifyZeroInteractions(filter1Listener);
    }
}
