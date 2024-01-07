/*
 * Copyright 2024 the Andlogview authors
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class FilterModelTest {

    @Spy
    FilterModel.Observer observer = new FilterModel.Observer() {
        @Override
        public void onFilterAdded(FilterModel model, Filter newFilter) {
            captureFiltersFrom(model);
        }

        @Override
        public void onFilterRemoved(FilterModel model, Filter removedFilter) {
            captureFiltersFrom(model);
        }

        @Override
        public void onFilterReplaced(FilterModel model, Filter oldFilter, Filter newFilter) {
            captureFiltersFrom(model);
        }
    };

    private final List<Filter> observedModel = new ArrayList<>();

    private void captureFiltersFrom(FilterModel model) {
        observedModel.clear();
        observedModel.addAll(model.getFilters());
    }

    private ListAssert<Filter> assertThatObserverSawFilters() {
        return assertThat(observedModel);
    }

    private AbstractCollectionAssert<?, Collection<? extends Filter>, Filter, ObjectAssert<Filter>> assertThatFilters(
            FilterModel model) {
        return assertThat(model.getFilters());
    }

    @Test
    void addingFilterNotifiesObservers() {
        var model = createModel();
        var filter = createFilter("filter");

        model.asObservable().addObserver(observer);
        model.addFilter(filter);

        verify(observer).onFilterAdded(model, filter);
        assertThatObserverSawFilters().containsExactly(filter);
        assertThatFilters(model).containsExactly(filter);
    }

    @Test
    void addingFilterTheSecondTimeDoesNotNotifyObservers() {
        var model = createModel();
        var filter = createFilter("filter");
        model.addFilter(filter);

        model.asObservable().addObserver(observer);
        model.addFilter(filter);

        verify(observer, never()).onFilterAdded(model, filter);
        assertThatFilters(model).containsExactly(filter);
    }

    @Test
    void removingFilterNotifiesObservers() {
        var model = createModel();
        var filter = createFilter("filter");
        model.addFilter(filter);

        model.asObservable().addObserver(observer);
        model.removeFilter(filter);

        verify(observer).onFilterRemoved(model, filter);
        assertThatObserverSawFilters().isEmpty();
        assertThatFilters(model).isEmpty();
    }

    @Test
    void removingNonAddedFilterDoesNotNotifyObservers() {
        var model = createModel();
        var filter = createFilter("filter");

        model.asObservable().addObserver(observer);
        model.removeFilter(filter);

        verify(observer, never()).onFilterRemoved(model, filter);
        assertThatFilters(model).isEmpty();
    }

    @Test
    void replacingFilterNotifiesObserver() {
        var model = createModel();
        var filter = createFilter("filter");
        var newFilter = createFilter("newFilter");
        model.addFilter(filter);

        model.asObservable().addObserver(observer);
        model.replaceFilter(filter, newFilter);

        verify(observer).onFilterReplaced(model, filter, newFilter);
        assertThatObserverSawFilters().containsExactly(newFilter);
        assertThatFilters(model).containsExactly(newFilter);
    }

    @Test
    void replacingFilterWithItselfDoesNothingAndDoesNotNotify() {
        var model = createModel();
        var filter = createFilter("filter");
        model.addFilter(filter);

        model.asObservable().addObserver(observer);
        model.replaceFilter(filter, filter);

        verifyNoInteractions(observer);
        assertThatFilters(model).containsExactly(filter);
    }

    @Test
    void replacingFilterWithExistingThrows() {
        var model = createModel();
        var filter = createFilter("filter");
        var otherFilter = createFilter("otherFilter");
        model.addFilter(filter);
        model.addFilter(otherFilter);

        assertThatThrownBy(() -> model.replaceFilter(filter, otherFilter)).isInstanceOf(IllegalArgumentException.class);
        assertThatFilters(model).containsExactly(filter, otherFilter);
    }

    @Test
    void filtersAreListedInTheOrderTheyAdded() {
        var model = createModel();
        var filter1 = createFilter("filter1");
        var filter2 = createFilter("filter2");
        var filter3 = createFilter("filter3");
        model.addFilter(filter1);
        model.addFilter(filter2);
        model.addFilter(filter3);

        assertThatFilters(model).containsExactly(filter1, filter2, filter3);
    }

    @Test
    void replacementFilterKeepsPositionOfReplaced() {
        var model = createModel();
        var filter1 = createFilter("filter1");
        var filter2 = createFilter("filter2");
        var filter3 = createFilter("filter3");
        model.addFilter(filter1);
        model.addFilter(filter2);
        model.addFilter(filter3);
        model.asObservable().addObserver(observer);

        var filter2Replacement = createFilter("filter2Replacement");
        model.replaceFilter(filter2, filter2Replacement);

        verify(observer).onFilterReplaced(model, filter2, filter2Replacement);
        assertThatObserverSawFilters().containsExactly(filter1, filter2Replacement, filter3);
        assertThatFilters(model).containsExactly(filter1, filter2Replacement, filter3);
    }

    @Test
    void removingAndAddingFilterMovesItToBack() {
        var model = createModel();
        var filter1 = createFilter("filter1");
        var filter2 = createFilter("filter2");
        model.addFilter(filter1);
        model.addFilter(filter2);

        model.removeFilter(filter1);
        model.addFilter(filter1);

        assertThatFilters(model).containsExactly(filter2, filter1);
    }

    FilterModel createModel() {
        return new FilterModelImpl();
    }

    Filter createFilter(String name) {
        return new ToggleFilter(FilteringMode.getDefaultMode(), true, logRecord -> true) {
            @Override
            public boolean equals(Object obj) {
                return this == obj;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }
}
