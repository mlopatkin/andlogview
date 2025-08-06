/*
 * Copyright 2017 Mikhail Lopatkin
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
package name.mlopatkin.andlogview;

import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

/**
 * A mutable holder for the data source. Notifies its subscriber when the data source changes.
 * <p>
 * This class is not thread-safe: it must be used on UI thread only.
 */
@MainFrameScoped
public class DataSourceHolder {
    private final Subject<Observer> observers = new Subject<>();
    private @Nullable DataSource source;

    @Inject
    public DataSourceHolder() {}

    public @Nullable DataSource getDataSource() {
        return source;
    }

    /**
     * Replaces stored source if any with a new one.
     *
     * @param source the new source
     */
    public void setDataSource(DataSource source) {
        // TODO(mlopatkin) figure out whether source can be null
        DataSource oldSource = this.source;
        this.source = source;
        for (Observer o : observers) {
            o.onDataSourceChanged(oldSource, source);
        }
    }

    public Observable<Observer> asObservable() {
        return observers.asObservable();
    }

    /**
     * Interface for listeners interested in data source changes.
     */
    public interface Observer {
        /**
         * Invoked when data source has been changed. At this point #getDataSource returns the new source. The state of
         * the old source is valid but undefined. This callback is invoked on the UI thread.
         *
         * @param oldSource the old source or {@code null} if no source has been set.
         * @param newSource the new source
         */
        void onDataSourceChanged(@Nullable DataSource oldSource, DataSource newSource);
    }
}
