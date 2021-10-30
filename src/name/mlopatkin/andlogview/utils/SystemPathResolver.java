/*
 * Copyright 2021 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.utils;

import static com.google.common.io.Files.getFileExtension;

import static java.util.stream.Collectors.toList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This class helps to resolve executable name similar to how the OS does it. The resolution process is typically based
 * on the list of directories set in the {@code PATH} environment variable.
 */
public abstract class SystemPathResolver {
    private SystemPathResolver() {}

    /**
     * Resolves a raw file path or executable name to the file location according to the rules of the current OS. The
     * returned Optional will be empty if the file is not found in any of the searchable directories - even if the given
     * path is an absolute path.
     * <p>
     * The returned path is absolute and normalized.
     *
     * @param rawPath the filename or path that can be resolved to the executable file according to the OS' rules
     * @return the Optional with the resolved path to the executable or empty Optional if the executable wasn't found.
     */
    public static Optional<File> resolveExecutablePath(String rawPath) {
        return getPathResolver().resolveExecutablePathImpl(rawPath);
    }

    abstract Optional<File> resolveExecutablePathImpl(String rawPath);

    private static SystemPathResolver getPathResolver() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return new WindowsPathResolver();
        }
        return new PosixPathResolver();
    }

    @VisibleForTesting
    static class PosixPathResolver extends SystemPathResolver {
        private static final char PATH_SEPARATOR = ':';
        private final List<String> pathVarElements;

        public PosixPathResolver() {
            this(System.getenv("PATH"));
        }

        @VisibleForTesting
        PosixPathResolver(String pathVar) {
            this.pathVarElements = pathSplitter(PATH_SEPARATOR).splitToList(pathVar);
        }

        @Override
        Optional<File> resolveExecutablePathImpl(String rawPath) {
            // TODO(mlopatkin): Make this proper path resolver.
            return Optional.of(new File(rawPath));
        }
    }

    /**
     * Windows-specific executable path resolution code as specified in
     * <a href="https://docs.microsoft.com/en-us/previous-versions//cc723564(v=technet.10)#command-search-sequence">MSDN</a>.
     */
    static class WindowsPathResolver extends SystemPathResolver {
        private static final char PATH_SEPARATOR = ';';
        private final List<Path> pathVarElements;
        private final List<String> pathExts;

        private WindowsPathResolver(Path curDir, String pathVar, String pathExtVar) {
            Splitter splitter = pathSplitter(PATH_SEPARATOR);
            // Windows always start searching the executable in the current directory, so we prepend it to the PATH
            // contents.
            pathVarElements = Stream.concat(
                    Stream.of(curDir),
                    splitter.splitToStream(pathVar).map(Paths::get)
            ).collect(toList());

            // PATHEXT can (in theory) have an empty extension. It is useless though.
            pathExts = Splitter.on(PATH_SEPARATOR).splitToList(pathExtVar);
        }

        public WindowsPathResolver() {
            this(Paths.get(System.getProperty("user.dir")), System.getenv("PATH"), System.getenv("PATHEXT"));
        }

        @VisibleForTesting
        WindowsPathResolver(File curDir, String pathVar, String pathExtVar) {
            this(curDir.toPath(), pathVar, pathExtVar);
        }

        @Override
        Optional<File> resolveExecutablePathImpl(String rawExecutable) {
            Path rawExecutablePath = Paths.get(rawExecutable).normalize();
            boolean hasExtension = !getFileExtension(rawExecutablePath.getFileName().toString()).isEmpty();
            if (!hasJustFilename(rawExecutablePath)) {
                if (hasExtension) {
                    return getExecutablePathIfExists(rawExecutablePath);
                }
                return resolveWithPathExt(rawExecutablePath.getParent(), rawExecutablePath.getFileName().toString());
            }
            // If the given filename has an extension then it doesn't participate in the PATHEXT lookup.
            if (hasExtension) {
                for (Path pathVarElement : pathVarElements) {
                    Optional<File> potentialResult = getExecutablePathIfExists(pathVarElement.resolve(rawExecutable));
                    if (potentialResult.isPresent()) {
                        return potentialResult;
                    }
                }
            } else {
                for (Path pathElement : pathVarElements) {
                    Optional<File> result = resolveWithPathExt(pathElement, rawExecutable);
                    if (result.isPresent()) {
                        return result;
                    }
                }
            }
            return Optional.empty();
        }

        private Optional<File> resolveWithPathExt(Path baseDir, String rawExecutable) {
            for (String pathExt : pathExts) {
                Path potentialExecutablePath = baseDir.resolve(rawExecutable + pathExt);
                Optional<File> potentialResult = getExecutablePathIfExists(potentialExecutablePath);
                if (potentialResult.isPresent()) {
                    return potentialResult;
                }
            }
            return Optional.empty();
        }

        private static boolean hasJustFilename(Path rawExecutableFile) {
            return rawExecutableFile.getNameCount() == 1;
        }
    }

    private static Optional<File> getExecutablePathIfExists(Path potentialExecutablePath) {
        if (isExecutableValid(potentialExecutablePath)) {
            // We don't want to hit the disk there but dropping extra stuff and ensuring absolute path is nice to the
            // clients.
            return Optional.of(potentialExecutablePath.normalize().toAbsolutePath().toFile());
        }
        return Optional.empty();
    }

    private static Splitter pathSplitter(char pathSeparator) {
        return Splitter.on(pathSeparator).omitEmptyStrings();
    }

    private static boolean isExecutableValid(Path potentialExecutable) {
        return Files.isExecutable(potentialExecutable);
    }
}
