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

package name.mlopatkin.andlogview.parsers.logcat;


import name.mlopatkin.andlogview.base.AppResources;
import name.mlopatkin.andlogview.logmodel.LogRecord;

import com.google.common.io.CharSource;
import com.google.gson.Gson;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogcatCompatProvider implements ArgumentsProvider, AnnotationConsumer<LogcatCompatSource> {
    private static final Gson GSON = new Gson();

    private @Nullable String resourcePath;
    private final Set<Format> formats = EnumSet.noneOf(Format.class);
    private final Set<Eoln> eolns = EnumSet.of(Eoln.NONE);

    @Override
    public void accept(LogcatCompatSource logcatCompatSource) {
        resourcePath = Objects.requireNonNull(logcatCompatSource.path());
        updateSetWithEnumArray(logcatCompatSource.formats(), formats);
        updateSetWithEnumArray(logcatCompatSource.eolns(), eolns);
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        var resourcePath = Objects.requireNonNull(this.resourcePath);
        var model = loadModel(resourcePath);
        if (formats.isEmpty()) {
            formats.addAll(model.getFormats());
        }
        var basePath = new File(resourcePath).getParent();
        return formats.stream()
                .map(format -> {
                    var source = model.getResourceForFormat(basePath, format)
                            .orElseThrow(() -> new IllegalArgumentException("Don't have a source for " + format));
                    var records = model.getRecords(format);
                    return new PartialArgs(format, records, source);
                }).flatMap(args ->
                        eolns.stream().map(eoln ->
                                arguments(args.format, args.records, eoln, loadLinesWithEoln(args.lines, eoln))
                        )
                );
    }

    private static <T extends Enum<T>> void updateSetWithEnumArray(@Nullable T[] input, Set<T> output) {
        if (input != null && input.length > 0) {
            output.clear();
            output.addAll(Arrays.asList(input));
        }
    }

    private static LogcatTestJsonModel loadModel(String basePath) throws IOException {
        try (var reader = AppResources.getResource(Objects.requireNonNull(basePath))
                .asCharSource(StandardCharsets.UTF_8)
                .openBufferedStream()) {
            return GSON.fromJson(reader, LogcatTestJsonModel.class);
        }
    }

    private static List<String> loadLinesWithEoln(CharSource source, Eoln eoln) {
        try {
            try (var lines = source.lines()) {
                return lines.map(s -> s + eoln.getChars()).collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class PartialArgs {
        final Format format;
        final List<LogRecord> records;
        final CharSource lines;

        private PartialArgs(Format format, List<LogRecord> records, CharSource lines) {
            this.format = format;
            this.records = records;
            this.lines = lines;
        }
    }

    private static Arguments arguments(Format format, List<LogRecord> records, Eoln eoln, List<String> lines) {
        return Arguments.of(format, records, eoln, lines);
    }
}
