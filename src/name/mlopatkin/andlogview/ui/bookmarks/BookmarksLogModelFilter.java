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

package org.bitbucket.mlopatkin.android.logviewer.ui.bookmarks;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogModelFilter;
import org.bitbucket.mlopatkin.utils.events.Observable;
import org.bitbucket.mlopatkin.utils.events.Subject;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;

import javax.inject.Inject;

public class BookmarksLogModelFilter implements LogModelFilter {
    private final Subject<Observer> observers = new Subject<>();

    private final BookmarkModel bookmarkModel;
    private final LogModelFilter modelFilter;

    private final Observer mainFilterObserver = new Observer() {
        @Override
        public void onModelChange() {
            notifyObservers();
        }
    };

    private final BookmarkModel.Observer bookmarkObserver = new BookmarkModel.Observer() {
        @Override
        public void onBookmarkAdded() {
            notifyObservers();
        }

        @Override
        public void onBookmarkRemoved() {
            notifyObservers();
        }
    };

    @Inject
    public BookmarksLogModelFilter(BookmarkModel bookmarkModel, LogModelFilter modelFilter) {
        this.bookmarkModel = bookmarkModel;
        this.modelFilter = modelFilter;
        bookmarkModel.asObservable().addObserver(bookmarkObserver);
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
