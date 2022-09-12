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

package name.mlopatkin.andlogview.filters;

import name.mlopatkin.andlogview.liblogcat.filters.LogBufferFilter;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;

import javax.inject.Inject;

@MainFrameScoped
class LogModelFilterImpl implements LogModelFilter {
    final FilterChain filterChain = new FilterChain();
    final LogRecordHighlighter highlighter = new LogRecordHighlighter();
    final LogBufferFilter bufferFilter = new LogBufferFilter();

    private final Subject<Observer> observers = new Subject<>();

    @Inject
    LogModelFilterImpl() {}

    @Override
    public boolean shouldShowRecord(LogRecord record) {
        return bufferFilter.test(record) && filterChain.shouldShow(record);
    }

    @Override
    public @Nullable Color getHighlightColor(LogRecord record) {
        return highlighter.getColor(record);
    }

    @Override
    public Observable<Observer> asObservable() {
        return observers.asObservable();
    }

    public void notifyObservers() {
        for (Observer o : observers) {
            o.onModelChange();
        }
    }
}
