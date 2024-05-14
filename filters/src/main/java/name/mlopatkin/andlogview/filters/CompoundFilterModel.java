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

import name.mlopatkin.andlogview.utils.events.CompoundObservable;
import name.mlopatkin.andlogview.utils.events.Observable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.Collection;

/**
 * A combination of several {@link FilterModel}.
 */
public class CompoundFilterModel implements FilterModel {
    private final FilterModel first;
    private final FilterModel second;
    private final Observable<FilterModel.Observer> observable;

    public CompoundFilterModel(FilterModel first, FilterModel second) {
        this.first = first;
        this.second = second;
        this.observable = new CompoundObservable<>(first.asObservable(), second.asObservable());
    }

    @Override
    public Observable<Observer> asObservable() {
        return observable;
    }

    @Override
    public Collection<? extends Filter> getFilters() {
        return ImmutableList.copyOf(Iterables.concat(first.getFilters(), second.getFilters()));
    }
}
