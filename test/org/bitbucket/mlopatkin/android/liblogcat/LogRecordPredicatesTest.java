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

package org.bitbucket.mlopatkin.android.liblogcat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.junit.Assert;
import org.junit.Test;

import org.bitbucket.mlopatkin.utils.FluentPredicate;

public class LogRecordPredicatesTest {

    private static final String OK_STRING = "OK_STRING";
    private static final String FAIL_STRING = "FAIL_STRING";

    private static final LogRecord OK_TAG = LogRecordUtils.forTag(OK_STRING);
    private static final LogRecord FAIL_TAG = LogRecordUtils.forTag(FAIL_STRING);

    private static final int OK_PID_VAL_1 = 123;
    private static final int OK_PID_VAL_2 = 456;
    private static final int FAIL_PID_VAL = 789;

    private static final LogRecord OK_PID_1 = LogRecordUtils.forPid(OK_PID_VAL_1);
    private static final LogRecord OK_PID_2 = LogRecordUtils.forPid(OK_PID_VAL_2);
    private static final LogRecord FAIL_PID = LogRecordUtils.forPid(FAIL_PID_VAL);

    private final Predicate<String> strMatcher = Predicates.equalTo(OK_STRING);

    @Test
    public void testMatchTag() throws Exception {
        FluentPredicate<LogRecord> predicate = LogRecordPredicates.matchTag(strMatcher);

        assertTrue(predicate.apply(OK_TAG));
        assertFalse(predicate.apply(FAIL_TAG));
    }

    @Test(expected = NullPointerException.class)
    public void testMatchTag_NPE() throws Exception {
        assertNpe(LogRecordPredicates.matchTag(strMatcher));
    }

    @Test
    public void testWithPid() throws Exception {
        FluentPredicate<LogRecord> predicate = LogRecordPredicates.withPid(OK_PID_VAL_1);

        assertTrue(predicate.apply(OK_PID_1));
        assertFalse(predicate.apply(FAIL_PID));
    }

    @Test(expected = NullPointerException.class)
    public void testWithPid_NPE() throws Exception {
        assertNpe(LogRecordPredicates.withPid(OK_PID_VAL_1));
    }

    @Test
    public void testMoreSevereThan() throws Exception {
        FluentPredicate<LogRecord> predicate = LogRecordPredicates.moreSevereThan(
                LogRecord.Priority.DEBUG);

        assertFalse(predicate.apply(LogRecordUtils.forPriority(LogRecord.Priority.VERBOSE)));
        assertTrue(predicate.apply(LogRecordUtils.forPriority(LogRecord.Priority.DEBUG)));
        assertTrue(predicate.apply(LogRecordUtils.forPriority(LogRecord.Priority.ERROR)));
    }

    @Test(expected = NullPointerException.class)
    public void testMoreSevereThan_NPE() throws Exception {
        assertNpe(LogRecordPredicates.moreSevereThan(LogRecord.Priority.DEBUG));
    }


    @Test
    public void testWithAnyOfPids() throws Exception {
        FluentPredicate<LogRecord> predicate = LogRecordPredicates
                .withAnyOfPids(Arrays.asList(OK_PID_VAL_1, OK_PID_VAL_2));

        assertTrue(predicate.apply(OK_PID_1));
        assertTrue(predicate.apply(OK_PID_2));
        assertFalse(predicate.apply(FAIL_PID));
    }

    @Test(expected = NullPointerException.class)
    public void testWithAnyOfPids_NPE() throws Exception {
        assertNpe(LogRecordPredicates.withAnyOfPids(
                Arrays.asList(OK_PID_VAL_1, OK_PID_VAL_2)));
    }

    @Test
    public void testMatchMessage() throws Exception {
        FluentPredicate<LogRecord> predicate = LogRecordPredicates
                .matchMessage(strMatcher);

        assertTrue(predicate.apply(LogRecordUtils.forMessage(OK_STRING)));
        assertFalse(predicate.apply(LogRecordUtils.forMessage(FAIL_STRING)));
    }

    @Test(expected = NullPointerException.class)
    public void testMatchMessage_NPE() throws Exception {
        assertNpe(LogRecordPredicates.matchMessage(strMatcher));
    }

    @Test
    public void testMatchAppName() throws Exception {
        FluentPredicate<LogRecord> predicate = LogRecordPredicates
                .matchAppName(strMatcher);

        assertTrue(predicate.apply(LogRecordUtils.forAppName(OK_STRING)));
        assertFalse(predicate.apply(LogRecordUtils.forAppName(FAIL_STRING)));
    }

    @Test(expected = NullPointerException.class)
    public void testMatchAppName_NPE() throws Exception {
        assertNpe(LogRecordPredicates.matchAppName(strMatcher));
    }


    @Test
    public void testWithBuffer() throws Exception {
        FluentPredicate<LogRecord> predicate = LogRecordPredicates
                .withBuffer(LogRecord.Buffer.MAIN);

        assertTrue(predicate.apply(LogRecordUtils.forBuffer(LogRecord.Buffer.MAIN)));
        assertFalse(predicate.apply(LogRecordUtils.forBuffer(LogRecord.Buffer.EVENTS)));
    }

    @Test(expected = NullPointerException.class)
    public void testWithBuffer_NPE() throws Exception {
        assertNpe(LogRecordPredicates.withBuffer(LogRecord.Buffer.MAIN));
    }

    private void assertNpe(FluentPredicate<LogRecord> predicate) {
        Assert.assertNotNull(predicate);
        predicate.apply(null);
    }
}

