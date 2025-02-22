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

import static name.mlopatkin.andlogview.filters.FilterModelAssert.assertThatFilters;
import static name.mlopatkin.andlogview.filters.Filters.childModelFilter;
import static name.mlopatkin.andlogview.filters.Filters.named;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.google.common.collect.ImmutableList;

import org.assertj.core.api.ListAssert;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class MutableFilterModelTest {
    @Spy
    FilterModel.Observer observer = new FilterModel.Observer() {
        @Override
        public void onFilterAdded(FilterModel model, Filter newFilter, @Nullable Filter before) {
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

    @Test
    void addingFilterNotifiesObservers() {
        var model = createModel();
        var filter = named("filter");

        model.asObservable().addObserver(observer);
        model.addFilter(filter);

        verify(observer).onFilterAdded(model, filter, null);
        assertThatObserverSawFilters().containsExactly(filter);
        assertThatFilters(model).containsExactly(filter);
    }

    @Test
    void addingFilterTheSecondTimeDoesNotNotifyObservers() {
        var model = createModel();
        var filter = named("filter");
        model.addFilter(filter);

        model.asObservable().addObserver(observer);
        model.addFilter(filter);

        verify(observer, never()).onFilterAdded(model, filter, null);
        assertThatFilters(model).containsExactly(filter);
    }

    @Test
    void removingFilterNotifiesObservers() {
        var model = createModel();
        var filter = named("filter");
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
        var filter = named("filter");

        model.asObservable().addObserver(observer);
        model.removeFilter(filter);

        verify(observer, never()).onFilterRemoved(model, filter);
        assertThatFilters(model).isEmpty();
    }

    @Test
    void replacingFilterNotifiesObserver() {
        var model = createModel();
        var filter = named("filter");
        var newFilter = named("newFilter");
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
        var filter = named("filter");
        model.addFilter(filter);

        model.asObservable().addObserver(observer);
        model.replaceFilter(filter, filter);

        verifyNoInteractions(observer);
        assertThatFilters(model).containsExactly(filter);
    }

    @Test
    void replacingFilterWithExistingThrows() {
        var model = createModel();
        var filter = named("filter");
        var otherFilter = named("otherFilter");
        model.addFilter(filter);
        model.addFilter(otherFilter);

        assertThatThrownBy(() -> model.replaceFilter(filter, otherFilter)).isInstanceOf(IllegalArgumentException.class);
        assertThatFilters(model).containsExactly(filter, otherFilter);
    }

    @Test
    void filtersAreListedInTheOrderTheyAdded() {
        var model = createModel();
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var filter3 = named("filter3");
        model.addFilter(filter1);
        model.addFilter(filter2);
        model.addFilter(filter3);

        assertThatFilters(model).containsExactly(filter1, filter2, filter3);
    }

    @Test
    void replacementFilterKeepsPositionOfReplaced() {
        var model = createModel();
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var filter3 = named("filter3");
        model.addFilter(filter1);
        model.addFilter(filter2);
        model.addFilter(filter3);
        model.asObservable().addObserver(observer);

        var filter2Replacement = named("filter2Replacement");
        model.replaceFilter(filter2, filter2Replacement);

        verify(observer).onFilterReplaced(model, filter2, filter2Replacement);
        assertThatObserverSawFilters().containsExactly(filter1, filter2Replacement, filter3);
        assertThatFilters(model).containsExactly(filter1, filter2Replacement, filter3);
    }

    @Test
    void canReplaceFilterAfterRemovingFilterInFront() {
        var model = createModel();
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        model.addFilter(filter1);
        model.addFilter(filter2);

        var replacement = named("replacement");
        model.removeFilter(filter1);
        model.replaceFilter(filter2, replacement);

        assertThatFilters(model).containsExactly(replacement);
    }

    @Test
    void removingAndAddingFilterMovesItToBack() {
        var model = createModel();
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        model.addFilter(filter1);
        model.addFilter(filter2);

        model.removeFilter(filter1);
        model.addFilter(filter1);

        assertThatFilters(model).containsExactly(filter2, filter1);
    }

    @Test
    void canAddMultipleFiltersAtOnce() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");

        var model = new FilterModelImpl(ImmutableList.of(filter1, filter2, filter1));

        assertThatFilters(model).containsExactly(filter1, filter2);
    }

    @Test
    void addingChildModelFilterNotifiesObservers() {
        var model = createModel();
        model.asObservable().addObserver(observer);

        var childrenFilter = childModelFilter();
        model.addFilter(childrenFilter);

        verify(observer).onFilterAdded(model, childrenFilter, null);
        verify(observer).onSubModelCreated(eq(model), any(), eq(childrenFilter));
    }

    @Test
    void removingChildModelFilterNotifiesObservers() {
        var childrenFilter = childModelFilter();
        var model = createModel(childrenFilter);
        model.asObservable().addObserver(observer);

        model.removeFilter(childrenFilter);

        verify(observer).onFilterRemoved(model, childrenFilter);
        verify(observer).onSubModelRemoved(eq(model), any(), eq(childrenFilter));
    }

    @Test
    void replacingChildModelFilterNotifiesObservers() {
        var childrenFilter1 = childModelFilter();
        var model = createModel(childrenFilter1);
        model.asObservable().addObserver(observer);

        var childrenFilter2 = childModelFilter();
        model.replaceFilter(childrenFilter1, childrenFilter2);

        verify(observer).onFilterReplaced(model, childrenFilter1, childrenFilter2);
        verify(observer).onSubModelRemoved(eq(model), any(), eq(childrenFilter1));
        verify(observer).onSubModelCreated(eq(model), any(), eq(childrenFilter2));
    }

    @Test
    void replacingChildModelFilterWithOtherNotifiesObservers() {
        var childrenFilter1 = childModelFilter();
        var model = createModel(childrenFilter1);
        model.asObservable().addObserver(observer);

        var filter2 = named("filter2");
        model.replaceFilter(childrenFilter1, filter2);

        verify(observer).onFilterReplaced(model, childrenFilter1, filter2);
        verify(observer).onSubModelRemoved(eq(model), any(), eq(childrenFilter1));
    }

    @Test
    void replacingFilterWithChildModelFilterNotifiesObservers() {
        var filter1 = named("filter1");
        var model = createModel(filter1);
        model.asObservable().addObserver(observer);

        var childrenFilter2 = childModelFilter();
        model.replaceFilter(filter1, childrenFilter2);

        verify(observer).onFilterReplaced(model, filter1, childrenFilter2);
        verify(observer).onSubModelCreated(eq(model), any(), eq(childrenFilter2));
    }

    @Test
    void sameInstanceOfSubmodelIsUsedWhenCreatingAndRemoving() {
        var model = createModel();
        model.asObservable().addObserver(observer);
        var subModel = ArgumentCaptor.forClass(FilterModel.class);

        var childrenFilter = childModelFilter();

        model.addFilter(childrenFilter);
        model.removeFilter(childrenFilter);

        verify(observer).onSubModelCreated(eq(model), subModel.capture(), eq(childrenFilter));
        verify(observer).onSubModelRemoved(eq(model), same(subModel.getValue()), eq(childrenFilter));
    }

    @Test
    void submodelHasFilters() {
        var filter1 = named("filter1");
        var model = createModel(filter1);
        model.asObservable().addObserver(observer);
        var subModel = ArgumentCaptor.forClass(FilterModel.class);

        var childrenFilter = childModelFilter();
        model.addFilter(childrenFilter);

        verify(observer).onSubModelCreated(eq(model), subModel.capture(), eq(childrenFilter));

        assertThatFilters(subModel.getValue()).containsExactly(filter1);
    }

    @Test
    void canHaveMultipleSubmodels() {
        var filter1 = named("filter1");
        var model = createModel(filter1);
        model.asObservable().addObserver(observer);
        var subModel = ArgumentCaptor.forClass(FilterModel.class);

        var childrenFilter1 = childModelFilter();
        model.addFilter(childrenFilter1);


        verify(observer).onSubModelCreated(eq(model), subModel.capture(), eq(childrenFilter1));
        var subModel1 = subModel.getValue();

        var filter2 = named("filter2");
        var childrenFilter2 = childModelFilter();
        model.addFilter(filter2);
        model.addFilter(childrenFilter2);

        verify(observer).onSubModelCreated(eq(model), subModel.capture(), eq(childrenFilter2));
        var subModel2 = subModel.getValue();

        assertThatFilters(subModel1).containsExactly(filter1);
        assertThatFilters(subModel2).containsExactly(filter1, childrenFilter1, filter2);
    }

    @Test
    void canGetModelForFilter() {
        var model = createModel();
        model.asObservable().addObserver(observer);

        var childrenFilter = childModelFilter();
        model.addFilter(childrenFilter);

        var subModel = ArgumentCaptor.forClass(FilterModel.class);
        verify(observer).onSubModelCreated(eq(model), subModel.capture(), eq(childrenFilter));

        assertThat(subModel.getValue()).isSameAs(model.findSubModel(childrenFilter));
    }

    @Test
    void noSubmodelForFilterNotInModel() {
        var model = createModel();

        var childrenFilter = childModelFilter();

        assertThat(model.findSubModel(childrenFilter)).isNull();
    }

    @Test
    void addingFiltersDoNotNotifyUnrelatedSubmodels() {
        var childrenFilter = childModelFilter();
        var model = createModel(childrenFilter);

        Objects.requireNonNull(model.findSubModel(childrenFilter)).asObservable().addObserver(observer);

        model.addFilter(named("filter1"));

        verify(observer, never()).onFilterAdded(any(), any(), any());
    }

    @Test
    void removingFiltersDoNotNotifyUnrelatedSubmodels() {
        var childrenFilter = childModelFilter();
        var filter1 = named("filter1");
        var model = createModel(childrenFilter, filter1);

        Objects.requireNonNull(model.findSubModel(childrenFilter)).asObservable().addObserver(observer);

        model.removeFilter(filter1);

        verify(observer, never()).onFilterRemoved(any(), any());
    }

    @Test
    void replacingFiltersDoNotNotifyUnrelatedSubmodels() {
        var childrenFilter = childModelFilter();
        var filter1 = named("filter1");
        var model = createModel(childrenFilter, filter1);

        observeSubModelOf(model, childrenFilter);

        model.replaceFilter(filter1, named("filter2"));

        verify(observer, never()).onFilterReplaced(any(), any(), any());
    }

    @Test
    void removingFiltersNotifyRelatedSubmodelsOnly() {
        var childrenFilter1 = childModelFilter();
        var filter = named("filter");
        var childrenFilter2 = childModelFilter();

        var model = createModel(childrenFilter1, filter, childrenFilter2);
        var subModel1 = observeSubModelOf(model, childrenFilter1);
        var subModel2 = observeSubModelOf(model, childrenFilter2);

        model.removeFilter(filter);

        verify(observer, never()).onFilterRemoved(subModel1, filter);
        verify(observer).onFilterRemoved(subModel2, filter);

        assertThatFilters(subModel2).containsExactly(childrenFilter1);
    }

    @Test
    void replacingFiltersNotifyRelatedSubmodelsOnly() {
        var childrenFilter1 = childModelFilter();
        var filter = named("filter");
        var childrenFilter2 = childModelFilter();

        var model = createModel(childrenFilter1, filter, childrenFilter2);
        var subModel1 = observeSubModelOf(model, childrenFilter1);
        var subModel2 = observeSubModelOf(model, childrenFilter2);

        var otherFilter = named("other");
        model.replaceFilter(filter, otherFilter);

        verify(observer, never()).onFilterReplaced(subModel1, filter, otherFilter);
        verify(observer).onFilterReplaced(subModel2, filter, otherFilter);

        assertThatFilters(subModel1).isEmpty();
        assertThatFilters(subModel2).containsExactly(childrenFilter1, otherFilter);
    }

    @Test
    void canRetrieveSubModelWhenAdding() {
        var model = createModel();
        var childrenFilter = childModelFilter();
        model.asObservable().addObserver(new FilterModel.Observer() {
            @Override
            public void onFilterAdded(FilterModel src, Filter newFilter, @Nullable Filter before) {
                assertThat(model.findSubModel(childrenFilter)).isNotNull();
            }

            @Override
            public void onSubModelCreated(FilterModel parentModel, FilterModel subModel, ChildModelFilter filter) {
                assertThat(model.findSubModel(childrenFilter)).isSameAs(subModel).isNotNull();
            }
        });

        model.addFilter(childrenFilter);
    }

    @Test
    void canRetrieveSubModelWhenReplacing() {
        var filter1 = named("filter1");
        var model = createModel(filter1);
        var childrenFilter = childModelFilter();
        model.asObservable().addObserver(new FilterModel.Observer() {
            @Override
            public void onFilterReplaced(FilterModel src, Filter oldFilter, Filter newFilter) {
                assertThat(newFilter).isSameAs(childrenFilter);
                assertThat(model.findSubModel(childrenFilter)).isNotNull();
            }

            @Override
            public void onSubModelCreated(FilterModel parentModel, FilterModel subModel, ChildModelFilter filter) {
                assertThat(model.findSubModel(childrenFilter)).isSameAs(subModel).isNotNull();
            }
        });

        model.replaceFilter(filter1, childrenFilter);
    }

    @Test
    void canInsertFilterBeforeFirst() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var model = createModel(filter1, filter2);
        model.asObservable().addObserver(observer);

        var newFilter = named("new");

        model.insertFilterBefore(newFilter, filter1);

        verify(observer).onFilterAdded(model, newFilter, filter1);
        assertThatFilters(model).containsExactly(newFilter, filter1, filter2);
        assertThatObserverSawFilters().containsExactly(newFilter, filter1, filter2);
    }

    @Test
    void canInsertFilterBeforeSecond() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var model = createModel(filter1, filter2);
        model.asObservable().addObserver(observer);

        var newFilter = named("new");

        model.insertFilterBefore(newFilter, filter2);

        verify(observer).onFilterAdded(model, newFilter, filter2);
        assertThatFilters(model).containsExactly(filter1, newFilter, filter2);
        assertThatObserverSawFilters().containsExactly(filter1, newFilter, filter2);
    }

    @Test
    void canInsertFilterBeforeEnd() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var model = createModel(filter1, filter2);
        model.asObservable().addObserver(observer);

        var newFilter = named("new");

        model.insertFilterBefore(newFilter, null);

        verify(observer).onFilterAdded(model, newFilter, null);
        assertThatFilters(model).containsExactly(filter1, filter2, newFilter);
        assertThatObserverSawFilters().containsExactly(filter1, filter2, newFilter);
    }

    @Test
    void canInsertChildModelBeforeFirst() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var model = createModel(filter1, filter2);
        model.asObservable().addObserver(observer);

        var newFilter = childModelFilter();
        model.insertFilterBefore(newFilter, filter1);

        var subModel = ArgumentCaptor.forClass(FilterModel.class);
        verify(observer).onSubModelCreated(eq(model), subModel.capture(), eq(newFilter));
        assertThatFilters(subModel.getValue()).isEmpty();
    }

    @Test
    void canInsertChildModelBeforeSecond() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var model = createModel(filter1, filter2);
        model.asObservable().addObserver(observer);

        var newFilter = childModelFilter();
        model.insertFilterBefore(newFilter, filter2);

        var subModel = ArgumentCaptor.forClass(FilterModel.class);
        verify(observer).onSubModelCreated(eq(model), subModel.capture(), eq(newFilter));
        assertThatFilters(subModel.getValue()).containsExactly(filter1);
    }

    @Test
    void canInsertChildModelBeforeEnd() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var model = createModel(filter1, filter2);
        model.asObservable().addObserver(observer);

        var newFilter = childModelFilter();
        model.insertFilterBefore(newFilter, null);

        var subModel = ArgumentCaptor.forClass(FilterModel.class);
        verify(observer).onSubModelCreated(eq(model), subModel.capture(), eq(newFilter));
        assertThatFilters(subModel.getValue()).containsExactly(filter1, filter2);
    }

    @Test
    void insertBeforeFirstNotifiesChildModel() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var children = childModelFilter();
        var model = createModel(filter1, filter2, children);
        var submodel = observeSubModelOf(model, children);

        var newFilter = named("new");
        model.insertFilterBefore(newFilter, filter1);

        verify(observer).onFilterAdded(submodel, newFilter, filter1);
        assertThatFilters(submodel).containsExactly(newFilter, filter1, filter2);
    }

    @Test
    void insertBeforeLastNotifiesChildModel() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var children = childModelFilter();
        var model = createModel(filter1, filter2, children);
        var submodel = observeSubModelOf(model, children);

        var newFilter = named("new");
        model.insertFilterBefore(newFilter, filter2);

        verify(observer).onFilterAdded(submodel, newFilter, filter2);
        assertThatFilters(submodel).containsExactly(filter1, newFilter, filter2);
    }

    @Test
    void insertBeforeEndNotifiesChildModel() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var children = childModelFilter();
        var model = createModel(filter1, filter2, children);
        var submodel = observeSubModelOf(model, children);

        var newFilter = named("new");
        model.insertFilterBefore(newFilter, children);

        verify(observer).onFilterAdded(submodel, newFilter, null);
        assertThatFilters(submodel).containsExactly(filter1, filter2, newFilter);
    }

    @ParameterizedTest(name = "Move filter{0} before {1}")
    @CsvSource({
            "0, -1, 1,2,0",
            "0, 1, 0,1,2",
            "0, 2, 1,0,2",
            "1, -1, 0,2,1",
            "1, 0, 1,0,2",
            "1, 2, 0,1,2",
            "2, -1, 0,1,2",
            "2, 0, 2,0,1",
            "2, 1, 0,2,1"
    })
    void canUseInsertToMoveFilters(int srcIndex, int targetIndex, int r0, int r1, int r2) {
        Filter[] filters = {
                named("filter0"),
                named("filter1"),
                named("filter2")
        };

        var model = createModel(filters);
        model.asObservable().addObserver(observer);
        var src = filters[srcIndex];
        var dst = targetIndex >= 0 ? filters[targetIndex] : null;

        model.insertFilterBefore(src, dst);

        assertThatFilters(model).containsExactly(filters[r0], filters[r1], filters[r2]);
        verify(observer).onFilterMoved(model, src);
    }

    @Test
    void movingFilterOutOfSubModel() {
        var filter1 = named("filter1");
        var children = childModelFilter();
        var model = createModel(filter1, children);
        var subModel = observeSubModelOf(model, children);

        model.insertFilterBefore(filter1, null);

        assertThatFilters(subModel).isEmpty();
        verify(observer).onFilterRemoved(subModel, filter1);
    }

    @Test
    void movingFilterInSubModelBackward() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var children = childModelFilter();
        var model = createModel(filter1, filter2, children);
        var subModel = observeSubModelOf(model, children);

        model.insertFilterBefore(filter2, filter1);

        assertThatFilters(subModel).containsExactly(filter2, filter1);
        verify(observer, never()).onFilterRemoved(eq(subModel), any());
        verify(observer, never()).onFilterAdded(eq(subModel), any(), any());
        verify(observer).onFilterMoved(subModel, filter2);
    }

    @Test
    void movingFilterInSubModelForward() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var children = childModelFilter();
        var model = createModel(filter1, filter2, children);
        var subModel = observeSubModelOf(model, children);

        model.insertFilterBefore(filter1, children);

        assertThatFilters(subModel).containsExactly(filter2, filter1);
        verify(observer, never()).onFilterRemoved(eq(subModel), any());
        verify(observer, never()).onFilterAdded(eq(subModel), any(), any());
        verify(observer).onFilterMoved(subModel, filter1);
    }

    @Test
    void movingFilterIntoSubModel() {
        var filter1 = named("filter1");
        var children = childModelFilter();
        var filter2 = named("filter2");
        var model = createModel(filter1, children, filter2);
        var subModel = observeSubModelOf(model, children);

        model.insertFilterBefore(filter2, filter1);

        assertThatFilters(subModel).containsExactly(filter2, filter1);
        verify(observer).onFilterAdded(subModel, filter2, filter1);
    }

    @Test
    void movingFilterLastIntoSubModel() {
        var filter1 = named("filter1");
        var children = childModelFilter();
        var filter2 = named("filter2");
        var model = createModel(filter1, children, filter2);
        var subModel = observeSubModelOf(model, children);

        model.insertFilterBefore(filter2, children);

        assertThatFilters(subModel).containsExactly(filter1, filter2);
        verify(observer).onFilterAdded(subModel, filter2, null);
    }

    @Test
    void movingSubmodelBoundaryForwardAddsFilters() {
        var filter1 = named("filter1");
        var children = childModelFilter();
        var filter2 = named("filter2");
        var filter3 = named("filter3");
        var model = createModel(filter1, children, filter2, filter3);
        var subModel = observeSubModelOf(model, children);

        model.insertFilterBefore(children, null);

        assertThatFilters(subModel).containsExactly(filter1, filter2, filter3);
        verify(observer).onFilterAdded(subModel, filter2, null);
        verify(observer).onFilterAdded(subModel, filter3, null);
        verifyNoMoreInteractions(observer);
    }

    @Test
    void movingSubmodelBoundaryBackwardRemovesFilters() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var filter3 = named("filter3");
        var filter4 = named("filter4");
        var children = childModelFilter();
        var model = createModel(filter1, filter2, filter3, children, filter4);
        var subModel = observeSubModelOf(model, children);

        model.insertFilterBefore(children, filter2);

        assertThatFilters(subModel).containsExactly(filter1);
        verify(observer).onFilterRemoved(subModel, filter2);
        verify(observer).onFilterRemoved(subModel, filter3);
        verifyNoMoreInteractions(observer);
    }

    @Test
    void movingFilterBeforeItselfThrows() {
        var filter1 = named("filter1");
        var filter2 = named("filter2");
        var filter3 = named("filter3");

        var model = createModel(filter1, filter2, filter3);

        assertThatThrownBy(
                () -> model.insertFilterBefore(filter2, filter2)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    FilterModel observeSubModelOf(MutableFilterModel model, ChildModelFilter filter) {
        var subModel = Objects.requireNonNull(model.findSubModel(filter));
        subModel.asObservable().addObserver(observer);
        return subModel;
    }

    MutableFilterModel createModel() {
        return new FilterModelImpl();
    }

    MutableFilterModel createModel(Filter... filters) {
        return new FilterModelImpl(Arrays.asList(filters));
    }
}
