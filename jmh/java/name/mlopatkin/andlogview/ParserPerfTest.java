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

import name.mlopatkin.andlogview.jmh.BenchmarkResources;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.Timestamp;
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.logcat.LogcatParseEventsHandler;
import name.mlopatkin.andlogview.parsers.logcat.LogcatParsers;
import name.mlopatkin.andlogview.parsers.logcat.LogcatPushParser;

import com.google.common.collect.ImmutableList;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@State(Scope.Thread)
@Fork(1)
public class ParserPerfTest {
    @Param({"100", "1000", "5000"})
    public int listSize;

    private @MonotonicNonNull ImmutableList<String> lines;

    private static class Collector implements LogcatParseEventsHandler {
        private final List<LogRecord> parsedRecords = new ArrayList<>();

        @Override
        public ParserControl logRecord(String message) {
            return logcatEntryCompleted(
                    LogRecord.createWithoutTimestamp(-1, -1, null, LogRecord.Priority.INFO, "", message));
        }

        @Override
        public ParserControl logRecord(Timestamp timestamp, int pid, int tid, LogRecord.Priority priority, String tag,
                String message) {
            return logcatEntryCompleted(
                    LogRecord.createWithTimestamp(timestamp, pid, tid, null, priority, tag, message));
        }

        private ParserControl logcatEntryCompleted(LogRecord record) {
            parsedRecords.add(record);
            return ParserControl.proceed();
        }

        public List<LogRecord> getParsedRecords() {
            return parsedRecords;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Setup(Level.Trial)
    public void setUp() throws Exception {
        try (Stream<String> lines = BenchmarkResources.loadResource("goldfish_omr1_threadtime.log").lines()) {
            this.lines = lines
                    .filter(Objects::nonNull)
                    .limit(listSize)
                    .collect(ImmutableList.toImmutableList());
        }
    }

    @Benchmark
    public void parseWithPushParser(Blackhole bh) {
        Collector cl = new Collector();
        try (LogcatPushParser pushParser = LogcatParsers.threadTime(cl)) {
            for (String line : lines) {
                if (!pushParser.nextLine(line)) {
                    break;
                }
            }
        }
        bh.consume(cl.getParsedRecords());
    }
}
