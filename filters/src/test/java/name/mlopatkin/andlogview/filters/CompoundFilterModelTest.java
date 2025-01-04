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
import static name.mlopatkin.andlogview.filters.ToggleFilter.hide;
import static name.mlopatkin.andlogview.filters.ToggleFilter.show;

import static com.google.common.base.Predicates.alwaysFalse;
import static com.google.common.base.Predicates.alwaysTrue;

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
        var model = createModel();
        var hideFilter = hide(alwaysFalse());

        parent.addFilter(hideFilter);

        assertThatFilters(model).doesNotContain(hideFilter);
    }

    @Test
    void filtersAddedToChildrenShowInTheModel() {
        var model = createModel();
        var hideFilter = hide(alwaysFalse());

        testFilter.getChildren().addFilter(hideFilter);

        assertThatFilters(model).containsExactly(hideFilter);
    }

    @Test
    void filtersAddedToChildrenTriggerNotification() {
        var model = createModel();
        model.asObservable().addObserver(observer);

        var hideFilter = hide(alwaysFalse());
        testFilter.getChildren().addFilter(hideFilter);

        verify(observer).onFilterAdded(model, hideFilter);
    }

    @Test
    void filtersRemovedFromChildrenTriggerNotification() {
        var model = createModel();
        var hideFilter = hide(alwaysFalse());
        testFilter.getChildren().addFilter(hideFilter);
        model.asObservable().addObserver(observer);

        testFilter.getChildren().removeFilter(hideFilter);


        verify(observer).onFilterRemoved(model, hideFilter);
    }

    @Test
    void filtersReplacedInChildrenTriggerNotification() {
        var model = createModel();
        var hideFilter = hide(alwaysFalse());
        testFilter.getChildren().addFilter(hideFilter);
        model.asObservable().addObserver(observer);

        var showFilter = show(alwaysTrue());
        testFilter.getChildren().replaceFilter(hideFilter, showFilter);


        verify(observer).onFilterReplaced(model, hideFilter, showFilter);
    }

    @Test
    void filtersAddedAfterIndexDoNotTriggerNotifications() {
        var model = createModel();
        model.asObservable().addObserver(observer);

        var hideFilter = hide(alwaysFalse());
        parent.addFilter(hideFilter);

        verify(observer, never()).onFilterAdded(any(), any());
    }

    @Test
    void filtersRemovedAfterIndexDoNotTriggerNotifications() {
        var hideFilter = hide(alwaysFalse());
        var model = createModel();
        parent.addFilter(hideFilter);

        model.asObservable().addObserver(observer);
        parent.removeFilter(hideFilter);

        verify(observer, never()).onFilterRemoved(any(), any());
    }

    @Test
    void filtersReplacedAfterIndexDoNotTriggerNotifications() {
        var model = createModel();
        var hideFilter = hide(alwaysFalse());
        parent.addFilter(hideFilter);

        model.asObservable().addObserver(observer);

        parent.replaceFilter(hideFilter, show(alwaysTrue()));

        verify(observer, never()).onFilterReplaced(any(), any(), any());
    }

    @Test
    void filtersRemovedBeforeIndexTriggerNotifications() {
        var hideFilter = hide(alwaysFalse());
        parent.addFilter(hideFilter);
        var model = createModel();

        model.asObservable().addObserver(observer);

        parent.removeFilter(hideFilter);

        verify(observer).onFilterRemoved(model, hideFilter);
    }

    @Test
    void filtersReplacedBeforeIndexTriggerNotifications() {
        var hideFilter = hide(alwaysFalse());
        parent.addFilter(hideFilter);

        var model = createModel();

        model.asObservable().addObserver(observer);

        var showFilter = show(alwaysTrue());
        parent.replaceFilter(hideFilter, showFilter);

        verify(observer).onFilterReplaced(model, hideFilter, showFilter);
    }

    private CompoundFilterModel createModel() {
        parent.addFilter(testFilter);
        return new CompoundFilterModel(Objects.requireNonNull(parent.findSubModel(testFilter)), testFilter);
    }

    private static TestChildModelFilter filter() {
        return Filters.childModelFilter();
    }
}
