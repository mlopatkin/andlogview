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

package name.mlopatkin.andlogview;

import name.mlopatkin.andlogview.base.LateInit;
import name.mlopatkin.andlogview.logmodel.BatchRecordsReceiver;
import name.mlopatkin.andlogview.logmodel.BufferedListener;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.RecordListener;
import name.mlopatkin.andlogview.test.TestData;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.List;

/**
 * A performance tests that estimates the responsibility of the EDT aka UI thread when the record listener is being fed
 * with new data. A naive implementation is prone to overflowing the EDT queue with tasks leaving no room to user input
 * processing.
 * <p>
 * The benchmark also estimates the speed of submitting the tasks to the record listener. However, there is no
 * measurement for the latency, i.e. how much time the record spends in the queue.
 */
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 10, time = 2)  // Timings are important or baseline will overflow the EDT queue with tasks.
@State(Scope.Benchmark)
@Fork(1)
@BenchmarkMode(Mode.Throughput)
public abstract class BufferedListenerPerfTest {
    private static final String GROUP = "LogRecordProcessor";

    @LateInit
    protected BatchRecordsReceiver<LogRecord> consumer;

    protected abstract RecordListener<LogRecord> createListener();

    @LateInit
    protected RecordListener<LogRecord> listener;

    @Setup(Level.Trial)
    public void setUp(Blackhole blackhole) throws Exception {

        consumer = new BatchRecordsReceiver<>() {

            @Override
            public void addRecords(List<LogRecord> records) {
                Blackhole.consumeCPU(records.size());
                blackhole.consume(records);
            }

            @Override
            public void setRecords(List<LogRecord> records) {
                blackhole.consume(records);
            }
        };
    }

    @Setup(Level.Iteration)
    public void setUpListener() throws Exception {
        listener = createListener();
    }

    @Benchmark
    @Group(GROUP)
    @GroupThreads(4)
    public void write() throws Exception {
        listener.addRecord(TestData.RECORD1);
    }

    @Benchmark
    @Group(GROUP)
    public void read() throws Exception {
        // Single thread that verifies the responsiveness of the EDT. The faster invokeAndWait completes, the less
        // crowded the queue is.
        EventQueue.invokeAndWait(() -> {});
    }

    @TearDown(Level.Iteration)
    public void tearDownEventQueue() throws Exception {
        // Wait for the queue to clean up pending tasks. This doesn't work well if the running task posts other work,
        // but there are no such implementation currently.
        EventQueue.invokeAndWait(() -> {});
    }


    /**
     * Baseline is to just post tasks for each record into the queue.
     */
    @SuppressWarnings("unused")
    public static class Baseline extends BufferedListenerPerfTest {
        @Override
        protected RecordListener<LogRecord> createListener() {
            return new RecordListener<>() {
                @Override
                public void addRecord(LogRecord record) {
                    EventQueue.invokeLater(() -> consumer.addRecords(Collections.singletonList(record)));
                }

                @Override
                public void setRecords(List<LogRecord> records) {}
            };
        }
    }

    @SuppressWarnings("unused")
    public static class Buffered extends BufferedListenerPerfTest {
        @Override
        protected RecordListener<LogRecord> createListener() {
            return new BufferedListener<>(consumer, EventQueue::invokeLater, LogRecord.LEGACY_COMPARATOR);
        }
    }
}
