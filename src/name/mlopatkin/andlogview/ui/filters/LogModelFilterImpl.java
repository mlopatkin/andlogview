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

package name.mlopatkin.andlogview.ui.filters;

import name.mlopatkin.andlogview.filters.Filter;
import name.mlopatkin.andlogview.filters.FilterChain;
import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.filters.LogRecordHighlighter;
import name.mlopatkin.andlogview.liblogcat.filters.LogBufferFilter;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import com.google.common.annotations.VisibleForTesting;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;

import javax.inject.Inject;

@MainFrameScoped
public class LogModelFilterImpl implements LogModelFilter {
    private final FilterChain filterChain = new FilterChain();
    private final LogRecordHighlighter highlighter = new LogRecordHighlighter();
    private final LogBufferFilter bufferFilter = new LogBufferFilter();

    private final Subject<Observer> observers = new Subject<>();

    @Inject
    @VisibleForTesting
    public LogModelFilterImpl(FilterModel model) {
        filterChain.setModel(model);
        highlighter.setModel(model);

        model.asObservable().addObserver(new FilterModel.Observer() {
            @Override
            public void onFilterAdded(FilterModel model, Filter newFilter) {
                notifyObservers();
            }

            @Override
            public void onFilterRemoved(FilterModel model, Filter removedFilter) {
                notifyObservers();
            }

            @Override
            public void onFilterReplaced(FilterModel model, Filter oldFilter, Filter newFilter) {
                notifyObservers();
            }
        });
    }

    public void setBufferEnabled(LogRecord.Buffer buffer, boolean enabled) {
        bufferFilter.setBufferEnabled(buffer, enabled);
        notifyObservers();
    }

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

    private void notifyObservers() {
        for (Observer o : observers) {
            o.onModelChange();
        }
    }
}
