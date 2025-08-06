/*
 * Copyright 2023 the Andlogview authors
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

package name.mlopatkin.andlogview.base.io;

import com.google.common.io.CharSource;
import com.google.common.io.Resources;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.function.Supplier;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@State(Scope.Benchmark)
@Fork(3)
public class LineReaderPerfTest {
    @Param({"lf", "crlf", "random"})
    private String eoln;

    private CharSource input;

    @Setup(Level.Trial)
    public void setUp() throws Exception {
        input = CharSource.wrap(loadWithLfConversion(loadResource("test_log_lf.log")));
    }

    private String loadWithLfConversion(CharSource source) throws IOException {
        Supplier<String> eolnFactory = switch (eoln) {
            case "lf" -> () -> "\n";
            case "cr" -> () -> "\r";
            case "crlf" -> () -> "\r\n";
            case "random" -> randomEolnGenerator();
            default -> throw new IllegalArgumentException("Unknown eoln mode " + eoln);
        };
        StringBuilder output = new StringBuilder();
        try (var reader = source.openBufferedStream()) {
            int ch = reader.read();
            while (ch != -1) {
                if (ch == '\n') {
                    output.append(eolnFactory.get());
                } else {
                    output.append((char) ch);
                }
                ch = reader.read();
            }
        }
        return output.toString();
    }

    private Supplier<String> randomEolnGenerator() {
        var rnd = new Random(1337L);
        String[] eolns = {"\n", "\r\n"};
        return () -> eolns[rnd.nextInt(2)];
    }

    @Benchmark
    public void parseWithBufferedReader(Blackhole bh) throws IOException {
        try (var in = input.openBufferedStream()) {
            String line = in.readLine();
            while (line != null) {
                bh.consume(line);
                line = in.readLine();
            }
        }
    }

    @Benchmark
    public void parseWithLineReader(Blackhole bh) throws IOException {
        try (var in = new LineReader(input, 8192)) {
            var line = in.readLine();
            while (line != null) {
                bh.consume(line);
                line = in.readLine();
            }
        }
    }

    public static CharSource loadResource(String benchmarkDataFile) {
        return Resources.asCharSource(Resources.getResource(LineReaderPerfTest.class, benchmarkDataFile),
                StandardCharsets.UTF_8);
    }
}
