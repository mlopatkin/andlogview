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

import name.mlopatkin.andlogview.utils.MyListUtils;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the {@link LogModel} that is read and updated from a single thread.
 */
class SingleThreadInMemoryLogModel implements LogModel, BatchRecordsReceiver<LogRecord> {
    private final ArrayList<LogRecord> records = new ArrayList<>();
    private final Subject<Observer> observers = new Subject<>();

    SingleThreadInMemoryLogModel() {
    }

    @Override
    public int size() {
        return records.size();
    }

    @Override
    public LogRecord getAt(int index) {
        return records.get(index);
    }

    @Override
    public void addRecords(List<LogRecord> newRecords) {
        if (newRecords.isEmpty()) {
            return;
        }
        for (Observer observer : observers) {
            observer.onBeforeRecordsInserted();
        }
        int position = MyListUtils.mergeOrdered(records, newRecords);
        for (Observer observer : observers) {
            observer.onRecordsInserted(position, newRecords.size());
        }
    }

    @Override
    public void clear() {
        int oldSize = records.size();
        records.clear();
        for (Observer observer : observers) {
            observer.onRecordsDiscarded(oldSize);
        }
    }

    @Override
    public void setRecords(List<LogRecord> newRecords) {
        clear();
        addRecords(newRecords);
    }

    @Override
    public Observable<Observer> asObservable() {
        return observers.asObservable();
    }
}
