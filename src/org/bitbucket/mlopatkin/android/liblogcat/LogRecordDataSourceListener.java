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
package org.bitbucket.mlopatkin.android.liblogcat;

import java.util.List;

/**
 * This interface should be implemented by classes that perform log record
 * processing. {@link DataSource} objects will put all records via
 * {@link #onNewRecord(LogRecord, boolean)} method invocations.
 */
public interface LogRecordDataSourceListener {
    /**
     * Called when a new log record is available in the {@link DataSource}.
     * There is a possibility that the order of
     * {@link #onNewRecord(LogRecord, boolean)} calls doesn't match the actual
     * order of records, i. e. there can be two consequent calls "add record A"
     * and "add record B" but the record B is created earlier than the record A.
     * If so {@code needPosition} is set to {@code true} and this is the
     * listener's responsibility to maintain a chronological order of records.
     * 
     * @param record
     *            a new record
     * @param needPosition
     *            {@code true} if the records order doesn't match the invokation
     *            order.
     */
    void onNewRecord(LogRecord record, boolean needPosition);

    /**
     * Called when the {@link DataSource} sends all containing data to the
     * listener and guarantees that there never be any more.
     * 
     * @param records
     *            list of records sorted by time in ascending order
     */
    void assign(List<LogRecord> records);
}
