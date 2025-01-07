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

import static name.mlopatkin.andlogview.logmodel.LogRecordUtils.forBuffer;
import static name.mlopatkin.andlogview.logmodel.LogRecordUtils.forUnknownBuffer;

import static org.assertj.core.api.Assertions.assertThat;

import static java.util.stream.Collectors.toList;

import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;
import name.mlopatkin.andlogview.logmodel.LogRecordUtils;

import com.google.common.collect.ImmutableSet;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

class BufferFilterTest {
    @Test
    void defaultFilterHidesEverything() {
        var filter = createFilter();

        assertThat(filter).acceptsAll(
                Stream.concat(
                        Stream.of(forUnknownBuffer()),
                        Stream.of(Buffer.values()).map(LogRecordUtils::forBuffer)
                ).collect(toList())
        );

        assertThat(filter.getAllowedBuffers()).isEmpty();
        assertThat(filter.isEnabled()).isTrue();
    }

    @Test
    void canCreateFilterWithSomeBuffersAllowed() {
        var filter = createFilter(Buffer.MAIN, Buffer.EVENTS);

        assertThat(filter).rejects(forBuffer(Buffer.MAIN), forBuffer(Buffer.EVENTS));
        assertThat(filter).accepts(forBuffer(Buffer.RADIO), forUnknownBuffer());

        assertThat(filter.getAllowedBuffers()).containsOnly(Buffer.MAIN, Buffer.EVENTS);
        assertThat(filter.isEnabled()).isTrue();
    }

    @Test
    void canAllowFilterLater() {
        var filter = createFilter(Buffer.MAIN).allowBuffer(Buffer.EVENTS);

        assertThat(filter).rejects(forBuffer(Buffer.MAIN), forBuffer(Buffer.EVENTS));
        assertThat(filter).accepts(forBuffer(Buffer.RADIO), forUnknownBuffer());

        assertThat(filter.getAllowedBuffers()).containsOnly(Buffer.MAIN, Buffer.EVENTS);
        assertThat(filter.isEnabled()).isTrue();
    }

    @Test
    void canAllowExistingFilter() {
        var filter = createFilter(Buffer.MAIN).allowBuffer(Buffer.MAIN);

        assertThat(filter).rejects(forBuffer(Buffer.MAIN));
        assertThat(filter).accepts(forBuffer(Buffer.RADIO), forUnknownBuffer());

        assertThat(filter.getAllowedBuffers()).containsOnly(Buffer.MAIN);
        assertThat(filter.isEnabled()).isTrue();
    }

    @Test
    void canDisallowFilterLater() {
        var filter = createFilter(Buffer.MAIN, Buffer.EVENTS).disallowBuffer(Buffer.MAIN);

        assertThat(filter).rejects(forBuffer(Buffer.EVENTS));
        assertThat(filter).accepts(forBuffer(Buffer.MAIN), forBuffer(Buffer.RADIO), forUnknownBuffer());

        assertThat(filter.getAllowedBuffers()).containsOnly(Buffer.EVENTS);
        assertThat(filter.isEnabled()).isTrue();
    }

    private BufferFilter createFilter(Buffer... buffers) {
        return new BufferFilter(ImmutableSet.copyOf(buffers));
    }
}
