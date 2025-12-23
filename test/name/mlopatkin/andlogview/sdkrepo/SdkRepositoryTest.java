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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.MoreFiles;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

class SdkRepositoryTest {
    private static final HashFunction HASH = Hashing.sha256();
    private static final URI TEST_URI = URI.create("https://example.com/src.zip");
    public static final String ARCHIVED_CONTENT = "some data";

    @TempDir
    Path sourceDataDir;

    @TempDir
    Path targetDir;

    final HttpClient httpClient = mock();
    final HttpResource response = mock();

    @BeforeEach
    void setUp() {
        when(httpClient.get(TEST_URI)).thenReturn(response);
    }

    @Test
    void canDownloadTestZip() throws IOException {
        var repo = createRepo();
        repo.downloadPackage(createTestPackage(), targetDir.toFile(), SdkRepository.InstallMode.OVERWRITE);

        try (var files = Files.list(targetDir)) {
            assertThat(paths(files)).containsExactly("file.txt");
        }
    }

    @Test
    void failIfNotEmptyThrowsWhenDirectoryHasFiles() throws IOException {
        // Create a non-empty directory
        Files.createDirectories(targetDir);
        Files.write(targetDir.resolve("existing.txt"), "existing content".getBytes(StandardCharsets.UTF_8));

        var repo = createRepo();
        assertThatThrownBy(() ->
                repo.downloadPackage(createTestPackage(), targetDir.toFile(),
                        SdkRepository.InstallMode.FAIL_IF_NOT_EMPTY)
        ).isInstanceOf(TargetDirectoryNotEmptyException.class)
                .satisfies(
                        e -> assertThat(((TargetDirectoryNotEmptyException) e).getDirectory())
                                .isEqualTo(targetDir.toFile()));
    }

    @Test
    void failIfNotEmptySucceedsWhenDirectoryIsEmpty() throws IOException {
        var emptyDir = targetDir.resolve("empty");
        Files.createDirectories(emptyDir);

        var repo = createRepo();
        repo.downloadPackage(createTestPackage(), emptyDir.toFile(), SdkRepository.InstallMode.FAIL_IF_NOT_EMPTY);

        try (var files = Files.list(emptyDir)) {
            assertThat(paths(files)).containsExactly("file.txt");
        }
    }

    @Test
    void failIfNotEmptySucceedsWhenDirectoryDoesNotExist() throws IOException {
        var nonExistingDir = targetDir.resolve("non-existing");
        var repo = createRepo();
        repo.downloadPackage(createTestPackage(), nonExistingDir.toFile(), SdkRepository.InstallMode.FAIL_IF_NOT_EMPTY);

        try (var files = Files.list(nonExistingDir)) {
            assertThat(paths(files)).containsExactly("file.txt");
        }
    }

    @Test
    void overwriteModeSucceedsWhenDirectoryIsNonEmpty() throws IOException {
        // Create a non-empty directory
        Files.createDirectories(targetDir);
        Files.write(targetDir.resolve("existing.txt"), "existing content".getBytes(StandardCharsets.UTF_8));

        var repo = createRepo();
        repo.downloadPackage(createTestPackage(), targetDir.toFile(), SdkRepository.InstallMode.OVERWRITE);

        try (var files = Files.list(targetDir)) {
            assertThat(paths(files)).containsExactlyInAnyOrder("file.txt", "existing.txt");
        }
    }

    @Test
    void overwriteModeOverwritesExistingFiles() throws IOException {
        // Create a non-empty directory
        Files.createDirectories(targetDir);
        Files.write(targetDir.resolve("file.txt"), "existing content".getBytes(StandardCharsets.UTF_8));

        var repo = createRepo();
        repo.downloadPackage(createTestPackage(), targetDir.toFile(), SdkRepository.InstallMode.OVERWRITE);

        try (var files = Files.list(targetDir)) {
            assertThat(paths(files)).containsExactly("file.txt");
        }

        assertThat(targetDir.resolve("file.txt")).content(StandardCharsets.UTF_8).isEqualTo(ARCHIVED_CONTENT);
    }

    @ParameterizedTest
    @EnumSource(SdkRepository.InstallMode.class)
    void usingExistingFileAsTargetFails(SdkRepository.InstallMode mode) throws IOException {
        // Create a non-empty directory
        Files.createDirectories(targetDir);
        var destination = Files.createFile(targetDir.resolve("destination"));

        var repo = createRepo();
        assertThatThrownBy(
                () -> repo.downloadPackage(createTestPackage(), destination.toFile(), mode)
        ).isInstanceOf(SdkException.class)
                .hasMessageContaining("A file with that name already exists.");
    }

    private SdkRepository createRepo() {
        return new SdkRepository(httpClient);
    }

    private SdkPackage createTestPackage() throws IOException {
        var testZip = createTestZip();
        var testPackage = createTestPackage(testZip);

        when(response.download(anyLong())).thenReturn(MoreFiles.asByteSource(testZip));
        doAnswer(invocation -> {
            MoreFiles.asByteSource(testZip).copyTo(invocation.<OutputStream>getArgument(0));
            return null;
        }).when(response).downloadInto(any());

        return testPackage;
    }

    private Path createTestZip() throws IOException {
        var zipPath = sourceDataDir.resolve("src.zip");
        try (ZipArchiveOutputStream zipOut =
                new ZipArchiveOutputStream(Files.newOutputStream(zipPath))) {
            ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
            zipOut.putArchiveEntry(entry);
            ByteSource.wrap(ARCHIVED_CONTENT.getBytes(StandardCharsets.UTF_8)).copyTo(zipOut);
            zipOut.closeArchiveEntry();
        }
        return zipPath;
    }

    private SdkPackage createTestPackage(Path zipSource) throws IOException {
        var zipHash = MoreFiles.asByteSource(zipSource).hash(HASH);

        return new SdkPackage(
                "package-name",
                TestSdkPackage.TEST_LICENSE,
                TEST_URI,
                SdkRepository.getHostOs(),
                HASH,
                zipHash
        );
    }

    private static Stream<String> paths(Stream<Path> files) {
        return files
                .map(Path::getFileName)
                .map(Path::toString);
    }
}
