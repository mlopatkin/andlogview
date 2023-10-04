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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.CharSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class LineReaderTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "Line without EOL",
            "Line with LF\n",
            "Line with CRLF\r\n",
            "Line with CR\r",
            "\r",
            "\r\n",
            "\n",
    })
    void canReadSingleLine(String line) throws Exception {
        assertThat(lines(line)).singleElement().isEqualTo(normalizeLine(line));
    }

    @Test
    void emptySourceProducesNoLines() throws Exception {
        assertThat(lines("")).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "\n",
            "\r",
            "\r\n"
    })
    void canReadSeveralLines(String eol) throws Exception {
        String input = convertEols("""
                First line
                Second line
                Third line
                """, eol);

        assertThat(lines(input)).containsExactly(
                "First line",
                "Second line",
                "Third line"
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "\n",
            "\r",
            "\r\n"
    })
    void canReadSeveralLinesWhereLastHasNoEol(String eol) throws Exception {
        String input = convertEols("""
                First line
                Second line
                Third line""", eol);

        assertThat(lines(input)).containsExactly(
                "First line",
                "Second line",
                "Third line"
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "\n",
            "\r",
            "\r\n"
    })
    void canReadSeveralLinesWhereLastIsBlank(String eol) throws Exception {
        String input = convertEols("""
                First line
                Second line

                """, eol);

        assertThat(lines(input)).containsExactly(
                "First line",
                "Second line",
                ""
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "\n",
            "\r",
            "\r\n"
    })
    void canReadSeveralLinesWhereAllAreBlank(String eol) throws Exception {
        String input = convertEols("""



                """, eol);

        assertThat(lines(input)).containsExactly(
                "",
                "",
                ""
        );
    }

    @Test
    void canReadMixedEols() throws Exception {
        String input = "\n\r\r\r\n\n\n\r\n";
        assertThat(lines(input)).allMatch(""::equals).hasSize(7);
    }

    private List<String> lines(String source) throws IOException {
        List<String> result = new ArrayList<>();
        try (var reader = createReader(source)) {
            var line = reader.readLine();
            while (line != null) {
                result.add(line.toString());
                line = reader.readLine();
            }
        }
        return result;
    }

    private LineReader createReader(String source) throws IOException {
        // Use small buffer size to ensure that all funny behaviors triggered by characters split between chunks
        // happen in tests.
        return new LineReader(CharSource.wrap(source), 1);
    }

    private String normalizeLine(String line) {
        return line.replaceAll("[\\r\\n]", "");
    }

    private String convertEols(String line, String newEol) {
        return line.replace("\n", newEol);
    }
}
