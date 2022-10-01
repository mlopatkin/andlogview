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

import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StaticLogModel implements LogModel {
    private final ArrayList<LogRecord> logRecords;
    private final Subject<Observer> observers = new Subject<>();

    public StaticLogModel(LogRecord... records) {
        this(Arrays.asList(records));
    }

    public StaticLogModel(List<LogRecord> records) {
        logRecords = new ArrayList<>(records);
    }

    @Override
    public int size() {
        return logRecords.size();
    }

    @Override
    public LogRecord getAt(int index) {
        return logRecords.get(index);
    }

    @Override
    public void clear() {
        int oldSize = logRecords.size();
        logRecords.clear();
        for (Observer observer : observers) {
            observer.onRecordsDiscarded(oldSize);
        }
    }

    @Override
    public Observable<Observer> asObservable() {
        return observers.asObservable();
    }
}
