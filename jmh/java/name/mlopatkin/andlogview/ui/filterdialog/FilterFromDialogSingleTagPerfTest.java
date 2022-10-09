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

package name.mlopatkin.andlogview.ui.filterdialog;

import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.jmh.BenchmarkResources;
import name.mlopatkin.andlogview.liblogcat.LogRecordParser;
import name.mlopatkin.andlogview.logmodel.LogRecord;

import com.google.common.collect.ImmutableList;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Thread)
@Fork(1)
public class FilterFromDialogSingleTagPerfTest {
    private @MonotonicNonNull ImmutableList<LogRecord> records;

    private @MonotonicNonNull Predicate<LogRecord> simplestPredicate;
    private @MonotonicNonNull Predicate<LogRecord> compiledPredicatePlainText;

    @Setup(Level.Trial)
    public void setUp() throws Exception {
        try (Stream<String> lines = BenchmarkResources.loadResource("goldfish_omr1_threadtime.log").lines()) {
            records = lines.map(line -> LogRecordParser.parseThreadTime(line, Collections.emptyMap()))
                    .filter(Objects::nonNull)
                    .collect(ImmutableList.toImmutableList());
        }

        simplestPredicate = p -> "ActivityManager".equalsIgnoreCase(p.getTag());


        FilterFromDialog plainTextFilter = new FilterFromDialog().setMode(FilteringMode.SHOW).setTags(
                Collections.singletonList("ActivityManager"));
        plainTextFilter.initialize();
        compiledPredicatePlainText = plainTextFilter;
    }

    @Benchmark
    public int baselineBenchmark() {
        int count = 0;
        for (LogRecord record : records) {
            if ("ActivityManager".equalsIgnoreCase(record.getTag())) {
                ++count;
            }
        }
        return count;
    }

    @Benchmark
    public int simplePredicateBenchmark() {
        int count = 0;
        Predicate<LogRecord> recordPredicate = simplestPredicate;
        for (LogRecord record : records) {
            if (recordPredicate.test(record)) {
                ++count;
            }
        }
        return count;
    }

    @Benchmark
    public int plainTextFilterBenchmark() {
        int count = 0;
        Predicate<LogRecord> recordPredicate = compiledPredicatePlainText;
        for (LogRecord record : records) {
            if (recordPredicate.test(record)) {
                ++count;
            }
        }
        return count;
    }
}
