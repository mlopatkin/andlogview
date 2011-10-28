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
package org.bitbucket.mlopatkin.android.logviewer;

import java.util.List;

import org.bitbucket.mlopatkin.android.liblogcat.RecordListener;

/**
 * This interface represents models that can receive log records of any type. It
 * allows batch processing for performance reasons. All methods of this
 * interface should be called on UI thread. This is not an extension of the
 * {@link RecordListener} because of this.
 * <p>
 * <b>Implementations of this interface are not thread-safe!</b>
 * 
 * @param <T>
 *            the actual type of records
 */
interface BatchRecordsReceiver<T> {
    /**
     * @see RecordListener#addRecord(Object)
     */
    void addRecord(T record);

    /**
     * Add several records at once. It is guaranteed that these records are
     * correctly sorted.
     * 
     * @param records
     */
    void addRecords(List<T> records);

    /**
     * @see RecordListener#setRecords(List)
     */
    void setRecords(List<T> records);
}
