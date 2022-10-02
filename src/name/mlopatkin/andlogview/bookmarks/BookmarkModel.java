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

package name.mlopatkin.andlogview.bookmarks;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

/**
 * Contains the list of all bookmarks made for the given log. All changes to the bookmarks mut be made through this
 * class.
 */
@MainFrameScoped
public class BookmarkModel {
    public interface Observer {
        void onBookmarkAdded();

        void onBookmarkRemoved();
    }

    private final Set<LogRecord> records = new HashSet<>();
    private final Subject<Observer> observers = new Subject<>();

    @Inject
    public BookmarkModel() {}

    public Observable<Observer> asObservable() {
        return observers.asObservable();
    }

    public boolean containsRecord(LogRecord record) {
        return records.contains(record);
    }

    public void addRecord(LogRecord record) {
        records.add(record);
        notifyAdd();
    }

    public void removeRecord(LogRecord record) {
        records.remove(record);
        notifyRemove();
    }

    public void clear() {
        records.clear();
        notifyRemove();
    }

    private void notifyAdd() {
        for (Observer o : observers) {
            o.onBookmarkAdded();
        }
    }

    private void notifyRemove() {
        for (Observer o : observers) {
            o.onBookmarkRemoved();
        }
    }
}
