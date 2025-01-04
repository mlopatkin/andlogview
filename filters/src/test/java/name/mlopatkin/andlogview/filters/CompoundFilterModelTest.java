/*
 * Copyright 2025 the Andlogview authors
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

import static name.mlopatkin.andlogview.filters.FilterModelAssert.assertThatFilters;
import static name.mlopatkin.andlogview.filters.Filters.named;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class CompoundFilterModelTest {
    private final MutableFilterModel parent = MutableFilterModel.create();
    private final TestChildModelFilter testFilter = filter();

    @Mock
    FilterModel.Observer observer;

    @Test
    void filtersAddedAfterIndexDoNotShowInTheModel() {
        var filter1 = named("filter1");
        var model = createModel(children(), filter1);

        assertThatFilters(model).doesNotContain(filter1);
        verify(observer, never()).onFilterAdded(any(), any(), any());
    }

    @Test
    void filtersOfToChildrenAreInTheModel() {
        var filter1 = named("filter1");
        var model = createModel(children(filter1));

        assertThatFilters(model).containsExactly(filter1);
    }

    @Test
    void filtersAddedToChildrenTriggerNotification() {
        var model = createModel(children());

        var newFilter = named("new");
        testFilter.getChildren().addFilter(newFilter);

        verify(observer).onFilterAdded(model, newFilter, null);
        assertThatFilters(model).containsExactly(newFilter);
    }

    @Test
    void filtersRemovedFromChildrenTriggerNotification() {
        var filter1 = named("filter1");
        var model = createModel(children(filter1));

        testFilter.getChildren().removeFilter(filter1);

        verify(observer).onFilterRemoved(model, filter1);
    }

    @Test
    void filtersReplacedInChildrenTriggerNotification() {
        var oldFilter = named("old");
        var model = createModel(children(oldFilter));

        var newFilter = named("new");
        testFilter.getChildren().replaceFilter(oldFilter, newFilter);

        verify(observer).onFilterReplaced(model, oldFilter, newFilter);
    }

    @Test
    void filtersAddedAfterIndexDoNotTriggerNotifications() {
        createModel(children());

        var newFilter = named("new");
        parent.addFilter(newFilter);

        verify(observer, never()).onFilterAdded(any(), any(), any());
    }

    @Test
    void filtersRemovedAfterIndexDoNotTriggerNotifications() {
        var filter1 = named("filter1");
        createModel(children(), filter1);

        parent.removeFilter(filter1);

        verify(observer, never()).onFilterRemoved(any(), any());
    }

    @Test
    void filtersReplacedAfterIndexDoNotTriggerNotifications() {
        var oldFilter = named("old");
        createModel(children(), oldFilter);

        parent.replaceFilter(oldFilter, named("new"));

        verify(observer, never()).onFilterReplaced(any(), any(), any());
    }

    @Test
    void filtersRemovedBeforeIndexTriggerNotifications() {
        var filter1 = named("filter1");
        var model = createModel(filter1, children());

        parent.removeFilter(filter1);

        verify(observer).onFilterRemoved(model, filter1);
    }

    @Test
    void filtersReplacedBeforeIndexTriggerNotifications() {
        var oldFilter = named("old");
        var model = createModel(oldFilter, children());

        var newFilter = named("new");
        parent.replaceFilter(oldFilter, newFilter);

        verify(observer).onFilterReplaced(model, oldFilter, newFilter);
    }

    @Test
    void insertingFirstFilterIntoParentTriggerNotifications() {
        var filter1 = named("filter1");
        var model = createModel(filter1, children());

        var filter0 = named("filter0");
        parent.insertFilterBefore(filter0, filter1);

        verify(observer).onFilterAdded(model, filter0, filter1);
        assertThatFilters(model).containsExactly(filter0, filter1);
    }

    @Test
    void insertingLastFilterIntoParentTriggerNotifications() {
        var filter1 = named("filter1");
        var model = createModel(filter1, children());

        var filter2 = named("filter2");
        parent.insertFilterBefore(filter2, children());

        verify(observer).onFilterAdded(model, filter2, null);
        assertThatFilters(model).containsExactly(filter1, filter2);
    }

    @Test
    void insertingLastFilterIntoParentTriggerNotificationsWhenChildFiltersAlsoPresent() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var model = createModel(filter1, children(filter2));

        var newFilter = named("new");
        parent.insertFilterBefore(newFilter, children());

        assertThatFilters(model).containsExactly(filter1, newFilter, filter2);
        verify(observer).onFilterAdded(model, newFilter, filter2);
    }

    private ChildModelFilter children(Filter... children) {
        for (var f : children) {
            testFilter.getChildren().addFilter(f);
        }
        return testFilter;
    }

    private CompoundFilterModel createModel(Filter f1, Filter... filters) {
        parent.addFilter(f1);

        for (var f : filters) {
            parent.addFilter(f);
        }
        return observe(new CompoundFilterModel(Objects.requireNonNull(parent.findSubModel(testFilter)), testFilter));
    }

    private CompoundFilterModel observe(CompoundFilterModel model) {
        model.asObservable().addObserver(observer);
        return model;
    }

    private static TestChildModelFilter filter() {
        return Filters.childModelFilter();
    }
}
