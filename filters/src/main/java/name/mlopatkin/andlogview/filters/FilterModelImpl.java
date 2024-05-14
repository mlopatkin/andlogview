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

import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

class FilterModelImpl implements MutableFilterModel {
    @VisibleForTesting
    protected final Subject<Observer> observers = new Subject<>();

    private final List<Filter> filters = new ArrayList<>();

    public FilterModelImpl() {}

    public FilterModelImpl(Collection<? extends Filter> filters) {
        this.filters.addAll(ImmutableSet.copyOf(filters));
    }

    @Override
    public void addFilter(Filter filter) {
        if (!filters.contains(filter)) {
            filters.add(filter);
            for (var observer : observers) {
                observer.onFilterAdded(this, filter);
            }
        }
    }

    @Override
    public void removeFilter(Filter filter) {
        if (filters.remove(filter)) {
            for (var observer : observers) {
                observer.onFilterRemoved(this, filter);
            }
        }
    }

    @Override
    public void replaceFilter(Filter toReplace, Filter newFilter) {
        var position = filters.indexOf(toReplace);
        Preconditions.checkArgument(position >= 0,
                String.format("Filter %s is not in the model", toReplace));
        if (Objects.equals(toReplace, newFilter)) {
            // Replacing the filter with itself, do nothing.
            return;
        }
        if (filters.contains(newFilter)) {
            throw new IllegalArgumentException(String.format("Filter %s is already in the model", newFilter));
        }
        filters.set(position, newFilter);

        for (var observer : observers) {
            observer.onFilterReplaced(this, toReplace, newFilter);
        }
    }

    @Override
    public Observable<Observer> asObservable() {
        return observers.asObservable();
    }

    @Override
    public Collection<? extends Filter> getFilters() {
        return ImmutableList.copyOf(filters);
    }
}
