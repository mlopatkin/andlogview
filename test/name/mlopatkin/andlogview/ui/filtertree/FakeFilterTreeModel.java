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

package name.mlopatkin.andlogview.ui.filtertree;

import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FakeFilterTreeModel implements FilterTreeModel<FakeFilterTreeModel.FakeFilter> {
    public static final class FakeFilter implements FilterNodeViewModel {
        private final boolean enabled;
        private final String title;

        FakeFilter(boolean enabled, String title) {
            this.enabled = enabled;
            this.title = title;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public String getText() {
            return title;
        }

        @Override
        public String getTooltip() {
            return title;
        }

        public FakeFilter enabled(boolean enabled) {
            return new FakeFilter(enabled, title);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof FakeFilter that) {
                return enabled == that.enabled && Objects.equals(title, that.title);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(enabled, title);
        }
    }

    private final List<FakeFilter> filters = new ArrayList<>();
    private final Subject<ModelObserver<? super FakeFilter>> observers = new Subject<>();

    @Override
    public void moveFilter(FakeFilter movedFilter, int newPosition) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public FakeFilter addFilter(String title) {
        var newFilter = new FakeFilter(true, title);
        filters.add(newFilter);

        for (var observer : observers) {
            observer.onFilterAdded(newFilter);
        }
        return newFilter;
    }

    public void removeFilter(FakeFilter filter) {
        if (filters.remove(filter)) {
            for (var observer : observers) {
                observer.onFilterRemoved(filter);
            }
        }
    }

    @Override
    public void setFilterEnabled(FakeFilter filter, boolean enabled) {
        var i = filters.indexOf(filter);
        var replacement = filter.enabled(enabled);
        filters.set(i, replacement);
        for (var observer : observers) {
            observer.onFilterReplaced(filter, replacement);
        }
    }

    @Override
    public Observable<ModelObserver<? super FakeFilter>> asObservable() {
        return observers.asObservable();
    }

    @Override
    public ImmutableList<FakeFilter> getFilters() {
        return ImmutableList.copyOf(filters);
    }

    @Override
    public void removeFilterForView(FakeFilter filter) {
        removeFilter(filter);
    }

    @Override
    public void editFilter(FakeFilter filter) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
