/*
 * Copyright 2020 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.liblogcat.filters;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordUtils;
import org.junit.Test;

import static org.bitbucket.mlopatkin.utils.PredicateMatchers.accepts;
import static org.bitbucket.mlopatkin.utils.PredicateMatchers.rejects;
import static org.junit.Assert.assertThat;

public class LogBufferFilterTest {

    @Test
    public void defaultBufferFilterAcceptsRecordsWithNullBuffer() {
        LogBufferFilter filter = new LogBufferFilter();
        assertThat(filter, accepts(LogRecordUtils.forBuffer(null)));
    }

    @Test
    public void defaultBufferFilterDiscardsRecordsWithNonnullBuffer() {
        LogBufferFilter filter = new LogBufferFilter();
        assertThat(filter, rejects(LogRecordUtils.forBuffer(Buffer.MAIN)));
    }

    @Test
    public void filterWithOneBufferAcceptsThisBuffer() {
        LogBufferFilter filter = new LogBufferFilter();
        filter.setBufferEnabled(Buffer.MAIN, true);
        assertThat(filter, accepts(LogRecordUtils.forBuffer(Buffer.MAIN)));
    }

    @Test
    public void filterWithOneBufferAcceptsNullBuffer() {
        LogBufferFilter filter = new LogBufferFilter();
        filter.setBufferEnabled(Buffer.MAIN, true);
        assertThat(filter, accepts(LogRecordUtils.forBuffer(null)));
    }

    @Test
    public void filterWithOneBufferRejectsOtherBuffers() {
        LogBufferFilter filter = new LogBufferFilter();
        filter.setBufferEnabled(Buffer.MAIN, true);
        assertThat(filter, rejects(LogRecordUtils.forBuffer(Buffer.EVENTS)));
    }

    @Test
    public void filterWithTwoBufferAcceptsTheseBuffers() {
        LogBufferFilter filter = new LogBufferFilter();
        filter.setBufferEnabled(Buffer.MAIN, true);
        filter.setBufferEnabled(Buffer.SYSTEM, true);
        assertThat(filter, accepts(LogRecordUtils.forBuffer(Buffer.MAIN)));
        assertThat(filter, accepts(LogRecordUtils.forBuffer(Buffer.SYSTEM)));
    }

    @Test
    public void filterWithTwoBuffersAcceptsNullBuffer() {
        LogBufferFilter filter = new LogBufferFilter();
        filter.setBufferEnabled(Buffer.MAIN, true);
        filter.setBufferEnabled(Buffer.SYSTEM, true);
        assertThat(filter, accepts(LogRecordUtils.forBuffer(null)));
    }

    @Test
    public void filterWithTwoBufferRejectsOtherBuffers() {
        LogBufferFilter filter = new LogBufferFilter();
        filter.setBufferEnabled(Buffer.MAIN, true);
        filter.setBufferEnabled(Buffer.SYSTEM, true);
        assertThat(filter, rejects(LogRecordUtils.forBuffer(Buffer.EVENTS)));
    }

    @Test
    public void filterRejectsBufferAfterItWasDisabled() {
        LogBufferFilter filter = new LogBufferFilter();
        filter.setBufferEnabled(Buffer.MAIN, true);
        filter.setBufferEnabled(Buffer.SYSTEM, true);
        filter.setBufferEnabled(Buffer.SYSTEM, false);
        assertThat(filter, rejects(LogRecordUtils.forBuffer(Buffer.SYSTEM)));
    }

    @Test
    public void filterAcceptsBufferThatIsStillEnabled() {
        LogBufferFilter filter = new LogBufferFilter();
        filter.setBufferEnabled(Buffer.MAIN, true);
        filter.setBufferEnabled(Buffer.SYSTEM, true);
        filter.setBufferEnabled(Buffer.SYSTEM, false);
        assertThat(filter, accepts(LogRecordUtils.forBuffer(Buffer.MAIN)));
    }

    @Test
    public void nullBufferIsAcceptedIfAllAreDisabled() {
        LogBufferFilter filter = new LogBufferFilter();
        filter.setBufferEnabled(Buffer.MAIN, true);
        filter.setBufferEnabled(Buffer.SYSTEM, true);
        filter.setBufferEnabled(Buffer.SYSTEM, false);
        filter.setBufferEnabled(Buffer.MAIN, false);
        assertThat(filter, accepts(LogRecordUtils.forBuffer(null)));
    }

    @Test
    public void secondEnablingOfBufferIsNoop() {
        LogBufferFilter filter = new LogBufferFilter();
        filter.setBufferEnabled(Buffer.MAIN, true);
        filter.setBufferEnabled(Buffer.MAIN, true);
        assertThat(filter, accepts(LogRecordUtils.forBuffer(Buffer.MAIN)));
    }

    @Test
    public void secondDisablingOfBufferIsNoop() {
        LogBufferFilter filter = new LogBufferFilter();
        filter.setBufferEnabled(Buffer.MAIN, true);
        filter.setBufferEnabled(Buffer.MAIN, false);
        filter.setBufferEnabled(Buffer.MAIN, false);
        assertThat(filter, rejects(LogRecordUtils.forBuffer(Buffer.MAIN)));
    }
}
