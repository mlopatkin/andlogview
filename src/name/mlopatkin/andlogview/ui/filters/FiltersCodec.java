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

package name.mlopatkin.andlogview.ui.filters;

import static com.google.common.collect.ImmutableList.toImmutableList;

import name.mlopatkin.andlogview.filters.Filter;
import name.mlopatkin.andlogview.search.RequestCompilationException;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filterdialog.IndexWindowFilter;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Converts filters to and from serialized forms.
 */
class FiltersCodec {
    private static final Logger logger = Logger.getLogger(FiltersCodec.class);

    private Stream<SavedFilterData> encodeFilter(Filter filter) {
        if (filter instanceof IndexWindowFilter indexWindowFilter) {
            return Stream.of(new IndexWindowFilterData(indexWindowFilter));
        } else if (filter instanceof FilterFromDialog filterFromDialog) {
            return Stream.of(new SavedDialogFilterData(filterFromDialog));
        }
        return Stream.empty();
    }

    public List<SavedFilterData> encode(Collection<? extends Filter> filters) {
        return filters.stream()
                .flatMap(this::encodeFilter)
                .collect(toImmutableList());
    }

    private Stream<Filter> decodeFilter(SavedFilterData data) {
        try {
            return Stream.of(data.fromSerializedForm());
        } catch (RequestCompilationException e) {
            logger.error("Failed to load filter", e);
            return Stream.empty();
        }
    }

    public List<Filter> decode(List<? extends SavedFilterData> filters) {
        return filters.stream()
                .flatMap(this::decodeFilter)
                .collect(toImmutableList());
    }
}
