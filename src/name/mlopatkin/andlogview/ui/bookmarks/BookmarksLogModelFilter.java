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

package name.mlopatkin.andlogview.ui.bookmarks;

import name.mlopatkin.andlogview.bookmarks.BookmarkModel;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import org.jspecify.annotations.Nullable;

import java.awt.Color;

import javax.inject.Inject;

public class BookmarksLogModelFilter implements LogModelFilter {
    private final Subject<Observer> observers = new Subject<>();

    private final BookmarkModel bookmarkModel;
    private final LogModelFilter modelFilter;

    @Inject
    public BookmarksLogModelFilter(BookmarkModel bookmarkModel, LogModelFilter modelFilter) {
        this.bookmarkModel = bookmarkModel;
        this.modelFilter = modelFilter;
        BookmarkModel.Observer bookmarkObserver = new BookmarkModel.Observer() {
            @Override
            public void onBookmarkAdded() {
                notifyObservers();
            }

            @Override
            public void onBookmarkRemoved() {
                notifyObservers();
            }
        };
        bookmarkModel.asObservable().addObserver(bookmarkObserver);
        Observer mainFilterObserver = this::notifyObservers;
        modelFilter.asObservable().addObserver(mainFilterObserver);
    }

    @Override
    public boolean shouldShowRecord(LogRecord record) {
        return bookmarkModel.containsRecord(record) && modelFilter.shouldShowRecord(record);
    }

    @Override
    public @Nullable Color getHighlightColor(LogRecord record) {
        // We don't want coloring in bookmarks list
        return null;
    }

    @Override
    public Observable<Observer> asObservable() {
        return observers.asObservable();
    }

    private void notifyObservers() {
        for (Observer observer : observers) {
            observer.onModelChange();
        }
    }
}
