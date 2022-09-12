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

package name.mlopatkin.andlogview.logmodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Predicate;

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

    private final Predicate<String> strMatcher = Predicate.isEqual(OK_STRING);

    @Test
    public void testMatchTag() throws Exception {
        Predicate<LogRecord> predicate = LogRecordPredicates.matchTag(strMatcher);

        assertTrue(predicate.test(OK_TAG));
        assertFalse(predicate.test(FAIL_TAG));
    }

    @Test(expected = NullPointerException.class)
    public void testMatchTag_NPE() throws Exception {
        assertNpe(LogRecordPredicates.matchTag(strMatcher));
    }

    @Test
    public void testWithPid() throws Exception {
        Predicate<LogRecord> predicate = LogRecordPredicates.withPid(OK_PID_VAL_1);

        assertTrue(predicate.test(OK_PID_1));
        assertFalse(predicate.test(FAIL_PID));
    }

    @Test(expected = NullPointerException.class)
    public void testWithPid_NPE() throws Exception {
        assertNpe(LogRecordPredicates.withPid(OK_PID_VAL_1));
    }

    @Test
    public void testMoreSevereThan() throws Exception {
        Predicate<LogRecord> predicate = LogRecordPredicates.moreSevereThan(LogRecord.Priority.DEBUG);

        assertFalse(predicate.test(LogRecordUtils.forPriority(LogRecord.Priority.VERBOSE)));
        assertTrue(predicate.test(LogRecordUtils.forPriority(LogRecord.Priority.DEBUG)));
        assertTrue(predicate.test(LogRecordUtils.forPriority(LogRecord.Priority.ERROR)));
    }

    @Test(expected = NullPointerException.class)
    public void testMoreSevereThan_NPE() throws Exception {
        assertNpe(LogRecordPredicates.moreSevereThan(LogRecord.Priority.DEBUG));
    }

    @Test
    public void testWithAnyOfPids() throws Exception {
        Predicate<LogRecord> predicate =
                LogRecordPredicates.withAnyOfPids(Arrays.asList(OK_PID_VAL_1, OK_PID_VAL_2));

        assertTrue(predicate.test(OK_PID_1));
        assertTrue(predicate.test(OK_PID_2));
        assertFalse(predicate.test(FAIL_PID));
    }

    @Test(expected = NullPointerException.class)
    public void testWithAnyOfPids_NPE() throws Exception {
        assertNpe(LogRecordPredicates.withAnyOfPids(Arrays.asList(OK_PID_VAL_1, OK_PID_VAL_2)));
    }

    @Test
    public void testMatchMessage() throws Exception {
        Predicate<LogRecord> predicate = LogRecordPredicates.matchMessage(strMatcher);

        assertTrue(predicate.test(LogRecordUtils.forMessage(OK_STRING)));
        assertFalse(predicate.test(LogRecordUtils.forMessage(FAIL_STRING)));
    }

    @Test(expected = NullPointerException.class)
    public void testMatchMessage_NPE() throws Exception {
        assertNpe(LogRecordPredicates.matchMessage(strMatcher));
    }

    @Test
    public void testMatchAppName() throws Exception {
        Predicate<LogRecord> predicate = LogRecordPredicates.matchAppName(strMatcher);

        assertTrue(predicate.test(LogRecordUtils.forAppName(OK_STRING)));
        assertFalse(predicate.test(LogRecordUtils.forAppName(FAIL_STRING)));
    }

    @Test(expected = NullPointerException.class)
    public void testMatchAppName_NPE() throws Exception {
        assertNpe(LogRecordPredicates.matchAppName(strMatcher));
    }

    @Test
    public void testWithBuffer() throws Exception {
        Predicate<LogRecord> predicate = LogRecordPredicates.withBuffer(LogRecord.Buffer.MAIN);

        assertTrue(predicate.test(LogRecordUtils.forBuffer(LogRecord.Buffer.MAIN)));
        assertFalse(predicate.test(LogRecordUtils.forBuffer(LogRecord.Buffer.EVENTS)));
    }

    @Test(expected = NullPointerException.class)
    public void testWithBuffer_NPE() throws Exception {
        assertNpe(LogRecordPredicates.withBuffer(LogRecord.Buffer.MAIN));
    }

    @SuppressWarnings("ReturnValueIgnored")
    private void assertNpe(Predicate<LogRecord> predicate) {
        Assert.assertNotNull(predicate);
        predicate.test(null);
    }
}
