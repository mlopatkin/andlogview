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

import static name.mlopatkin.andlogview.base.collections.MyIterables.forEnumeration;

import name.mlopatkin.andlogview.thirdparty.systemutils.SystemUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.HashingOutputStream;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;

import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

/**
 * The main entry point into downloading the repository components. It connects to the web service, downloads the XML
 * content definition, and exposes this information.
 */
public class SdkRepository {
    private static final URI REPOSITORY_URI = URI.create("https://dl.google.com/android/repository/");
    private static final URI MANIFEST_URI = REPOSITORY_URI.resolve("repository2-3.xml");
    private static final long MAX_REPOSITORY_MANIFEST_SIZE = 5 * 1024 * 1024; // 5 Mb

    private final HttpClient httpClient;

    @Inject
    SdkRepository(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Finds a package with the given name and returns its metadata if available.
     *
     * @param packageName the name of the package, e.g. {@code platform-tools}.
     * @return optional with SdkPackage data or an empty optional if the package isn't available
     * @throws IOException if fetching the document fails
     */
    public Optional<SdkPackage> locatePackage(String packageName) throws IOException, ManifestParseException {
        ByteSource repositoryXml = httpClient.get(MANIFEST_URI).download(MAX_REPOSITORY_MANIFEST_SIZE);

        try (var parser = new SdkRepoManifestParser(REPOSITORY_URI, repositoryXml)) {
            return parser.read(packageName).stream().filter(p -> p.getTargetOs().equals(getHostOs())).findFirst();
        }
    }

    private SdkPackage.TargetOs getHostOs() {
        if (SystemUtils.IS_OS_MACOS) {
            return SdkPackage.TargetOs.MAC_OS;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return SdkPackage.TargetOs.WINDOWS;
        }
        return SdkPackage.TargetOs.LINUX;
    }

    public void downloadPackage(SdkPackage sdkPackage, File targetDirectory) throws IOException {
        Files.createDirectories(targetDirectory.toPath());

        var tempFile = File.createTempFile("adb", ".download", targetDirectory);
        tempFile.deleteOnExit();
        try (
                var tempFileOutput = Files.newOutputStream(tempFile.toPath());
                var hashingStream = new HashingOutputStream(sdkPackage.getChecksumAlgorithm(), tempFileOutput)
        ) {
            httpClient.get(sdkPackage.getDownloadUrl()).downloadInto(hashingStream);
            hashingStream.flush();

            if (!Objects.equals(hashingStream.hash(), sdkPackage.getPackageChecksum())) {
                throw new IOException("Checksum mismatch");
            }

            extractZip(tempFile, targetDirectory);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    public void extractZip(File zipFile, File targetDirectory) throws IOException {
        var targetPath = targetDirectory.toPath();
        var archiveSize = Files.size(zipFile.toPath());
        var maxExpectedFileSize = archiveSize * 100;

        try (
                var zip = ZipFile.builder().setFile(zipFile).get()
        ) {
            for (var entry : forEnumeration(zip::getEntries)) {
                var extractPath = Paths.get(entry.getName()).normalize();
                var targetEntryPath = targetPath.resolve(extractPath);
                if (!targetEntryPath.startsWith(targetPath)) {
                    throw new IOException(
                            String.format(
                                    "Path traversal detected in '%s', writing file '%s' outside the target directory",
                                    zipFile,
                                    extractPath)
                    );
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(targetEntryPath);
                    continue;
                } else {
                    Files.createDirectories(targetEntryPath.getParent());
                }

                try (var zipEntryStream = zip.getInputStream(entry)) {
                    Files.copy(ByteStreams.limit(zipEntryStream, maxExpectedFileSize), targetEntryPath);
                    if (zipEntryStream.read() != -1) {
                        throw new IOException(
                                "The compression ratio for the file is over 99%. This is likely a zip bomb");
                    }
                    if (entry.getUnixMode() != 0) {
                        try {
                            Files.setPosixFilePermissions(targetEntryPath, modeToPermissions(entry.getUnixMode()));
                        } catch (UnsupportedOperationException ignored) {
                            // ok to ignore on Windows
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("OctalInteger")
    private static Set<PosixFilePermission> modeToPermissions(int unixMode) {
        var permissions = ImmutableSet.<PosixFilePermission>builder();
        // Owner permissions
        if ((unixMode & 0400) != 0) {
            permissions.add(PosixFilePermission.OWNER_READ);
        }
        if ((unixMode & 0200) != 0) {
            permissions.add(PosixFilePermission.OWNER_WRITE);
        }
        if ((unixMode & 0100) != 0) {
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }

        // Group permissions
        if ((unixMode & 0040) != 0) {
            permissions.add(PosixFilePermission.GROUP_READ);
        }
        if ((unixMode & 0020) != 0) {
            permissions.add(PosixFilePermission.GROUP_WRITE);
        }
        if ((unixMode & 0010) != 0) {
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
        }

        // Others permissions
        if ((unixMode & 0004) != 0) {
            permissions.add(PosixFilePermission.OTHERS_READ);
        }
        if ((unixMode & 0002) != 0) {
            permissions.add(PosixFilePermission.OTHERS_WRITE);
        }
        if ((unixMode & 0001) != 0) {
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        }
        return permissions.build();
    }
}
