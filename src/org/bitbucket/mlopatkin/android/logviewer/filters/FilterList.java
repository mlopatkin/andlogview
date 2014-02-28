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
 * FilterList maintains a set of all filters in the application, including special ones (like buffer
 * filtering).
 *
 * In particular, it is responsible for saving and restoring the state.
 */
public class FilterList {

    private final ListMultimap<Class<?>, Filter> filtersByClass = ArrayListMultimap.create();
    private final SetMultimap<Filter, Class<?>> filters = HashMultimap.create();


    public <T extends Filter> void registerFilter(T filter, Class<? super T> firstClass,
            Class<? super T>... moreClasses) {
        Preconditions.checkArgument(!filters.containsKey(filter));

        Set<Class<? super T>> allClasses = Sets.newHashSet(moreClasses);
        allClasses.add(firstClass);

        for (Class<?> clazz : allClasses) {
            filtersByClass.put(clazz, filter);
        }
        filters.putAll(filter, allClasses);
    }

    @SuppressWarnings("unchecked")
    public <T extends Filter> List<T> getFiltersFor(Class<T> clazz) {
        // type-safety is guaranteed by registerFilter
        return (List<T>) Collections.unmodifiableList(filtersByClass.get(clazz));
    }
}
