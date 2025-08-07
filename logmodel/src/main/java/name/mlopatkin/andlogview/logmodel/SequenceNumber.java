/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.logmodel;

import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;

import com.google.common.base.Preconditions;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A sequence number of the log record. Log records produced by logcat have a strict writing order which is unrelated to
 * the timestamps. Timestamps can be affected by time adjustments or, in case of MM-DD timestamp, a New Year. The
 * writing order is stable. In general writing order is better suited for the log entry ordering, as it represents the
 * order in which log records appear. Modern versions of logcat (since Android 5.0) support global ordering across all
 * buffers except kernel. Earlier versions are writing logs in per-buffer storages, so there is no ordering between
 * different buffers.
 * <p>
 * This class takes into account which buffers have the consistent ordering when comparing two numbers.
 */
public class SequenceNumber implements Comparable<SequenceNumber> {
    private final int seqNo;
    private final @Nullable Buffer buffer;

    SequenceNumber(int seqNo, @Nullable Buffer buffer) {
        this.seqNo = seqNo;
        this.buffer = buffer;
    }

    @Override
    public int compareTo(SequenceNumber o) {
        Preconditions.checkArgument(isComparableTo(o), "Comparing non-comparable sequence numbers");
        return Integer.compare(seqNo, o.seqNo);
    }

    public boolean isComparableTo(SequenceNumber o) {
        return Objects.equals(buffer, o.buffer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof SequenceNumber that) {
            return seqNo == that.seqNo && buffer == that.buffer;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seqNo, buffer);
    }
}
