/*
 * Copyright 2022 the Andlogview authors
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

import name.mlopatkin.andlogview.base.concurrent.SequentialExecutor;
import name.mlopatkin.andlogview.utils.events.Observable;

/**
 * This is a list of logs collected so far. LogModel is a live collection and can grow over time, as the log sources
 * keep producing the logs.
 */
public interface LogModel {
    interface Observer {

        /**
         * Called just before the new records are added to the model.
         */
        default void onBeforeRecordsInserted() {}

        /**
         * Called once after new records are added to the model. As these records may be scattered among existing
         * records it is hard (and not really necessary) to provide exact insert positions. Instead, only the position
         * of the first inserted record and the total number of inserted record are passed to this method.
         *
         * @param position the position of the first inserted record
         * @param count the total number of the inserted records
         */
        default void onRecordsInserted(int position, int count) {}

        /**
         * Called when the collected records are discarded.
         *
         * @param oldSize the number of discarded records
         */
        default void onRecordsDiscarded(int oldSize) {}
    }

    /**
     * Returns the total number of rows in the model.
     *
     * @return the number of rows in the model
     */
    int size();

    /**
     * Returns the record at position {@code index}. The very first record has the index 0.
     *
     * @param index the index of the record
     * @return the record at {@code index}
     * @throws IndexOutOfBoundsException if the {@code index} is negative or >= {@link #size()}
     */
    LogRecord getAt(int index);

    // TODO(mlopatkin) Can I get rid of this and recreate a log model from scratch when it makes sense (i.e. for ADB
    //  data sources only)?

    /**
     * Clears collected data.
     */
    void clear();

    Observable<Observer> asObservable();

    /**
     * Creates an implementation of the log model based on the provided DataSource. The implementation is bound to the
     * provided {@code modelOwner}. The model can only be quieried from the owning executor. The listeners are also
     * going to be notified on the provided executor.
     *
     * @param dataSource the data source to provide log data for this model
     * @param modelOwner the owning executor for this model
     * @return the LogModel
     */
    static LogModel fromDataSource(DataSource dataSource, SequentialExecutor modelOwner) {
        SingleThreadInMemoryLogModel model = new SingleThreadInMemoryLogModel();
        BufferedListener<LogRecord> recordListener = new BufferedListener<>(model, modelOwner);
        dataSource.setLogRecordListener(recordListener);
        return model;
    }

    /**
     * Creates an empty LogModel that never grows. The returned implementation is thread-safe. It never notifies its
     * listeners.
     *
     * @return the empty LogModel
     */
    static LogModel empty() {
        return new EmptyLogModel();
    }
}
