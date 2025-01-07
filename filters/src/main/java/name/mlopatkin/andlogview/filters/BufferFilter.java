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

package name.mlopatkin.andlogview.filters;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;

/**
 * A filter that {@linkplain FilteringMode#HIDE HIDEs} all records that do not come from the allowed buffers.
 */
public final class BufferFilter extends AbstractFilter<BufferFilter> implements PredicateFilter {
    private final ImmutableSet<LogRecord.Buffer> allowedBuffers;

    public BufferFilter(Collection<? extends LogRecord.Buffer> allowedBuffers) {
        this(ImmutableSet.copyOf(allowedBuffers), true);
    }

    private BufferFilter(ImmutableSet<LogRecord.Buffer> allowedBuffers, boolean enabled) {
        super(FilteringMode.HIDE, enabled);

        this.allowedBuffers = allowedBuffers;
    }

    @Override
    public boolean test(LogRecord logRecord) {
        return !allowedBuffers.contains(logRecord.getBuffer());
    }

    @Override
    protected BufferFilter copy(boolean enabled) {
        return new BufferFilter(allowedBuffers, enabled);
    }

    public BufferFilter allowBuffer(LogRecord.Buffer buffer) {
        if (allowedBuffers.contains(buffer)) {
            return this;
        }

        return new BufferFilter(ImmutableSet.<LogRecord.Buffer>builder().addAll(allowedBuffers).add(buffer).build(),
                isEnabled());
    }

    public BufferFilter disallowBuffer(LogRecord.Buffer buffer) {
        if (!allowedBuffers.contains(buffer)) {
            return this;
        }

        return new BufferFilter(allowedBuffers.stream().filter(b -> !b.equals(buffer)).collect(toImmutableSet()),
                isEnabled());
    }

    public Set<LogRecord.Buffer> getAllowedBuffers() {
        return allowedBuffers;
    }
}
