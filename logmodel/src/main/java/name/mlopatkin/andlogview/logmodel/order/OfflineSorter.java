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

package name.mlopatkin.andlogview.logmodel.order;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.SequenceNumber;
import name.mlopatkin.andlogview.logmodel.Timestamp;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Merges multiple lists of log records into a single list. All records are known beforehand.
 */
public class OfflineSorter {
    private final ListMultimap<SequenceNumber, LogRecord> buffer = ArrayListMultimap.create();
    private boolean hasTimeTravels;

    public void add(LogRecord record) {
        Preconditions.checkArgument(record.getTime() != null, "Records without timestamp cannot be sorted");

        // TODO(mlopatkin) Maybe create a ComparisonToken type to have just put() here?
        for (SequenceNumber leader : buffer.keySet()) {
            if (leader.isComparableTo(record.getSeqNo())) {
                var existing = buffer.get(leader);
                hasTimeTravels = hasTimeTravels || checkTimeTravel(existing, record);
                existing.add(record);
                return;
            }
        }
        buffer.put(record.getSeqNo(), record);
    }

    private boolean checkTimeTravel(List<LogRecord> existing, LogRecord newRecord) {
        if (!existing.isEmpty()) {
            LogRecord lastExisting = existing.get(existing.size() - 1);
            assert lastExisting.getSeqNo().isComparableTo(newRecord.getSeqNo());

            // TODO(mlopatkin) we can insert the record into a proper place still, or sort the records by sequence
            //   number when building.
            Preconditions.checkArgument(lastExisting.getSeqNo().compareTo(newRecord.getSeqNo()) < 0,
                    "The new record <%s> is out of order", newRecord);
            var lastTimestamp = lastExisting.getTime();
            var newTimestamp = newRecord.getTime();
            assert lastTimestamp != null && newTimestamp != null;

            return lastTimestamp.compareTo(newTimestamp) > 0;
        }
        return false;
    }

    public boolean hasTimeTravels() {
        return hasTimeTravels;
    }

    public List<LogRecord> buildTimestampOrdered() {
        var output = new ArrayList<LogRecord>(buffer.size());
        buffer.asMap().values().forEach(output::addAll);
        output.sort(timestampBufferComparator());
        return output;
    }

    public List<LogRecord> build() {
        var output = new ArrayList<LogRecord>(buffer.size());

        // When time travels are present, it isn't possible to sort the whole sequence at once, as the comparator is no
        // longer transitive. Suppose we have three records:
        // - A := seq(1, A), time(0:20)
        // - B := seq(1, B), time(0:10)
        // - C := seq(2, A), time(0:00)
        // We see that A < C because sequence, B < A because time. If the ordering relation is transitive then
        // B < C should follow. However, because of time travel, we observe C < B with the given comparator.
        if (!hasTimeTravels) {
            buffer.asMap().values().forEach(output::addAll);
            output.sort(timeBasedCrossSequenceComparator());
        } else {
            sortWithTimeTravel(output);
        }

        return output;
    }

    private void sortWithTimeTravel(List<LogRecord> output) {
        // This is a very limited heuristic. It assumes that every time travel is present in every buffer.
        // Each buffer is split at time travel boundaries. Then we take one split from each buffer and merge & sort
        // these splits, until no more splits left.
        // This should work fine for larger time adjustments and spammy logs, but may fail in other cases, e.g. when
        // one buffer has time travel detected and another one doesn't. Imagine:
        // A: 0:00, 0:10, | 0:09, 0:11
        // B: 0:01, | 0:09, 0:12
        // where `|` symbol marks the time travel moment. The algorithm would merge
        // [0:00, 0:10] and [0:01, 0:09, 0:12], then take [0:09, 0:11] from A as a tail, giving
        // [0:00, 0:01, 0:09, 0:10, 0:12, 0:09, 0:11] instead of correct
        // [0:00, 0:01, 0:10, 0:09, 0:09, 0:11, 0:12].
        // However, the result would be correct for B: 0:01, 0:09, 0:12 |, so the correct solution without a priori
        // knowledge of the time travel moment is impossible.
        // One important example of non-spammy logs is the crash log where only crashes are present.

        @SuppressWarnings("SimplifyStreamApiCallChains")  // suggests API from Java 16.
        var splitters = buffer.keySet().stream().map(buffer::get).map(BufferSplitter::new).collect(Collectors.toList());
        var comparator = timeBasedCrossSequenceComparator();

        while (!splitters.isEmpty()) {
            int prevSize = output.size();
            for (Iterator<BufferSplitter> iter = splitters.iterator(); iter.hasNext(); ) {
                var splitter = iter.next();
                output.addAll(splitter.next());
                if (!splitter.hasNext()) {
                    iter.remove();
                }
            }

            output.subList(prevSize, output.size()).sort(comparator);
        }
    }

    /**
     * Creates a comparator that orders entries within the same sequence by the sequence number. Different sequences are
     * sorted by timestamp. Note that this comparator requires all timestamps to be consistent with sequence numbers,
     * otherwise the comparison wouldn't be transitive.
     *
     * @return the comparator
     */
    private static Comparator<LogRecord> timeBasedCrossSequenceComparator() {
        Comparator<LogRecord> crossSequenceComparator = timestampBufferComparator();

        return (o1, o2) -> {
            if (o1.getSeqNo().isComparableTo(o2.getSeqNo())) {
                return o1.getSeqNo().compareTo(o2.getSeqNo());
            }
            return crossSequenceComparator.compare(o1, o2);
        };
    }

    /**
     * Creates a comparator that orders entries based on the timestamp. It doesn't take a sequence number into account
     * at all. This comparator uses record's buffer as a tiebreaker. The comparator is not null-safe but can handle null
     * timestamps and buffers.
     *
     * @return the comparator
     */
    private static Comparator<LogRecord> timestampBufferComparator() {
        Comparator<LogRecord.Buffer> bufferComparator = Comparator.nullsFirst(Comparator.naturalOrder());
        Comparator<Timestamp> timestampComparator = Comparator.nullsFirst(Comparator.naturalOrder());

        return Comparator.comparing(LogRecord::getTime, timestampComparator)
                .thenComparing(LogRecord::getBuffer, bufferComparator);
    }

    private static class BufferSplitter extends AbstractIterator<List<LogRecord>> {
        private final List<LogRecord> records;
        private int pos;

        public BufferSplitter(List<LogRecord> records) {
            this.records = records;
        }

        @Override
        protected @Nullable List<LogRecord> computeNext() {
            if (pos == records.size()) {
                return endOfData();
            }
            int start = pos;
            Timestamp prevTime = records.get(start).getTime();
            assert prevTime != null;

            while (++pos < records.size()) {
                Timestamp currentTime = records.get(pos).getTime();
                assert currentTime != null;
                if (prevTime.compareTo(currentTime) > 0) {
                    break;
                }
                prevTime = currentTime;
            }
            return records.subList(start, pos);
        }
    }
}
