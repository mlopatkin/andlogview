/*
 * Copyright 2014 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.filters;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 * FilterList maintains a set of all filters in the application, including special ones (like
 * buffer filtering).
 *
 * In particular, it is responsible for saving and restoring the state.
 */
public class FilterList {

    public interface FilterListChangedListener<T> {

        void onFilterAdded(T filter);

        void onFilterReplaced(T oldFilter, T newFilter);

        void onFilterRemoved(T filter);
    }

    private final ListMultimap<Class<?>, Filter> filtersByClass = ArrayListMultimap.create();
    private final SetMultimap<Filter, Class<?>> filters = HashMultimap.create();
    private final SetMultimap<Class<?>, FilterListChangedListener<?>> listeners = HashMultimap
            .create();

    public <T extends Filter> void addFilter(T filter, Class<? super T> firstClass,
            Class<? super T>... moreClasses) {
        Preconditions.checkArgument(!filters.containsKey(filter));

        Set<Class<? super T>> allClasses = Sets.newHashSet(moreClasses);
        allClasses.add(firstClass);

        Set<FilterListChangedListener<? super T>> toNotify = Sets.newHashSet();

        for (Class<? super T> clazz : allClasses) {
            filtersByClass.put(clazz, filter);
            toNotify.addAll(getListenersFor(clazz));
        }

        for (FilterListChangedListener<? super T> listener : toNotify) {
            listener.onFilterAdded(filter);
        }

        filters.putAll(filter, allClasses);
    }

    @SuppressWarnings("unchecked")
    public <T extends Filter> List<T> getFiltersFor(Class<T> clazz) {
        // type-safety is guaranteed by addFilter
        return (List<T>) Collections.unmodifiableList(filtersByClass.get(clazz));
    }

    public <T extends Filter> void addListener(FilterListChangedListener<? super T> listener,
            Class<T> clazz) {
        listeners.put(clazz, listener);
    }

    public <T extends Filter> void removeListener(FilterListChangedListener<? super T> listener) {
        while (listeners.values().remove(listener)) {
        }
        ;
    }

    @SuppressWarnings("unchecked")
    private <T> Collection<FilterListChangedListener<T>> getListenersFor(
            Class<T> clazz) {
        // type-safety provided by addListener
        Set<?> result = listeners.get(clazz);
        return (Set<FilterListChangedListener<T>>) result;
    }

}
