/*
 * Copyright 2021 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.liblogcat;

import com.google.common.primitives.Ints;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * AssertJ-styled verifier for the LogRecord.
 */
public class AssertLogRecord {
    private final Set<Field> checkedFields = EnumSet.noneOf(Field.class);
    private final LogRecord item;

    private AssertLogRecord(LogRecord item) {
        this.item = item;
    }

    /**
     * Starts the assertion chain and checks that the argument is not null.
     *
     * @param record the record to check
     * @return the assertion chain builder
     * @throws AssertionError if the {@code record} is {@code null}
     */
    public static AssertLogRecord assertThatRecord(@Nullable LogRecord record) {
        Assert.assertNotNull("Record is null", record);
        return new AssertLogRecord(Objects.requireNonNull(record));
    }

    /**
     * Checks that the record has a timestamp set and month and day of this timestamp match arguments. Timestamp is
     * considered verified after this method.
     *
     * @param month the expected month of the timestamp
     * @param day the expected day of the timestamp
     * @return the assertion chain builder
     * @throws AssertionError if the check fails
     */
    public AssertLogRecord hasDate(int month, int day) {
        checkedFields.add(Field.TIME);
        // TODO(mlopatkin) handle the case when the timestamp in the LogRecord actually has year.
        LocalDate actual = getTimeAsLocalDateTime().toLocalDate();
        LocalDate expected = LocalDate.of(actual.getYear(), month, day);

        Assert.assertEquals(expected, actual);
        return this;
    }

    /**
     * Checks that the record has a timestamp set and hour, minutes, seconds and milliseconds of this timestamp match
     * arguments. Timestamp is considered verified after this method.
     *
     * @param hour the expected hour of the timestamp
     * @param min the expected minute of the timestamp
     * @param sec the expected second of the timestamp
     * @param msec the expected milliseconds of the timestamp
     * @return the assertion chain builder
     * @throws AssertionError if the check fails
     */
    public AssertLogRecord hasTime(int hour, int min, int sec, int msec) {
        checkedFields.add(Field.TIME);
        LocalTime expected = LocalTime.of(hour, min, sec, Ints.checkedCast(TimeUnit.MILLISECONDS.toNanos(msec)));
        Assert.assertEquals(expected, getTimeAsLocalDateTime().toLocalTime());
        return this;
    }

    private LocalDateTime getTimeAsLocalDateTime() {
        Timestamp time = item.getTime();
        Assert.assertNotNull("Time is null", time);
        return time.asDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }


    /**
     * Checks that all the unchecked fields of the record that have defaults are equal to these defaults. This should be
     * the last check in the chain.
     *
     * @throws AssertionError if the check fails
     */
    public void andAllOtherFieldAreDefaults() {
        if (!checkedFields.contains(Field.TIME)) {
            Assert.assertNull("Time is set (non-default)", item.getTime());
        }
        if (!checkedFields.contains(Field.PID)) {
            Assert.assertEquals("PID is set", LogRecord.NO_ID, item.getPid());
        }
        if (!checkedFields.contains(Field.TID)) {
            Assert.assertEquals("PID is set", LogRecord.NO_ID, item.getTid());
        }
        if (!checkedFields.contains(Field.BUFFER)) {
            Assert.assertNull("Buffer is set (non-default)", item.getBuffer());
        }
        if (!checkedFields.contains(Field.APP_NAME)) {
            Assert.assertEquals("App name is set", "", item.getAppName());
        }
    }

    /**
     * Checks that the record's tag is equal to the given tag.
     *
     * @param tag the expected tag of the record
     * @return the assertion chain builder
     * @throws AssertionError if the check fails
     */
    public AssertLogRecord hasTag(String tag) {
        checkedFields.add(Field.TAG);
        Assert.assertEquals("Tag mismatch", tag, item.getTag());
        return this;
    }


    /**
     * Checks that the record's message is equal to the given one.
     *
     * @param message the expected message of the record
     * @return the assertion chain builder
     * @throws AssertionError if the check fails
     */
    public AssertLogRecord hasMessage(String message) {
        checkedFields.add(Field.MESSAGE);
        Assert.assertEquals("Message mismatch", message, item.getMessage());
        return this;
    }

    /**
     * Checks that the record's pid is equal to the given one.
     *
     * @param pid the expected pid of the record
     * @return the assertion chain builder
     * @throws AssertionError if the check fails
     */
    public AssertLogRecord hasPid(int pid) {
        checkedFields.add(Field.PID);
        Assert.assertEquals("Pid mismatch", pid, item.getPid());
        return this;
    }

    /**
     * Checks that the record's tid is equal to the given one.
     *
     * @param tid the expected tid of the record
     * @return the assertion chain builder
     * @throws AssertionError if the check fails
     */
    public AssertLogRecord hasTid(int tid) {
        checkedFields.add(Field.TID);
        Assert.assertEquals("Tid mismatch", tid, item.getTid());
        return this;
    }

    /**
     * Checks that the record's priority is equal to the given one.
     *
     * @param priority the expected priority of the record
     * @return the assertion chain builder
     * @throws AssertionError if the check fails
     */
    public AssertLogRecord hasPriority(LogRecord.Priority priority) {
        checkedFields.add(Field.PRIORITY);
        Assert.assertEquals("Priority mismatch", priority, item.getPriority());
        return this;
    }

    /**
     * Checks that the record's buffer is equal to the given one.
     *
     * @param buffer the expected buffer of the record
     * @return the assertion chain builder
     * @throws AssertionError if the check fails
     */
    public AssertLogRecord hasBuffer(LogRecord.Buffer buffer) {
        checkedFields.add(Field.BUFFER);
        Assert.assertEquals("Buffer mismatch", buffer, item.getBuffer());
        return this;
    }


    /**
     * Checks that the record's app name is equal to the given one.
     *
     * @param appName the expected app name of the record
     * @return the assertion chain builder
     * @throws AssertionError if the check fails
     */
    public AssertLogRecord hasAppName(String appName) {
        checkedFields.add(Field.APP_NAME);
        Assert.assertEquals("App name mismatch", appName, item.getAppName());
        return this;
    }
}
