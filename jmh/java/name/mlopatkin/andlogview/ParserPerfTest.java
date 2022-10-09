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
import name.mlopatkin.andlogview.parsers.ParserUtils;
import name.mlopatkin.andlogview.parsers.logcat.ListCollectingHandler;
import name.mlopatkin.andlogview.parsers.logcat.LogcatParsers;

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
        ListCollectingHandler cl = new ListCollectingHandler();
        try (var pushParser = LogcatParsers.threadTime(cl)) {
            ParserUtils.readInto(pushParser, lines.stream());
        }
        bh.consume(cl.getCollectedRecords());
    }
}
