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
import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.filters.FiltersChangeObserver;
import name.mlopatkin.andlogview.ui.filterdialog.IndexWindowFilter;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

public class IndexFilterCollection {
    private final Map<IndexWindowFilter, IndexFilterController> controllerMap = new HashMap<>();
    private final IndexFilterController.Factory controllerFactory;

    @Inject
    public IndexFilterCollection(IndexFilterController.Factory controllerFactory, FilterModel model) {
        this.controllerFactory = controllerFactory;

        fetchFilters(model).forEach(filter -> controllerMap.computeIfAbsent(filter, controllerFactory::create).show());

        model.asObservable().addObserver(new FiltersChangeObserver(this::onFiltersChanged));
    }

    private void onFiltersChanged(FilterModel model) {
        var updatedControllers = fetchFilters(model).collect(Collectors.toSet());

        for (var iterator = controllerMap.entrySet().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            if (!updatedControllers.contains(entry.getKey())) {
                entry.getValue().close();
                iterator.remove();
            }
        }

        updatedControllers.stream().filter(filter -> !controllerMap.containsKey(filter)).forEach(filter -> {
            var controller = controllerFactory.create(filter);
            controllerMap.put(filter, controller);
            controller.show();
        });
    }

    private static Stream<IndexWindowFilter> fetchFilters(FilterModel model) {
        return model.getFilters()
                .stream()
                .filter(Filter::isEnabled)
                .map(IndexFilterCollection::transformFilter)
                .filter(Objects::nonNull);
    }

    private static @Nullable IndexWindowFilter transformFilter(Filter filter) {
        return FilteringMode.WINDOW.equals(filter.getMode()) ? (IndexWindowFilter) filter : null;
    }
}
