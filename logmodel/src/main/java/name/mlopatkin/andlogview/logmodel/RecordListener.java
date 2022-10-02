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
package name.mlopatkin.andlogview.logmodel;

import java.util.List;

/**
 * This interface should be implemented by classes that perform log record
 * processing. {@link DataSource} objects will put all records via
 * {@link #addRecord(Object)} method invocations.
 *
 * @param <T> the actual type of records
 */
public interface RecordListener<T> {
    /**
     * Called when a new log record is available in the {@link DataSource}.
     * There is a possibility that the order of {@code addRecord} calls
     * doesn't match the actual order of records, i. e. there can be two
     * consequent calls "add record A" and "add record B" but the record B is
     * created earlier than the record A.
     *
     * @param record a non-null new record
     */
    void addRecord(T record);

    /**
     * Called when the {@link DataSource} sends all containing data to the
     * listener and guarantees that there never be any more. The list supplied
     * cannot be null or contain nulls.
     *
     * @param records list of records sorted by time in ascending order
     */
    void setRecords(List<T> records);
}
