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

package name.mlopatkin.andlogview.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceParameterTest {

    @Test
    void canResolveRelativePaths(@Resource("RELATIVE") Path relativeResource) throws Exception {
        assertThat(readString(relativeResource)).contains("RELATIVE");
    }

    @Test
    void canResolveRelativePathsAsFiles(@Resource("RELATIVE") File relativeResource) throws Exception {
        assertThat(readString(relativeResource.toPath())).contains("RELATIVE");
    }

    @Test
    void canResolveRelativePathsInSubpackage(@Resource("sub/SUB") Path relativeResource) throws Exception {
        assertThat(readString(relativeResource)).contains("SUB");
    }

    @Test
    void canResolveAbsolutePaths(@Resource("/name/mlopatkin/ABSOLUTE") Path absoluteResource) throws Exception {
        assertThat(readString(absoluteResource)).contains("ABSOLUTE");
    }

    @Test
    void canResolveAbsolutePathsAsFiles(@Resource("/name/mlopatkin/ABSOLUTE") File absoluteResource) throws Exception {
        assertThat(readString(absoluteResource.toPath())).contains("ABSOLUTE");
    }

    @SuppressWarnings("ReadWriteStringCanBeUsed")
    private static String readString(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
