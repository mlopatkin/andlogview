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

import name.mlopatkin.andlogview.filters.FilterCollection;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.liblogcat.LogRecord;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.inject.Inject;

public class IndexFilterCollection implements FilterCollection<Predicate<LogRecord>> {
    public interface Observer {
        void onFilterDisabled(Predicate<LogRecord> filter);
    }

    private final Map<Predicate<LogRecord>, IndexFilterController> controllerMap = new HashMap<>();
    private final IndexFilterController.Factory controllerFactory;
    private final Subject<Observer> observers = new Subject<>();

    @Inject
    public IndexFilterCollection(IndexFilterController.Factory controllerFactory) {
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void addFilter(FilteringMode mode, Predicate<LogRecord> filter) {
        controllerMap.put(filter, controllerFactory.create(this, filter));
    }

    @Override
    public void setFilterEnabled(FilteringMode mode, Predicate<LogRecord> filter, boolean enabled) {
        IndexFilterController filterController = controllerMap.get(filter);
        assert filterController != null;
        filterController.setEnabled(enabled);
    }

    @Override
    public void removeFilter(FilteringMode mode, Predicate<LogRecord> filter) {
        controllerMap.remove(filter).destroy();
    }

    void onFilterDisabledByItself(Predicate<LogRecord> filter) {
        for (Observer observer : observers) {
            observer.onFilterDisabled(filter);
        }
    }

    public Observable<Observer> asObservable() {
        return observers.asObservable();
    }
}
