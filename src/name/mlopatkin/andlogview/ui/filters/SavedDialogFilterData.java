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

import static name.mlopatkin.andlogview.logmodel.LogRecord.Priority;

import name.mlopatkin.andlogview.config.InvalidJsonContentException;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.search.RequestCompilationException;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialogData;

import com.google.common.collect.ImmutableList;

import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.List;

class SavedDialogFilterData extends SavedFilterData {
    private final FilterData filterData;
    private transient @Nullable FilterFromDialog filter;

    SavedDialogFilterData(FilterFromDialog filter) {
        super(filter.isEnabled());
        this.filterData = new FilterData(filter.getData());
        this.filter = filter;
    }

    @Override
    public FilterFromDialog fromSerializedForm() throws RequestCompilationException, InvalidJsonContentException {
        var filter = this.filter;
        if (filter == null) {
            // This might be non-migrated index window filter, so calling toFilter is justified.
            this.filter = filter = filterData.toFilterData().toFilter(enabled);
        }
        return filter;
    }

    private static class FilterData {
        private @Nullable FilteringMode mode;

        private @Nullable String name;
        private @Nullable List<String> tags;
        private @Nullable List<Integer> pids;
        private @Nullable List<String> apps;
        private @Nullable String messagePattern;
        private @Nullable Priority priority;
        private @Nullable Color highlightColor;

        @SuppressWarnings("unused")
        public FilterData() {
            // Used by JSON reader.
        }

        public FilterData(FilterFromDialogData fromDialog) {
            mode = fromDialog.getMode();
            name = fromDialog.getName();
            // We replace empty lists with nulls to skip them from the serialized form.
            tags = emptyToNull(fromDialog.getTags());
            pids = emptyToNull(fromDialog.getPids());
            apps = emptyToNull(fromDialog.getApps());
            messagePattern = fromDialog.getMessagePattern();
            priority = fromDialog.getPriority();
            highlightColor = fromDialog.getHighlightColor();
        }

        public FilterFromDialogData toFilterData() throws InvalidJsonContentException {
            if (mode == null) {
                throw new InvalidJsonContentException("Required mode field is missing in JSON filter data");
            }
            return new FilterFromDialogData(mode)
                    .setName(name)
                    .setTags(nullToEmpty(tags))
                    .setPids(nullToEmpty(pids))
                    .setApps(nullToEmpty(apps))
                    .setMessagePattern(messagePattern)
                    .setPriority(priority)
                    .setHighlightColor(highlightColor);
        }

        private static <T> @Nullable List<T> emptyToNull(List<T> list) {
            return list.isEmpty() ? null : list;
        }

        private static <T> List<T> nullToEmpty(@Nullable List<T> list) {
            return list == null ? ImmutableList.of() : ImmutableList.copyOf(list);
        }
    }
}
