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

import name.mlopatkin.andlogview.filters.Filter;
import name.mlopatkin.andlogview.filters.FilterCollection;
import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.filters.PredicateFilter;
import name.mlopatkin.andlogview.utils.events.ScopedObserver;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class IndexFilterCollection implements FilterCollection<PredicateFilter> {
    private final Map<PredicateFilter, IndexFilterController> controllerMap = new HashMap<>();
    private final IndexFilterController.Factory controllerFactory;

    @Inject
    public IndexFilterCollection(IndexFilterController.Factory controllerFactory) {
        this.controllerFactory = controllerFactory;
    }

    @Override
    @Inject
    public ScopedObserver setModel(FilterModel model) {
        // This is a late call to subscribe to model changes.
        return FilterCollection.super.setModel(model);
    }

    @Override
    public @Nullable PredicateFilter transformFilter(Filter filter) {
        return FilteringMode.WINDOW.equals(filter.getMode()) ? (PredicateFilter) filter : null;
    }

    @Override
    public void addFilter(PredicateFilter filter) {
        if (!filter.isEnabled()) {
            return;
        }
        var controller = controllerFactory.create(filter);
        controllerMap.put(filter, controller);
        controller.show();
    }

    @Override
    public void removeFilter(PredicateFilter filter) {
        if (filter.isEnabled()) {
            controllerMap.remove(filter).close();
        }
    }
}
