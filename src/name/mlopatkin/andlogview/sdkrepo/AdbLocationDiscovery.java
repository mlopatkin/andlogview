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

import name.mlopatkin.andlogview.base.collections.MyStreams;
import name.mlopatkin.andlogview.base.io.WindowsPaths;

import org.apache.commons.lang3.SystemUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Discovers ADB binary location in common Android SDK installation directories.
 * <p>
 * This class searches platform-specific locations where Android Studio and other tools typically install the Android
 * SDK, and attempts to locate the ADB executable.
 * </p>
 */
public class AdbLocationDiscovery {
    public static final String ADB_EXECUTABLE = (SystemUtils.IS_OS_WINDOWS ? "adb.exe" : "adb");

    private static final Logger log = LoggerFactory.getLogger(AdbLocationDiscovery.class);


    private static final String PLATFORM_TOOLS_DIR = SdkPackage.PLATFORM_TOOLS;

    private AdbLocationDiscovery() {}

    public static Stream<File> discoverAdbLocations() {
        return getCandidatePaths()
                .map(sdkRoot -> sdkRoot.resolve(PLATFORM_TOOLS_DIR).resolve(ADB_EXECUTABLE))
                .filter(path -> Files.isRegularFile(path) && Files.isExecutable(path))
                .map(Path::toFile);
    }

    private static Stream<Path> getCandidatePaths() {
        return Stream.concat(
                getEnvironmentPaths(),
                getCommonInstallationPaths()
        );
    }

    /**
     * Checks environment variables that Android Studio uses.
     */
    private static Stream<Path> getEnvironmentPaths() {
        return MyStreams.withoutNulls(
                Stream.of(
                        System.getenv("ANDROID_HOME"),
                        System.getenv("ANDROID_SDK_ROOT")
                ).map(AdbLocationDiscovery::parsePath)
        );
    }

    /**
     * Returns platform-specific installation paths.
     */
    private static Stream<Path> getCommonInstallationPaths() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return getWindowsPaths();
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            return getMacOsPaths();
        } else {
            return getLinuxPaths();
        }
    }

    private static Stream<Path> getWindowsPaths() {
        return MyStreams.withoutNulls(Stream.of(
                // Android Studio default location (most common)
                resolve(WindowsPaths.getLocalAppData(), "Android", "Sdk"),

                // Common manual installations, as suggested by LLMs
                resolve(WindowsPaths.getSystemDrive(), "Android", "Sdk"),
                resolve(WindowsPaths.getProgramFiles(), "Android", "android-sdk"),
                resolve(WindowsPaths.getProgramFilesX86(), "Android", "android-sdk")
        ));
    }

    private static Stream<Path> getMacOsPaths() {
        Path userHome = getUserHome();

        return MyStreams.withoutNulls(Stream.of(
                // Android Studio default location
                resolve(userHome, "Library", "Android", "sdk"),

                // Common manual installations, as suggested by LLMs
                Paths.get("/", "Applications", "Android Studio.app", "Contents", "sdk"),
                resolve(userHome, "android-sdk")
        ));
    }

    private static Stream<Path> getLinuxPaths() {
        return MyStreams.withoutNulls(Stream.of(
                // Android Studio default location (most common)
                resolve(getUserHome(), "Android", "Sdk"),

                // Common manual installations, as suggested by LLMs
                Paths.get("/", "opt", "android-sdk"),
                resolve(getUserHome(), ".android-sdk"),
                Paths.get("/", "usr", "local", "android-sdk")
        ));
    }

    private static @Nullable Path resolve(@Nullable Path base, String... elements) {
        if (base == null) {
            return null;
        }

        var result = base;
        for (String element : elements) {
            result = result.resolve(element);
        }
        return result;
    }

    private static @Nullable Path parsePath(@Nullable String path) {
        if (path == null) {
            return null;
        }
        try {
            return Paths.get(path);
        } catch (InvalidPathException e) {
            log.error("Failed to parse path '{}'", path, e);
            return null;
        }
    }

    private static @Nullable Path getUserHome() {
        return SystemUtils.USER_HOME != null ? parsePath(SystemUtils.USER_HOME) : null;
    }
}
