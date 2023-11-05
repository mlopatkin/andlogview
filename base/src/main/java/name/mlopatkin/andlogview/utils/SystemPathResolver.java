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

import name.mlopatkin.andlogview.thirdparty.systemutils.SystemUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This class helps to resolve executable name similar to how the OS does it. The resolution process is typically based
 * on the list of directories set in the {@code PATH} environment variable.
 */
public abstract class SystemPathResolver {
    @VisibleForTesting
    public SystemPathResolver() {}

    /**
     * Resolves a raw file path or executable name to the file location according to the rules of the current OS. The
     * returned Optional will be empty if the file is not found in any of the searchable directories - even if the given
     * path is an absolute path.
     * <p>
     * The returned path is absolute and normalized.
     *
     * @param rawPath the filename or path that can be resolved to the executable file according to the OS'
     *         rules
     * @return the Optional with the resolved path to the executable or empty Optional if the executable wasn't found.
     */
    public abstract Optional<File> resolveExecutablePath(String rawPath);

    public static SystemPathResolver getPathResolver() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return new WindowsPathResolver();
        }
        return new PosixPathResolver();
    }

    /**
     * POSIX-specific executable path resolution as specified in
     * <a href="https://pubs.opengroup.org/onlinepubs/009696899/basedefs/xbd_chap08.html#tag_08_03">IEEE Std 1003.1</a>
     */
    @VisibleForTesting
    static class PosixPathResolver extends SystemPathResolver {
        private static final char PATH_SEPARATOR = ':';
        private final List<Path> pathVarElements;
        private final Path curDir;

        public PosixPathResolver() {
            this(Paths.get(System.getProperty("user.dir")), System.getenv("PATH"));
        }

        @VisibleForTesting
        PosixPathResolver(Path curDir, String pathVar) {
            this.curDir = curDir;
            this.pathVarElements = Splitter.on(PATH_SEPARATOR).splitToStream(pathVar).map(pathElem -> {
                // Empty strings have meaning in POSIX - they represent a current directory.
                if (pathElem.isEmpty()) {
                    return curDir;
                }
                return Paths.get(pathElem);
            }).collect(toList());
        }

        @Override
        public Optional<File> resolveExecutablePath(String rawExecutable) {
            Path rawExecutablePath = Paths.get(rawExecutable);
            if (!hasJustFilename(rawExecutablePath)) {
                // Relative paths in Java are always resolved within the current directory at the time JVM was started.
                // This quirk breaks test isolation, so an explicit resolve withing `curDir` is added there.
                return getExecutablePathIfExists(curDir.resolve(rawExecutablePath));
            }
            for (Path pathVarElement : pathVarElements) {
                Optional<File> result = getExecutablePathIfExists(pathVarElement.resolve(rawExecutablePath));
                if (result.isPresent()) {
                    return result;
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Windows-specific executable path resolution code as specified in
     * <a href="https://docs.microsoft.com/en-us/previous-versions//cc723564(v=technet.10)#command-search-sequence">MSDN</a>.
     */
    @VisibleForTesting
    static class WindowsPathResolver extends SystemPathResolver {
        private static final char PATH_SEPARATOR = ';';
        private final List<Path> pathVarElements;
        private final List<String> pathExts;

        private WindowsPathResolver(Path curDir, String pathVar, String pathExtVar) {
            // Windows always start searching the executable in the current directory, so we prepend it to the PATH
            // contents.
            pathVarElements = Stream.concat(
                    Stream.of(curDir),
                    splitPathVariable(pathVar)
            ).collect(toList());

            // PATHEXT can (in theory) have an empty extension. It is useless though.
            pathExts = Splitter.on(PATH_SEPARATOR).splitToList(pathExtVar);
        }

        private static Stream<Path> splitPathVariable(String pathVar) {
            // Windows PATH handling seems to be aware of quote characters, even though it isn't documented anywhere.
            // Quotes never end up in the resulting variable, but the semicolon doesn't separate elements if it appears
            // inside quotes. A quote may be unclosed, it is implicitly closed at the end of the variable.
            var iterator = new AbstractIterator<String>() {
                private int pos;

                @Nullable
                @Override
                protected String computeNext() {
                    var result = new StringBuilder();
                    boolean inQuotes = false;
                    while (pos < pathVar.length()) {
                        var curCh = pathVar.charAt(pos++);

                        switch (curCh) {
                            case '"':
                                inQuotes = !inQuotes;
                                break;
                            case PATH_SEPARATOR:
                                if (!inQuotes) {
                                    return result.toString();
                                }
                                // After a quote a semicolon is just a character, not a path separator.
                                // fall through
                            default:
                                result.append(curCh);
                                break;
                        }
                    }
                    if (result.length() > 0) {
                        return result.toString();
                    }
                    return endOfData();
                }
            };

            //noinspection UnstableApiUsage
            return Streams.stream(iterator).map(WindowsPathResolver::safeResolve).flatMap(Streams::stream);
        }

        private static Optional<Path> safeResolve(String pathStr) {
            try {
                return Optional.of(Paths.get(pathStr));
            } catch (InvalidPathException ex) {
                // PATH variable can have unsupported characters in it. We've already handled quotes, but everything
                // else we just skip.
                return Optional.empty();
            }
        }

        public WindowsPathResolver() {
            this(Paths.get(System.getProperty("user.dir")), System.getenv("PATH"), System.getenv("PATHEXT"));
        }

        @VisibleForTesting
        WindowsPathResolver(File curDir, String pathVar, String pathExtVar) {
            this(curDir.toPath(), pathVar, pathExtVar);
        }

        @Override
        public Optional<File> resolveExecutablePath(String rawExecutable) {
            Path rawExecutablePath = Paths.get(rawExecutable).normalize();
            boolean hasExtension = !getFileExtension(rawExecutablePath.getFileName().toString()).isEmpty();
            if (!hasJustFilename(rawExecutablePath)) {
                if (hasExtension) {
                    return getExecutablePathIfExists(rawExecutablePath);
                }
                return resolveWithPathExt(Objects.requireNonNull(rawExecutablePath.getParent()),
                        rawExecutablePath.getFileName().toString());
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
    }

    private static Optional<File> getExecutablePathIfExists(Path potentialExecutablePath) {
        if (isExecutableValid(potentialExecutablePath)) {
            // We don't want to hit the disk there but dropping extra stuff and ensuring absolute path is nice to the
            // clients.
            return Optional.of(potentialExecutablePath.normalize().toAbsolutePath().toFile());
        }
        return Optional.empty();
    }

    private static boolean isExecutableValid(Path potentialExecutable) {
        return Files.isExecutable(potentialExecutable);
    }

    private static boolean hasJustFilename(Path rawExecutableFile) {
        return rawExecutableFile.getNameCount() == 1;
    }
}
