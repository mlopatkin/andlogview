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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import name.mlopatkin.andlogview.test.Resource;

import com.google.common.base.Strings;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

class SafeZipFileTest {
    @Test
    void canDetectZipBombs(@Resource("zip-bomb.zip") Path zipBomb, @TempDir Path outputDirectory) throws Exception {
        var zipFile = new SafeZipFile(zipBomb);

        assertThatThrownBy(() -> zipFile.extractTo(outputDirectory, false))
                .hasMessageContaining("This is likely a zip bomb");
    }

    @Test
    void extractionFailsWhenOutOfDiskSpace(@TempDir Path tempDir) throws Exception {
        // Create a zip file with data that will exceed the Jimfs space limit
        Path zipPath = ZipFileBuilder.zipFile(tempDir.resolve("test.zip"), zip -> {
            var payload = Strings.repeat("0123456789", 200); // ~2KB per file
            // 100 files, total ~200KB extracted data.
            for (int i = 0; i < 100; ++i) {
                zip.file("entry " + i + ".txt", payload);
            }
        });

        // Create an in-memory file system with limited space (50KB)
        Configuration config = Configuration.unix()
                .toBuilder()
                .setMaxSize(50 * 1024) // 50 KB limit
                .build();

        try (FileSystem fs = Jimfs.newFileSystem(config)) {
            Path targetDir = fs.getPath("/extract");
            SafeZipFile safeZipFile = new SafeZipFile(zipPath);

            // Extraction should fail when running out of space
            assertThatThrownBy(() -> safeZipFile.extractTo(targetDir, false))
                    .isInstanceOf(IOException.class);
        }
    }

    @Test
    void canDetectPathTraversalWithParentReferences(@TempDir Path tempDir) throws Exception {
        Path zipPath = ZipFileBuilder.zipFile(
                tempDir.resolve("traversal.zip"),
                zip -> zip.file("../../etc/passwd", "malicious content")
        );

        Path outputDirectory = tempDir.resolve("output");
        var zipFile = new SafeZipFile(zipPath);

        assertThatThrownBy(() -> zipFile.extractTo(outputDirectory, false))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Path traversal detected");
    }

    @Test
    void canDetectPathTraversalWithSingleParentReference(@TempDir Path tempDir) throws Exception {
        Path zipPath = ZipFileBuilder.zipFile(
                tempDir.resolve("traversal.zip"),
                zip -> zip.file("../outside.txt", "malicious content")
        );

        Path outputDirectory = tempDir.resolve("output");
        var zipFile = new SafeZipFile(zipPath);

        assertThatThrownBy(() -> zipFile.extractTo(outputDirectory, false))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Path traversal detected");
    }

    @Test
    void canDetectPathTraversalWithMixedSegments(@TempDir Path tempDir) throws Exception {
        Path zipPath = ZipFileBuilder.zipFile(
                tempDir.resolve("traversal.zip"),
                zip -> zip.file("subdir/../../outside.txt", "malicious content")
        );

        Path outputDirectory = tempDir.resolve("output");
        var zipFile = new SafeZipFile(zipPath);

        assertThatThrownBy(() -> zipFile.extractTo(outputDirectory, false))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Path traversal detected");
    }

    @Test
    void extractionFailsWhenFileExistsAndOverwriteDisabled(@TempDir Path tempDir) throws Exception {
        Path zipPath = ZipFileBuilder.zipFile(tempDir.resolve("test.zip"), zip -> zip.file("file.txt", "new content"));

        Path outputDirectory = tempDir.resolve("output");
        Files.createDirectories(outputDirectory);
        Files.write(outputDirectory.resolve("file.txt"), "existing content".getBytes(StandardCharsets.UTF_8));

        var zipFile = new SafeZipFile(zipPath);

        assertThatThrownBy(() -> zipFile.extractTo(outputDirectory, false))
                .isInstanceOf(IOException.class);
    }

    @Test
    void extractionOverwritesExistingFilesWhenOverwriteEnabled(@TempDir Path tempDir) throws Exception {
        Path zipPath = ZipFileBuilder.zipFile(tempDir.resolve("test.zip"), zip -> zip.file("file.txt", "new content"));

        Path outputDirectory = tempDir.resolve("output");
        Files.createDirectories(outputDirectory);
        Files.write(outputDirectory.resolve("file.txt"), "existing content".getBytes(StandardCharsets.UTF_8));

        var zipFile = new SafeZipFile(zipPath);
        zipFile.extractTo(outputDirectory, true);

        assertThat(outputDirectory.resolve("file.txt"))
                .content(StandardCharsets.UTF_8)
                .isEqualTo("new content");
    }

    @Test
    void extractionWithOverwritePreservesOtherFiles(@TempDir Path tempDir) throws Exception {
        Path zipPath = ZipFileBuilder.zipFile(tempDir.resolve("test.zip"), zip -> zip.file("file.txt", "new content"));

        Path outputDirectory = tempDir.resolve("output");
        Files.createDirectories(outputDirectory);
        Files.write(outputDirectory.resolve("file.txt"), "existing content".getBytes(StandardCharsets.UTF_8));
        Files.write(outputDirectory.resolve("other.txt"), "other content".getBytes(StandardCharsets.UTF_8));

        var zipFile = new SafeZipFile(zipPath);
        zipFile.extractTo(outputDirectory, true);

        assertThat(outputDirectory.resolve("file.txt"))
                .content(StandardCharsets.UTF_8)
                .isEqualTo("new content");
        assertThat(outputDirectory.resolve("other.txt"))
                .content(StandardCharsets.UTF_8)
                .isEqualTo("other content");
    }
}
