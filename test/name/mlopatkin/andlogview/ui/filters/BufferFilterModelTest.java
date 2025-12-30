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

import static name.mlopatkin.andlogview.logmodel.LogRecord.Buffer.CRASH;
import static name.mlopatkin.andlogview.logmodel.LogRecord.Buffer.EVENTS;
import static name.mlopatkin.andlogview.logmodel.LogRecord.Buffer.MAIN;
import static name.mlopatkin.andlogview.logmodel.LogRecord.Buffer.RADIO;
import static name.mlopatkin.andlogview.logmodel.LogRecord.Buffer.SYSTEM;
import static name.mlopatkin.andlogview.logmodel.LogRecordUtils.forBuffer;
import static name.mlopatkin.andlogview.logmodel.LogRecordUtils.forUnknownBuffer;

import static org.assertj.core.api.Assertions.assertThat;

import static java.util.stream.Collectors.toSet;

import name.mlopatkin.andlogview.config.Preference;
import name.mlopatkin.andlogview.filters.FilterChain;
import name.mlopatkin.andlogview.filters.MutableFilterModel;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;
import name.mlopatkin.andlogview.logmodel.LogRecordUtils;

import com.google.common.collect.ImmutableSet;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

class BufferFilterModelTest {
    private final MutableFilterModel filterModel = MutableFilterModel.create();

    @Test
    void noBuffersAreEnabledByDefault() {
        var model = createModel();

        for (var buffer : Buffer.values()) {
            assertThat(model.isBufferEnabled(buffer)).isFalse();
        }

        assertThat(shouldShow()).rejectsAll(
                Stream.concat(
                        Stream.of(forUnknownBuffer()),
                        Stream.of(Buffer.values()).map(LogRecordUtils::forBuffer)
                ).collect(toSet())
        );
    }

    @Test
    void canDisableFiltering() {
        var model = createModel();

        model.setBufferFilteringEnabled(false);

        for (var buffer : Buffer.values()) {
            assertThat(model.isBufferEnabled(buffer)).isFalse();
        }

        assertThat(shouldShow()).acceptsAll(
                Stream.concat(
                        Stream.of(forUnknownBuffer()),
                        Stream.of(Buffer.values()).map(LogRecordUtils::forBuffer)
                ).collect(toSet())
        );
    }

    @Test
    void canEnableDisabledFiltering() {
        var model = createModel();

        model.setBufferFilteringEnabled(false);
        model.setBufferFilteringEnabled(true);

        assertThat(shouldShow()).rejectsAll(
                Stream.concat(
                        Stream.of(forUnknownBuffer()),
                        Stream.of(Buffer.values()).map(LogRecordUtils::forBuffer)
                ).collect(toSet())
        );
    }

    @Test
    void canAllowBuffersWhenCreatingModel() {
        var model = createModel(CRASH, MAIN);

        assertThat(model.isBufferEnabled(MAIN)).isTrue();
        assertThat(model.isBufferEnabled(CRASH)).isTrue();
        assertThat(model.isBufferEnabled(EVENTS)).isFalse();

        assertThat(shouldShow()).accepts(
                forBuffer(MAIN),
                forBuffer(CRASH)
        );

        assertThat(shouldShow()).rejects(
                forBuffer(EVENTS),
                forUnknownBuffer()
        );
    }

    @Test
    void reenablingFilteringKeepsAllowedBuffers() {
        var model = createModel(CRASH, MAIN);

        model.setBufferFilteringEnabled(false);
        model.setBufferFilteringEnabled(true);

        assertThat(model.isBufferEnabled(MAIN)).isTrue();
        assertThat(model.isBufferEnabled(CRASH)).isTrue();
        assertThat(model.isBufferEnabled(EVENTS)).isFalse();

        assertThat(shouldShow()).accepts(
                forBuffer(MAIN),
                forBuffer(CRASH)
        );

        assertThat(shouldShow()).rejects(
                forBuffer(EVENTS),
                forUnknownBuffer()
        );
    }

    @Test
    void canEnableBufferLater() {
        var model = createModel(MAIN);
        model.setBufferEnabled(CRASH, true);

        assertThat(model.isBufferEnabled(MAIN)).isTrue();
        assertThat(model.isBufferEnabled(CRASH)).isTrue();
        assertThat(model.isBufferEnabled(EVENTS)).isFalse();

        assertThat(shouldShow()).accepts(
                forBuffer(MAIN),
                forBuffer(CRASH)
        );

        assertThat(shouldShow()).rejects(
                forBuffer(EVENTS),
                forUnknownBuffer()
        );
    }

    @Test
    void canDisableBufferLater() {
        var model = createModel(MAIN, CRASH, EVENTS);
        model.setBufferEnabled(EVENTS, false);

        assertThat(model.isBufferEnabled(MAIN)).isTrue();
        assertThat(model.isBufferEnabled(CRASH)).isTrue();
        assertThat(model.isBufferEnabled(EVENTS)).isFalse();

        assertThat(shouldShow()).accepts(
                forBuffer(MAIN),
                forBuffer(CRASH)
        );

        assertThat(shouldShow()).rejects(
                forBuffer(EVENTS),
                forUnknownBuffer()
        );
    }

    @Test
    void preferencePersistsDisabledAndEnabledBuffers() {
        var pref = createPref(MAIN, SYSTEM, CRASH);
        var model1 = createModel(MutableFilterModel.create(), pref);

        model1.setBufferEnabled(SYSTEM, false);
        model1.setBufferEnabled(RADIO, true);

        var model2 = createModel(filterModel, pref);

        assertThat(model2.isBufferEnabled(MAIN)).isTrue();
        assertThat(model2.isBufferEnabled(RADIO)).isTrue();
        assertThat(model2.isBufferEnabled(SYSTEM)).isFalse();

        assertThat(shouldShow()).accepts(
                forBuffer(MAIN),
                forBuffer(RADIO)
        );
        assertThat(shouldShow()).rejects(
                forBuffer(SYSTEM),
                forBuffer(EVENTS)
        );
    }

    private Preference<Set<Buffer>> createPref(Buffer... enabledBuffers) {
        return new Preference<>() {
            ImmutableSet<Buffer> buffers = ImmutableSet.copyOf(enabledBuffers);

            @Override
            public boolean isSet() {
                return true;
            }

            @Override
            public void set(Set<Buffer> value) {
                buffers = ImmutableSet.copyOf(value);
            }

            @Override
            public Set<Buffer> get() {
                return buffers;
            }
        };
    }

    private BufferFilterModel createModel(Buffer... enabledBuffers) {
        return createModel(filterModel, createPref(enabledBuffers));
    }

    private BufferFilterModel createModel(MutableFilterModel model, Preference<Set<Buffer>> pref) {
        return new BufferFilterModel(model, pref);
    }

    @SuppressWarnings("resource")
    private Predicate<LogRecord> shouldShow() {
        // Leaking FilterChain is fine here, because model goes away with the test.
        var chain = new FilterChain(filterModel);
        return chain::shouldShow;
    }
}
