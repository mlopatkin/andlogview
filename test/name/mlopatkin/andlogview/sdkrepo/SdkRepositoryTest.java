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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

class SdkRepositoryTest {
    private static final HashFunction HASH = Hashing.sha256();
    private static final URI TEST_URI = URI.create("https://example.com/src.zip");

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
        repo.downloadPackage(createTestPackage(), targetDir.toFile());

        try (var files = Files.list(targetDir)) {
            assertThat(files
                    .map(Path::getFileName)
                    .map(Path::toString)
            ).containsExactly("file.txt");
        }
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
            ByteSource.wrap("some data".getBytes(StandardCharsets.UTF_8)).copyTo(zipOut);
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
}
