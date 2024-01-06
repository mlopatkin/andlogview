/*
 * Copyright 2024 the Andlogview authors
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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CommandLineTest {
    @Test
    void canParseDefaultCommandLine() {
        var cmd = CommandLine.fromArgs();

        assertThat(cmd.isShouldShowUsage()).isFalse();
        assertThat(cmd.getFileArgument()).isNull();
        assertThat(cmd.isDebug()).isFalse();
    }

    @Test
    void canParseDebugArgument() {
        var cmd = CommandLine.fromArgs("-d");

        assertThat(cmd.isShouldShowUsage()).isFalse();
        assertThat(cmd.getFileArgument()).isNull();
        assertThat(cmd.isDebug()).isTrue();
    }

    @Test
    void canParseFileArgument() {
        var cmd = CommandLine.fromArgs("input.txt");

        assertThat(cmd.isShouldShowUsage()).isFalse();
        assertThat(cmd.getFileArgument()).hasName("input.txt");
        assertThat(cmd.isDebug()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "-d,input.txt",
            "input.txt,-d"
    })
    void canParseFileArgumentAndDebug(String first, String second) {
        var cmd = CommandLine.fromArgs(first, second);

        assertThat(cmd.isShouldShowUsage()).isFalse();
        assertThat(cmd.getFileArgument()).hasName("input.txt");
        assertThat(cmd.isDebug()).isTrue();
    }

    @Test
    void invalidSwitchShowsHelp() {
        var cmd = CommandLine.fromArgs("--unsupported");

        assertThat(cmd.isShouldShowUsage()).isTrue();
        assertThat(cmd.getFileArgument()).isNull();
        assertThat(cmd.isDebug()).isFalse();
    }

    @Test
    void multipleFilesShowsHelp() {
        var cmd = CommandLine.fromArgs("file1.txt", "file2.txt");

        assertThat(cmd.isShouldShowUsage()).isTrue();
        assertThat(cmd.getFileArgument()).isNull();
        assertThat(cmd.isDebug()).isFalse();
    }
}
