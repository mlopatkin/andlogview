/*
 * Copyright 2025 the Andlogview authors
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

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.Preference;
import name.mlopatkin.andlogview.config.SimpleClient;
import name.mlopatkin.andlogview.filters.BufferFilter;
import name.mlopatkin.andlogview.filters.MutableFilterModel;
import name.mlopatkin.andlogview.logmodel.LogRecord;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.reflect.TypeToken;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

public class BufferFilterModel {
    private final MutableFilterModel filterModel;
    private final Preference<Set<LogRecord.Buffer>> enabledBuffersPref;

    private BufferFilter currentFilter;

    @VisibleForTesting
    BufferFilterModel(MutableFilterModel filterModel, Preference<Set<LogRecord.Buffer>> enabledBuffersPref) {
        this.filterModel = filterModel;
        this.enabledBuffersPref = enabledBuffersPref;

        this.currentFilter = new BufferFilter(
                Stream.of(LogRecord.Buffer.values())
                        .filter(enabledBuffersPref.get()::contains)
                        .collect(toImmutableSet())
        );

        filterModel.addFilter(currentFilter);
    }

    BufferFilterModel(ConfigStorage configStorage, MutableFilterModel filterModel) {
        this(
                filterModel,
                enabledBuffersPref(configStorage)
        );
    }

    MutableFilterModel getConfiguredFilterModel() {
        return filterModel;
    }

    private void setFilter(BufferFilter filter) {
        filterModel.replaceFilter(currentFilter, filter);
        currentFilter = filter;

        enabledBuffersPref.set(currentFilter.getAllowedBuffers());
    }

    public void setBufferFilteringEnabled(boolean enabled) {
        setFilter(enabled ? currentFilter.enabled() : currentFilter.disabled());
    }

    public boolean isBufferEnabled(LogRecord.Buffer buffer) {
        return currentFilter.getAllowedBuffers().contains(buffer);
    }

    public void setBufferEnabled(LogRecord.Buffer buffer, boolean enabled) {
        setFilter(enabled ? currentFilter.allowBuffer(buffer) : currentFilter.disallowBuffer(buffer));
    }

    public static Preference<Set<LogRecord.Buffer>> enabledBuffersPref(ConfigStorage configStorage) {
        return configStorage.preference(new SimpleClient<>(
                "logcatBuffers",
                new TypeToken<>() {},
                () -> EnumSet.of(LogRecord.Buffer.MAIN, LogRecord.Buffer.SYSTEM, LogRecord.Buffer.CRASH)
        ));
    }
}
