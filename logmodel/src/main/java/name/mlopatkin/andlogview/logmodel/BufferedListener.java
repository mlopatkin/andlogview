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

import com.google.errorprone.annotations.concurrent.GuardedBy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * This class tries to limit the number of events pushing records into the
 * model.
 * <p/>
 * If the source produces records with high frequency and for each record we create an event
 * then the event queue becomes overloaded with these events. The UI becomes
 * unresponsive. However, new records should appear as fast as possible.
 */
public class BufferedListener<T> implements RecordListener<T> {
    private final BatchRecordsReceiver<T> receiver;
    private final Executor receiverExecutor;
    private final Comparator<? super T> comparator;

    private final Object lock = new Object();

    @GuardedBy("lock")
    private ArrayList<T> pendingRecords = new ArrayList<>();

    public static <V extends Comparable<? super V>> BufferedListener<V> create(BatchRecordsReceiver<V> receiver,
            Executor receiverExecutor) {
        return new BufferedListener<>(receiver, receiverExecutor, Comparator.naturalOrder());
    }

    public BufferedListener(BatchRecordsReceiver<T> receiver, Executor receiverExecutor,
            Comparator<? super T> comparator) {
        this.receiver = receiver;
        this.receiverExecutor = receiverExecutor;
        this.comparator = comparator;
    }

    @Override
    public void addRecord(T record) {
        synchronized (lock) {
            boolean needsFlush = pendingRecords.isEmpty();
            pendingRecords.add(record);
            if (needsFlush) {
                receiverExecutor.execute(this::flushRecords);
            }
        }
    }

    @Override
    public void setRecords(List<T> records) {
        receiverExecutor.execute(() -> receiver.setRecords(records));
    }

    private void flushRecords() {
        ArrayList<T> records;
        synchronized (lock) {
            records = pendingRecords;
            pendingRecords = new ArrayList<>();
        }
        records.sort(comparator);
        receiver.addRecords(records);
    }
}
