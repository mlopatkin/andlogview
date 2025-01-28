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

package name.mlopatkin.andlogview.ui.preferences;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Goes through ADB install flow. It is planned to have several steps:
 * <ol>
 *     <li>Download the Android SDK package list</li>
 *     <li>Ask the user to accept the SDK license and select the destination directory</li>
 *     <li>Download and extract the package</li>
 *     <li>Configure AndLogView to use the extracted ADB</li>
 * </ol>
 * <p>
 * Alternatively, when these niceties aren't available, just open the browser for the user to download the Platform
 * tools.
 */
public interface InstallAdbPresenter {
    /**
     * Checks if the web install service is available. For example, it may not be available if the JVM doesn't allow
     * opening the browser.
     *
     * @return {@code true} if installing the ADB can be attempted
     */
    boolean isAvailable();

    /**
     * Starts the ADB installation flow.
     *
     * @return the promise of installation result.
     * @throws IllegalStateException if the ADB install is not available
     */
    CompletableFuture<Result> startInstall();


    /**
     * A result of the installation action. A poor man's sealed class. Use static factory methods to obtains a concrete
     * instance of this class.
     */
    abstract class Result {
        private Result() {}

        /**
         * The download was abandoned because of the network or file failure.
         *
         * @param failure the failure
         * @return the {@link DownloadFailure} instance
         */
        public static DownloadFailure failure(Throwable failure) {
            return new DownloadFailure(failure);
        }

        /**
         * The package wasn't found in the remote repository. This is likely because the XML structure has been changed.
         *
         * @return the {@link PackageNotFound} instance
         */
        public static PackageNotFound notFound() {
            return PackageNotFound.INSTANCE;
        }

        /**
         * The package was successfully installed.
         *
         * @param adbPath the path to the installed ADB executable
         * @return the {@link Installed} instance pointing to the executable
         */
        public static Installed installed(File adbPath) {
            return new Installed(adbPath);
        }

        /**
         * The user was sent to download the package manually.
         *
         * @return the {@link ManualFallback} instance
         */
        public static ManualFallback manual() {
            return ManualFallback.INSTANCE;
        }

        /**
         * The user has cancelled the download.
         *
         * @return the {@link Cancelled} instance
         */
        public static Cancelled cancelled() {
            return Cancelled.INSTANCE;
        }
    }

    /**
     * The download was abandoned because of the network or file failure.
     */
    final class DownloadFailure extends Result {
        private final Throwable failure;

        private DownloadFailure(Throwable failure) {
            this.failure = failure;
        }

        public Throwable getFailure() {
            return failure;
        }
    }

    /**
     * The package wasn't found in the remote repository.
     */
    final class PackageNotFound extends Result {
        private static final PackageNotFound INSTANCE = new PackageNotFound();

        private PackageNotFound() {}
    }

    /**
     * The package was successfully installed.
     */
    final class Installed extends Result {
        private final File adbPath;

        private Installed(File adbPath) {
            this.adbPath = adbPath;
        }

        public File getAdbPath() {
            return adbPath;
        }
    }

    /**
     * The user was sent to download the package manually.
     */
    final class ManualFallback extends Result {
        private static final ManualFallback INSTANCE = new ManualFallback();

        private ManualFallback() {}
    }

    /**
     * The user has cancelled the download.
     */
    final class Cancelled extends Result {
        private static final Cancelled INSTANCE = new Cancelled();

        private Cancelled() {}
    }
}
