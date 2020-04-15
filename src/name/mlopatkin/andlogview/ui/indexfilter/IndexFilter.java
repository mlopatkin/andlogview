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

package name.mlopatkin.andlogview.ui.indexfilter;

import name.mlopatkin.andlogview.liblogcat.LogRecord;
import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;
import java.util.function.Predicate;

class IndexFilter implements LogModelFilter {
    private final LogModelFilter parent;
    private final Predicate<LogRecord> filter;

    private final Subject<Observer> observers = new Subject<>();

    private final Observer parentObserver = new Observer() {
        @Override
        public void onModelChange() {
            notifyObservers();
        }
    };

    public IndexFilter(LogModelFilter parent, Predicate<LogRecord> filter) {
        this.parent = parent;
        this.filter = filter;
        parent.asObservable().addObserver(parentObserver);
    }

    @Override
    public boolean shouldShowRecord(LogRecord record) {
        return parent.shouldShowRecord(record) && filter.test(record);
    }

    @Override
    public @Nullable Color getHighlightColor(LogRecord record) {
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
