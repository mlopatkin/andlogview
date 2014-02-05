/*
 * Copyright 2013 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer;

import static org.bitbucket.mlopatkin.android.liblogcat.LogRecord.NO_ID;
import static org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.json.JSONObject;
import org.junit.Test;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordUtils;
import org.bitbucket.mlopatkin.android.liblogcat.filters.LogRecordFilter;

public class FilterDialogDataTest {

    private FilterDialogData data = new FilterDialogData();

    @Test
    public void testCreateFilter_singlePid() throws Exception {
        data.setPids(Arrays.asList(1));

        LogRecordFilter filter = data.makeFilter();

        assertTrue(filter.include(LogRecordUtils.forPid(1)));
        assertFalse(filter.include(LogRecordUtils.forPid(2)));
    }

    @Test
    public void testCreateFilter_multiPid() throws Exception {
        data.setPids(Arrays.asList(1, 3));

        LogRecordFilter filter = data.makeFilter();

        assertTrue(filter.include(LogRecordUtils.forPid(1)));
        assertTrue(filter.include(LogRecordUtils.forPid(3)));
        assertFalse(filter.include(LogRecordUtils.forPid(2)));
    }

    @Test
    public void testCreateFilter_appName() throws Exception {
        data.setApplications(Arrays.asList("app1"));

        LogRecordFilter filter = data.makeFilter();

        assertTrue(filter.include(LogRecordUtils.forAppName("app1")));
        assertFalse(filter.include(LogRecordUtils.forAppName("app2")));
        assertFalse(filter.include(LogRecordUtils.forAppName("app")));
        assertFalse(filter.include(LogRecordUtils.forTag("app1")));
        assertFalse(filter.include(LogRecordUtils.forMessage("app1")));
    }

    @Test
    public void testCreateFilter_pidsAndAppName() throws Exception {
        data.setPids(Arrays.asList(1, 2));
        data.setApplications(Arrays.asList("app1"));

        LogRecordFilter filter = data.makeFilter();

        assertTrue(filter.include(LogRecordUtils.forAppName("app1")));
        assertTrue(filter.include(LogRecordUtils.forPid(1)));
        assertTrue(filter.include(LogRecordUtils.forPid(2)));
        assertFalse(filter.include(LogRecordUtils.forPid(3)));
        assertFalse(filter.include(LogRecordUtils.forAppName("app2")));
        assertFalse(filter.include(LogRecordUtils.forAppName("app")));
        assertFalse(filter.include(LogRecordUtils.forTag("app1")));
        assertFalse(filter.include(LogRecordUtils.forMessage("app1")));
    }

    @Test
    public void testCreateFilter_tag() throws Exception {
        data.setTags(Arrays.asList("tag1"));

        LogRecordFilter filter = data.makeFilter();

        assertTrue(filter.include(LogRecordUtils.forTag("tag1")));
        assertFalse(filter.include(LogRecordUtils.forTag("tag")));
        assertFalse(filter.include(LogRecordUtils.forAppName("tag1")));
        assertFalse(filter.include(LogRecordUtils.forMessage("tag1")));
    }

    @Test
    public void testCreateFilter_tags() throws Exception {
        data.setTags(Arrays.asList("tag1", "tag2"));

        LogRecordFilter filter = data.makeFilter();

        assertTrue(filter.include(LogRecordUtils.forTag("tag1")));
        assertTrue(filter.include(LogRecordUtils.forTag("tag2")));
        assertFalse(filter.include(LogRecordUtils.forTag("tag")));
        assertFalse(filter.include(LogRecordUtils.forAppName("tag1")));
        assertFalse(filter.include(LogRecordUtils.forMessage("tag1")));
    }

    @Test
    public void testCreateFilter_message() throws Exception {
        data.setMessage("abc");

        LogRecordFilter filter = data.makeFilter();

        assertTrue(filter.include(LogRecordUtils.forMessage("abc")));
        assertTrue(filter.include(LogRecordUtils.forMessage("Ends with abc")));
        assertTrue(filter.include(LogRecordUtils.forMessage("abc with starts")));
        assertTrue(filter.include(LogRecordUtils.forMessage("in the abc middle")));
        assertTrue(filter.include(LogRecordUtils.forMessage("Case AbC insensitive")));
        assertFalse(filter.include(LogRecordUtils.forMessage("Contains no _a_b_c_")));
        assertFalse(filter.include(LogRecordUtils.forTag("abc")));
        assertFalse(filter.include(LogRecordUtils.forAppName("abc")));
    }

    @Test
    public void testCreateFilter_message_regex() throws Exception {
        data.setMessage("/.bc/");

        LogRecordFilter filter = data.makeFilter();

        assertTrue(filter.include(LogRecordUtils.forMessage("abc")));
        assertTrue(filter.include(LogRecordUtils.forMessage("bbc")));
        assertTrue(filter.include(LogRecordUtils.forMessage("Ends with abc")));
        assertTrue(filter.include(LogRecordUtils.forMessage("abc with starts")));
        assertTrue(filter.include(LogRecordUtils.forMessage("in the abc middle")));
        assertFalse(filter.include(LogRecordUtils.forMessage("Contains no _a_b_c_")));
        assertFalse(filter.include(LogRecordUtils.forTag("abc")));
        assertFalse(filter.include(LogRecordUtils.forAppName("abc")));
    }

    @Test
    public void testCreateFilter_priority() throws Exception {
        data.setSelectedPriority(Priority.ERROR);

        LogRecordFilter filter = data.makeFilter();

        assertTrue(filter.include(LogRecordUtils.forPriority(Priority.ERROR)));
        assertTrue(filter.include(LogRecordUtils.forPriority(Priority.FATAL)));
        assertFalse(filter.include(LogRecordUtils.forPriority(Priority.INFO)));
        assertFalse(filter.include(LogRecordUtils.forPriority(Priority.DEBUG)));
        assertFalse(filter.include(LogRecordUtils.forPriority(Priority.VERBOSE)));
        assertFalse(filter.include(LogRecordUtils.forPriority(Priority.WARN)));
    }

    @Test
    public void testCreateFilter_andConditions() throws Exception {
        data.setSelectedPriority(Priority.ERROR);
        data.setMessage("abc");
        data.setTags(Arrays.asList("tag1", "tag2"));
        data.setPids(Arrays.asList(1, 2));
        data.setApplications(Arrays.asList("app1"));

        LogRecordFilter filter = data.makeFilter();

        LogRecord valid1 = new LogRecord(new Date(), 1, NO_ID, "app2", Priority.FATAL, "tag1",
                "Contains abc");
        LogRecord valid2 = new LogRecord(new Date(), 5, NO_ID, "app1", Priority.FATAL, "tag2",
                "Contains abc");
        LogRecord valid3 = new LogRecord(new Date(), 2, NO_ID, "app3", Priority.ERROR, "tag2",
                "Contains abc");

        LogRecord invalidMessage = new LogRecord(new Date(), 1, NO_ID, "app2", Priority.FATAL,
                "tag1",
                "Contains no a_b_c");
        LogRecord invalidPid = new LogRecord(new Date(), 5, NO_ID, "app2", Priority.FATAL, "tag1",
                "Contains abc");
        LogRecord invalidPriority = new LogRecord(new Date(), 1, NO_ID, "app2", Priority.INFO,
                "tag1",
                "Contains abc");
        LogRecord invalidTag = new LogRecord(new Date(), 1, NO_ID, "app2", Priority.FATAL, "tag2a",
                "Contains abc");

        assertTrue(filter.include(valid1));
        assertTrue(filter.include(valid2));
        assertTrue(filter.include(valid3));

        assertFalse(filter.include(invalidMessage));
        assertFalse(filter.include(invalidPid));
        assertFalse(filter.include(invalidPriority));
        assertFalse(filter.include(invalidTag));
    }

    @Test
    public void testSerializeDeserialize() throws Exception {
        data.setSelectedPriority(Priority.ERROR);
        data.setMessage("abc");
        data.setTags(Arrays.asList("tag1", "tag2"));
        data.setPids(Arrays.asList(1, 2));
        data.setApplications(Arrays.asList("app1"));

        JSONObject json = data.toJson();

        FilterDialogData deserializedData = new FilterDialogData(json);

        assertEquals(data, deserializedData);
    }
}
