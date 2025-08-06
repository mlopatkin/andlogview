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

import name.mlopatkin.andlogview.base.collections.MyIterables;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

class FilterModelImpl implements MutableFilterModel {
    @VisibleForTesting
    protected final Subject<Observer> observers = new Subject<>();

    private final List<Filter> filters = new ArrayList<>();
    private final Map<ChildModelFilter, SubModel> subModels = new HashMap<>();

    public FilterModelImpl() {}

    public FilterModelImpl(Collection<? extends Filter> filters) {
        filters.stream().distinct().forEach(this::addFilter);
    }

    @Override
    public void insertFilterBefore(Filter newFilter, @Nullable Filter before) {
        int insertPos = before != null ? filters.indexOf(before) : filters.size();

        Preconditions.checkArgument(insertPos >= 0, "Filter %s is not in the model", before);
        int prevPos = filters.indexOf(newFilter);
        if (prevPos >= 0) {
            moveFilter(prevPos, insertPos);
            return;
        }

        filters.add(insertPos, newFilter);

        if (newFilter instanceof ChildModelFilter childModelFilter) {
            addChildModelFilter(childModelFilter);
        }

        for (var observer : observers) {
            observer.onFilterAdded(this, newFilter, before);
        }

        withSubmodelsInRange(
                insertPos + 1,
                filters.size(),
                subModel -> {
                    // We have a tail to modify, so we weren't adding to the end.
                    assert before != null;
                    subModel.notifyFilterInserted(newFilter, before);
                }
        );
    }

    private void moveFilter(int curPos, int beforePos) {
        var movedFilter = filters.get(curPos);
        if (curPos < beforePos) {
            // Move it forward
            for (int i = curPos; i < beforePos - 1; ++i) {
                filters.set(i, filters.get(i + 1));
            }
            filters.set(beforePos - 1, movedFilter);
            withSubmodelsInRange(curPos, beforePos - 1, subModel -> subModel.notifyFilterRemoved(movedFilter));
            withMaybeSubmodel(beforePos - 1, subModel -> {
                for (int i = curPos; i < beforePos - 1; i++) {
                    // Observer protocol here pretends that we add filters one-by-one. This isn't necessarily correct
                    // when we consider the contents of the model :(
                    subModel.notifyFilterInserted(filters.get(i), movedFilter);
                }
            });
            withSubmodelsInRange(beforePos, filters.size(), subModel -> subModel.notifyFilterMoved(movedFilter));
        } else if (curPos > beforePos) {
            var beforeFilter = filters.get(beforePos);
            // Move it backward
            for (int i = curPos; i > beforePos; --i) {
                filters.set(i, filters.get(i - 1));
            }
            filters.set(beforePos, movedFilter);

            withMaybeSubmodel(beforePos, subModel -> {
                for (int i = beforePos + 1; i <= curPos; i++) {
                    // Observer protocol here pretends that we remove filters one-by-one. This isn't necessarily correct
                    // when we consider the contents of the model :(
                    subModel.notifyFilterRemoved(filters.get(i));
                }
            });
            withSubmodelsInRange(beforePos + 1, curPos + 1,
                    subModel -> subModel.notifyFilterInserted(movedFilter, beforeFilter));
            withSubmodelsInRange(curPos + 1, filters.size(), subModel -> subModel.notifyFilterMoved(movedFilter));
        } else {
            throw new IllegalArgumentException("Cannot move filter " + movedFilter + " before itself");
        }

        for (var observer : observers) {
            observer.onFilterMoved(this, movedFilter);
        }
    }

    private void withSubmodelsInRange(int from, int to, Consumer<? super SubModel> subModelConsumer) {
        for (int i = from; i < to; ++i) {
            withMaybeSubmodel(i, subModelConsumer);
        }
    }

    private void withMaybeSubmodel(int index, Consumer<? super SubModel> subModelConsumer) {
        @SuppressWarnings("SuspiciousMethodCalls")
        var submodel = subModels.get(filters.get(index));
        if (submodel != null) {
            subModelConsumer.accept(submodel);
        }
    }

    @Override
    public void addFilter(Filter filter) {
        if (!filters.contains(filter)) {
            insertFilterBefore(filter, null);
        }
    }

    private void addChildModelFilter(ChildModelFilter filter) {
        var subModel = new SubModel(filter);
        subModels.put(filter, subModel);
        for (var observer : observers) {
            observer.onSubModelCreated(this, subModel, filter);
        }
    }

    @Override
    public void removeFilter(Filter filter) {
        int position = filters.indexOf(filter);
        if (position >= 0) {
            filters.remove(position);

            if (filter instanceof ChildModelFilter childModelFilter) {
                removeChildModelFilter(childModelFilter);
            }

            for (var observer : observers) {
                observer.onFilterRemoved(this, filter);
            }

            for (int i = position; i < filters.size(); ++i) {
                withMaybeSubmodel(i, subModel -> subModel.notifyFilterRemoved(filter));
            }
        }
    }

    private void removeChildModelFilter(ChildModelFilter filter) {
        var subModel = subModels.remove(filter);
        assert subModel != null;
        for (var observer : observers) {
            observer.onSubModelRemoved(this, subModel, filter);
        }
    }

    @Override
    public void replaceFilter(Filter toReplace, Filter newFilter) {
        var position = filters.indexOf(toReplace);
        Preconditions.checkArgument(position >= 0, "Filter %s is not in the model", toReplace);
        if (Objects.equals(toReplace, newFilter)) {
            // Replacing the filter with itself, do nothing.
            return;
        }
        if (filters.contains(newFilter)) {
            throw new IllegalArgumentException(String.format("Filter %s is already in the model", newFilter));
        }
        filters.set(position, newFilter);

        if (toReplace instanceof ChildModelFilter removedChildModelFilter) {
            removeChildModelFilter(removedChildModelFilter);
        }
        if (newFilter instanceof ChildModelFilter addedChildModelFilter) {
            addChildModelFilter(addedChildModelFilter);
        }

        for (var observer : observers) {
            observer.onFilterReplaced(this, toReplace, newFilter);
        }

        for (int i = position + 1; i < filters.size(); ++i) {
            withMaybeSubmodel(i, subModel -> subModel.notifyFilterReplaced(toReplace, newFilter));
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

    @Override
    public @Nullable FilterModel findSubModel(ChildModelFilter filter) {
        return subModels.get(filter);
    }

    private class SubModel implements FilterModel {
        private final ChildModelFilter filter;
        private final Subject<Observer> observers = new Subject<>();

        public SubModel(ChildModelFilter filter) {
            this.filter = filter;
        }

        @Override
        public Observable<Observer> asObservable() {
            return observers.asObservable();
        }

        @Override
        public Collection<? extends Filter> getFilters() {
            return ImmutableList.copyOf(MyIterables.takeWhile(filters.iterator(), f -> f != filter));
        }

        public void notifyFilterInserted(Filter newFilter, Filter before) {
            var adjustedBefore = before != filter ? before : null;
            for (var observer : observers) {
                observer.onFilterAdded(this, newFilter, adjustedBefore);
            }
        }

        public void notifyFilterRemoved(Filter removed) {
            for (var observer : observers) {
                observer.onFilterRemoved(this, removed);
            }
        }

        public void notifyFilterReplaced(Filter toReplace, Filter newFilter) {
            for (var observer : observers) {
                observer.onFilterReplaced(this, toReplace, newFilter);
            }
        }

        public void notifyFilterMoved(Filter moved) {
            for (var observer : observers) {
                observer.onFilterMoved(this, moved);
            }
        }
    }
}
