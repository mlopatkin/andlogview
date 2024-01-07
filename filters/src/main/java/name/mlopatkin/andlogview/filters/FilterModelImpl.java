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

import com.google.common.base.Preconditions;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class FilterModelImpl implements FilterModel {
    private final Subject<Observer> observers = new Subject<>();
    private final Set<Filter> filters = new LinkedHashSet<>();

    @Override
    public void addFilter(Filter filter) {
        if (filters.add(filter)) {
            for (var observer : observers) {
                observer.onFilterAdded(filter);
            }
        }
    }

    @Override
    public void removeFilter(Filter filter) {
        if (filters.remove(filter)) {
            for (var observer : observers) {
                observer.onFilterRemoved(filter);
            }
        }
    }

    @Override
    public void replaceFilter(Filter toReplace, Filter newFilter) {
        Preconditions.checkArgument(filters.contains(toReplace),
                String.format("Filter %s is not in the model", toReplace));
        if (Objects.equals(toReplace, newFilter)) {
            // Replacing the filter with itself, do nothing.
            return;
        }
        var wasRemoved = filters.remove(toReplace);
        assert wasRemoved : "Holder for " + toReplace + " disappeared";

        if (!filters.add(newFilter)) {
            throw new IllegalArgumentException(String.format("Filter %s is already in the model", newFilter));
        }
        for (var observer : observers) {
            observer.onFilterReplaced(toReplace, newFilter);
        }
    }

    @Override
    public Observable<Observer> asObservable() {
        return observers.asObservable();
    }
}
