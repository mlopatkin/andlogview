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

import name.mlopatkin.andlogview.thirdparty.systemutils.SystemUtils;

import com.google.common.hash.HashingOutputStream;
import com.google.common.io.ByteSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.net.ssl.SSLException;

/**
 * The main entry point into downloading the repository components. It connects to the web service, downloads the XML
 * content definition, and exposes this information.
 */
public class SdkRepository {
    private static final Logger logger = LoggerFactory.getLogger(SdkRepository.class);

    /**
     * Installation mode for {@link #downloadPackage(SdkPackage, File, InstallMode)}.
     */
    public enum InstallMode {
        /**
         * Check if the target directory is empty before proceeding. Throws {@link TargetDirectoryNotEmptyException}
         * if the directory exists and contains files.
         */
        FAIL_IF_NOT_EMPTY,

        /**
         * Proceed with installation even if the target directory is not empty. Existing files may be overwritten.
         */
        OVERWRITE
    }

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

    public static SdkPackage.TargetOs getHostOs() {
        if (SystemUtils.IS_OS_MACOS) {
            return SdkPackage.TargetOs.MAC_OS;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return SdkPackage.TargetOs.WINDOWS;
        }
        return SdkPackage.TargetOs.LINUX;
    }

    public void downloadPackage(SdkPackage sdkPackage, File targetDirectory, InstallMode mode) throws SdkException {
        if (mode == InstallMode.FAIL_IF_NOT_EMPTY && targetDirectory.isDirectory()) {
            String[] contents = targetDirectory.list();
            if (contents != null && contents.length > 0) {
                throw new TargetDirectoryNotEmptyException(targetDirectory);
            }
        }

        File tempFile = createTempDownloadTarget(targetDirectory);

        try {
            downloadWithChecksumVerification(sdkPackage, tempFile);
            extractZip(tempFile, targetDirectory, mode == InstallMode.OVERWRITE);
        } finally {
            deleteSilently(tempFile);
        }
    }

    private void downloadWithChecksumVerification(SdkPackage sdkPackage, File destination) throws SdkException {
        try (
                var tempFileOutput = ExceptionTranslatingOutputStream.open(destination);
                var hashingStream = new HashingOutputStream(sdkPackage.getChecksumAlgorithm(), tempFileOutput)
        ) {
            downloadPackageInto(sdkPackage, hashingStream);
            hashingStream.flush();

            var downloadedChecksum = hashingStream.hash();
            var expectedChecksum = sdkPackage.getPackageChecksum();
            if (!Objects.equals(downloadedChecksum, expectedChecksum)) {
                // Don't show the actual checksums to the user in the primary message.
                throw SdkException.rethrow(
                        new IOException(String.format(
                                "Checksum mismatch: expected `%s`, got `%s`", expectedChecksum, downloadedChecksum
                        )),
                        "The checksum of the downloaded package doesn't match. "
                        + "The file may have been corrupted while downloading."
                );
            }
        } catch (IOException ex) {
            throw SdkException.rethrow(
                    ex,
                    "Unexpected failure when downloading package %s. Consider reporting this issue. "
                    + "See details for more information.",
                    sdkPackage.getName()
            );
        }
    }

    private void downloadPackageInto(SdkPackage sdkPackage, HashingOutputStream hashingStream) throws SdkException {
        try {
            httpClient.get(sdkPackage.getDownloadUrl()).downloadInto(hashingStream);
        } catch (UnknownHostException e) {
            throw SdkException.rethrow(e, "Cannot resolve host `%s`.", sdkPackage.getDownloadUrl().getHost());
        } catch (SocketTimeoutException e) {
            throw SdkException.rethrow(
                    e,
                    "Timed out while downloading package `%s`. A firewall may be blocking connections to `%s`.",
                    sdkPackage.getName(),
                    sdkPackage.getDownloadUrl().getHost()
            );
        } catch (SocketException e) {
            throw SdkException.rethrow(e, "Cannot connect to host `%s`.", sdkPackage.getDownloadUrl().getHost());
        } catch (SSLException e) {
            throw SdkException.rethrow(
                    e,
                    "Secure connection failure when connecting to `%s`. "
                    + "Your connection may be intercepted or Java SSL configuration may be invalid.",
                    sdkPackage.getDownloadUrl().getHost()
            );
        } catch (IOException e) {
            throw SdkException.rethrow(e, "Failed to download package `%s`. See details for more information.",
                    sdkPackage.getName());
        }
    }

    private File createTempDownloadTarget(File targetDirectory) throws SdkException {
        try {
            Files.createDirectories(targetDirectory.toPath());
        } catch (FileAlreadyExistsException ex) {
            throw SdkException.rethrow(
                    ex, "Cannot create directory `%s`. A file with that name already exists.", targetDirectory
            );
        } catch (AccessDeniedException ex) {
            throw SdkException.rethrow(
                    ex, "Insufficient permissions to create directory `%s`.", targetDirectory
            );
        } catch (IOException ex) {
            throw SdkException.rethrow(ex, "Cannot create directory `%s`.", targetDirectory);
        }

        try {
            var tempFile = File.createTempFile("adb", ".download", targetDirectory);
            tempFile.deleteOnExit();
            return tempFile;
        } catch (IOException ex) {
            throw SdkException.rethrow(
                    ex, "Cannot save the package. Is the target directory `%s` writable?", targetDirectory
            );
        }
    }

    private void deleteSilently(File toDelete) {
        try {
            Files.deleteIfExists(toDelete.toPath());
        } catch (IOException e) {
            logger.error("Failed to delete temp file at `{}`", toDelete.getAbsolutePath(), e);
        }
    }

    private void extractZip(File zipFile, File targetDirectory, boolean overwriteExisting) throws SdkException {
        try {
            new SafeZipFile(zipFile.toPath()).extractTo(targetDirectory.toPath(), overwriteExisting);
        } catch (IOException ex) {
            throw SdkException.rethrow(
                    ex, "Failed to extract downloaded package zip at `%s` into `%s`.", zipFile, targetDirectory
            );
        }
    }
}
