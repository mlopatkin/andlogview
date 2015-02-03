/*
 * Copyright 2015 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.bookmarks;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogModelFilter;
import org.bitbucket.mlopatkin.utils.events.Observable;
import org.bitbucket.mlopatkin.utils.events.Subject;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Singleton;

@Singleton
public class BookmarkModel implements LogModelFilter {

    private final Set<LogRecord> records = new HashSet<>();
    private final Subject<Observer> observers = new Subject<>();

    @Override
    public boolean shouldShowRecord(LogRecord record) {
        return records.contains(record);
    }

    @Nullable
    @Override
    public Color getHighlightColor(LogRecord record) {
        return null;
    }

    @Override
    public Observable<Observer> asObservable() {
        return observers.asObservable();
    }

    public void addRecord(LogRecord record) {
        records.add(record);
        notifyListeners();
    }

    public void removeRecord(LogRecord record) {
        records.remove(record);
        notifyListeners();
    }

    public void clear() {
        records.clear();
        notifyListeners();
    }

    private void notifyListeners() {
        for (Observer l : observers) {
            l.onModelChange();
        }
    }
}
