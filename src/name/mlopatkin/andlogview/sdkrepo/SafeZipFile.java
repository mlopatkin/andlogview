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

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;

import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * Allows to safely extract a ZIP file. Prevents popular issues, like path traversals and zip bombs.
 */
class SafeZipFile {
    private static final int MAX_COMPRESSION_RATIO = 100;

    private final Path zipFile;

    public SafeZipFile(Path zipFile) {
        this.zipFile = zipFile;
    }

    /**
     * Extracts this ZIP file into the target directory, creating it if necessary.
     *
     * @param targetDirectory the target directory to extract files into
     * @throws IOException if extraction fails
     */
    public void extractTo(Path targetDirectory) throws IOException {
        var archiveSize = Files.size(zipFile);
        var maxExpectedFileSize = archiveSize * MAX_COMPRESSION_RATIO;

        try (var zip = openZip()) {
            Files.createDirectories(targetDirectory);

            var filePermissionsSupported =
                    Files.getFileStore(targetDirectory).supportsFileAttributeView(PosixFileAttributeView.class);

            for (var entry : forEnumeration(zip::getEntries)) {
                var extractPath = Paths.get(entry.getName()).normalize();
                var targetEntryPath = targetDirectory.resolve(extractPath);
                if (!targetEntryPath.startsWith(targetDirectory)) {
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
                }
                // Zip files do not have to contain directory entries or to contain them before the files.
                Files.createDirectories(targetEntryPath.getParent());

                try (var zipEntryStream = zip.getInputStream(entry)) {
                    Files.copy(ByteStreams.limit(zipEntryStream, maxExpectedFileSize), targetEntryPath);
                    if (zipEntryStream.read() != -1) {
                        // We have read about MAX_COMPRESSION_RATIO times the size of the archive, but the compressed
                        // file is still going.
                        throw new IOException(
                                "The compression ratio for the file `" + extractPath + "` is over 99%. "
                                + "This is likely a zip bomb");
                    }
                    if (filePermissionsSupported && entry.getUnixMode() != 0) {
                        Files.setPosixFilePermissions(targetEntryPath, modeToPermissions(entry.getUnixMode()));
                    }
                }
            }
        }
    }

    private ZipFile openZip() throws IOException {
        return ZipFile.builder().setFile(zipFile.toFile()).get();
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
