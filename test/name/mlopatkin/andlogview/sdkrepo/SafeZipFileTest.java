/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.sdkrepo;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import name.mlopatkin.andlogview.test.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class SafeZipFileTest {
    @Test
    void canDetectZipBombs(@Resource("zip-bomb.zip") Path zipBomb, @TempDir Path outputDirectory) throws Exception {
        var zipFile = new SafeZipFile(zipBomb);

        assertThatThrownBy(() -> zipFile.extractTo(outputDirectory)).hasMessageContaining("This is likely a zip bomb");
    }
}
