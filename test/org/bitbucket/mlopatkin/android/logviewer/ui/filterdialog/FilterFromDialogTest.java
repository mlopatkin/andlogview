/*
 * Copyright 2015 Mikhail Lopatkin
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
 *
 *
 */

package org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordUtils;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FilterFromDialogTest {

    private FilterFromDialog filter = new FilterFromDialog();

    @Before
    public void setUp() throws Exception {
        filter.setMode(FilteringMode.HIDE);
    }

    @Test
    public void testTag_Single() throws Exception {
        LogRecord tag1 = LogRecordUtils.forTag("TAG1");
        LogRecord tag2 = LogRecordUtils.forTag("TAG2");

        filter.setTags(Collections.singletonList("TAG1"));
        filter.initialize();

        assertTrue(filter.apply(tag1));
        assertFalse(filter.apply(tag2));
    }


    @Test
    public void testTag_Multiple() throws Exception {
        LogRecord tag1 = LogRecordUtils.forTag("TAG1");
        LogRecord tag2 = LogRecordUtils.forTag("TAG2");
        LogRecord tag3 = LogRecordUtils.forTag("TAG3");

        filter.setTags(Arrays.asList("TAG1", "TAG2"));
        filter.initialize();

        assertTrue(filter.apply(tag1));
        assertTrue(filter.apply(tag2));
        assertFalse(filter.apply(tag3));
    }

    @Test
    public void testTag_CaseInsensitive() throws Exception {
        LogRecord tag1 = LogRecordUtils.forTag("TAG1");
        LogRecord tag2 = LogRecordUtils.forTag("TaG1");
        LogRecord tag3 = LogRecordUtils.forTag("TAG3");

        filter.setTags(Collections.singletonList("tag1"));
        filter.initialize();

        assertTrue(filter.apply(tag1));
        assertTrue(filter.apply(tag2));
        assertFalse(filter.apply(tag3));
    }

    @Test
    public void testTag_incompleteTagDoesntMatch() throws Exception {
        List<LogRecord> tags = Arrays.asList(
                LogRecordUtils.forTag("Middle Tag Middle"),
                LogRecordUtils.forTag("Tag Begin"),
                LogRecordUtils.forTag("End Tag"),
                LogRecordUtils.forTag("middletagwithoutspaces"),
                LogRecordUtils.forTag("#tag#"));

        filter.setTags(Collections.singletonList("Tag"));
        filter.initialize();

        for (LogRecord r : tags) {
            assertFalse(filter.apply(r));
        }
    }

    @Test
    public void testTag_Regexp() throws Exception {
        LogRecord tag1 = LogRecordUtils.forTag("TAG1");
        LogRecord tag2 = LogRecordUtils.forTag("TAG2");
        LogRecord tag3 = LogRecordUtils.forTag("TAG3");

        filter.setTags(Collections.singletonList("/TAG[12]/"));
        filter.initialize();

        assertTrue(filter.apply(tag1));
        assertTrue(filter.apply(tag2));
        assertFalse(filter.apply(tag3));
    }

    @Test
    public void testTag_incompleteTagMatchRegexp() throws Exception {
        List<LogRecord> tags = Arrays.asList(
                LogRecordUtils.forTag("Middle Tag Middle"),
                LogRecordUtils.forTag("Tag Begin"),
                LogRecordUtils.forTag("End Tag"),
                LogRecordUtils.forTag("middletagwithoutspaces"),
                LogRecordUtils.forTag("#tag#"));

        filter.setTags(Collections.singletonList("/Tag/"));
        filter.initialize();

        for (LogRecord r : tags) {
            assertTrue(r.toString(), filter.apply(r));
        }
    }

    @Test
    public void testPidsAppNames_ifAppNameAndPidAreSpecifiedThenEitherOneShouldMatch() throws Exception {
        LogRecord record1 = LogRecordUtils.forPidAndAppName(1, "app1");
        LogRecord record2 = LogRecordUtils.forPidAndAppName(2, "app2");
        LogRecord record3 = LogRecordUtils.forPidAndAppName(3, "app3");

        filter.setPids(Collections.singletonList(1));
        filter.setApps(Collections.singletonList("app2"));

        filter.initialize();
        assertTrue(filter.apply(record1));
        assertTrue(filter.apply(record2));
        assertFalse(filter.apply(record3));
    }

    @Test
    public void testAppNamesRegexMatchThrowsAppropriateExceptions() throws Exception {
        assertInitializeThrowsExceptionWithRequestValue(
                new FilterFromDialog().setApps(Collections.singletonList(" ")), " ");

        assertInitializeThrowsExceptionWithRequestValue(
                new FilterFromDialog().setApps(Collections.singletonList("/?/")), "/?/");

        assertInitializeThrowsExceptionWithRequestValue(
                new FilterFromDialog().setTags(Collections.singletonList(" ")), " ");

        assertInitializeThrowsExceptionWithRequestValue(
                new FilterFromDialog().setTags(Collections.singletonList("/?/")), "/?/");

        assertInitializeThrowsExceptionWithRequestValue(
                new FilterFromDialog().setMessagePattern("/?/"), "/?/");
    }


    private static void assertInitializeThrowsExceptionWithRequestValue(FilterFromDialog filter,
                                                                        String expectedRequest) {
        try {
            filter.initialize();
            fail("Exception expected");
        } catch (RequestCompilationException e) {
            assertEquals(expectedRequest, e.getRequestValue());
        }
    }
}
