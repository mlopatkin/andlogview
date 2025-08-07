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

import name.mlopatkin.andlogview.utils.events.LazySubject;
import name.mlopatkin.andlogview.utils.events.Observable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * A combination of several {@link FilterModel}.
 */
public class CompoundFilterModel implements FilterModel {
    private final FilterModel parent;
    private final ChildModelFilter filter;

    private final Observer modelObserver = new Observer() {
        @Override
        public void onFilterAdded(FilterModel model, Filter newFilter, @Nullable Filter before) {
            var adjustedBefore = before;
            if (model == parent && before == null) {
                adjustedBefore = Iterables.getFirst(filter.getChildren().getFilters(), null);
            }

            for (var obs : observers) {
                obs.onFilterAdded(CompoundFilterModel.this, newFilter, adjustedBefore);
            }
        }

        @Override
        public void onFilterRemoved(FilterModel model, Filter removedFilter) {
            for (var obs : observers) {
                obs.onFilterRemoved(CompoundFilterModel.this, removedFilter);
            }
        }

        @Override
        public void onFilterReplaced(FilterModel model, Filter oldFilter, Filter newFilter) {
            for (var obs : observers) {
                obs.onFilterReplaced(CompoundFilterModel.this, oldFilter, newFilter);
            }
        }
    };

    private final LazySubject<Observer> observers = new LazySubject<>(
            this::subscribeObservers,
            this::unsubscribeObservers
    );

    public CompoundFilterModel(FilterModel parent, ChildModelFilter filter) {
        this.parent = parent;
        this.filter = filter;
    }

    private void subscribeObservers() {
        parent.asObservable().addObserver(modelObserver);
        filter.getChildren().asObservable().addObserver(modelObserver);
    }

    private void unsubscribeObservers() {
        parent.asObservable().removeObserver(modelObserver);
        filter.getChildren().asObservable().removeObserver(modelObserver);
    }

    @Override
    public Observable<Observer> asObservable() {
        return observers.asObservable();
    }

    @Override
    public Collection<? extends Filter> getFilters() {
        return ImmutableList.<Filter>builder()
                .addAll(parent.getFilters())
                .addAll(filter.getChildren().getFilters())
                .build();
    }
}
