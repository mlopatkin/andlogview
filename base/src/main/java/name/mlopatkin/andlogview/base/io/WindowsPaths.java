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

package name.mlopatkin.andlogview.base.io;

import org.apache.commons.lang3.SystemUtils;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A collection of well-known paths on Windows. This class only functions when running on Windows, as it relies on the
 * environment to resolve the paths.
 *
 * @see <a href="https://learn.microsoft.com/en-us/windows/win32/shell/knownfolderid">Windows documentation</a>
 */
public final class WindowsPaths {
    /**
     * Returns path to the roaming app data directory of the current user. On modern Windows it is located in
     * {@code C:\Users\<username>\AppData\Roaming}. This is where applications typically store their configuration
     * files.
     * <p>
     * Returns null if not running on Windows.
     *
     * @return the path to the directory, if available, or null
     */
    public static @Nullable Path getRoamingAppData() {
        return resolveKnownFolderInUserProfile("APPDATA", "AppData/Roaming/", "Application Data/");
    }

    /**
     * Returns path to the local app data directory of the current user. On modern Windows it is located in
     * {@code C:\Users\<username>\AppData\Local}. This is where applications typically store the files that do not
     * need to be replicated with the network profile.
     * <p>
     * Returns null if not running on Windows.
     *
     * @return the path to the directory, if available, or null
     */
    public static @Nullable Path getLocalAppData() {
        return resolveKnownFolderInUserProfile("LOCALAPPDATA", "AppData/Local/", "Local Settings/Application Data/");
    }

    /**
     * Returns path to the primary Program Files directory on the system drive. On 64-bit systems, it holds 64-bit
     * programs.
     * <p>
     * Returns null if not running on Windows.
     *
     * @return the path to the directory, if available, or null
     */
    public static @Nullable Path getProgramFiles() {
        return resolveKnownFolderOnSystemDrive("ProgramFiles", "Program Files/");
    }

    /**
     * Returns path to the 32-bit Program Files directory on the system drive. It is only available on 64-bit OS.
     * <p>
     * Returns null if not running on Windows.
     *
     * @return the path to the directory, if available, or null
     */
    public static @Nullable Path getProgramFilesX86() {
        return resolveKnownFolderOnSystemDrive("ProgramFiles(x86)", "Program Files (x86)/");
    }

    /**
     * Returns the system drive root.
     * <p>
     * Returns null if not running on Windows.
     *
     * @return the path to the directory, if available, or null
     */
    public static @Nullable Path getSystemDrive() {
        var systemDrivePath = System.getenv("SystemDrive");
        if (systemDrivePath != null) {
            return Paths.get(systemDrivePath + "/");
        }

        return null;
    }

    private static @Nullable Path resolveKnownFolderInUserProfile(
            String environmentVariable,
            String inUserProfilePath,
            String legacyInUserProfilePath
    ) {
        if (!SystemUtils.IS_OS_WINDOWS) {
            return null;
        }

        var environmentPath = System.getenv(environmentVariable);
        if (environmentPath != null) {
            return Paths.get(environmentPath);
        }

        if (SystemUtils.USER_HOME == null) {
            // We don't know where the user profile is.
            return null;
        }

        if (isAtLeastVista()) {
            return Paths.get(SystemUtils.USER_HOME, inUserProfilePath);
        }
        return Paths.get(SystemUtils.USER_HOME, legacyInUserProfilePath);
    }

    private static @Nullable Path resolveKnownFolderOnSystemDrive(
            String environmentVariable,
            String inSystemDrivePath
    ) {
        if (!SystemUtils.IS_OS_WINDOWS) {
            return null;
        }

        var environmentPath = System.getenv(environmentVariable);
        if (environmentPath != null) {
            return Paths.get(environmentPath);
        }

        var systemDrive = getSystemDrive();
        if (systemDrive != null) {
            return systemDrive.resolve(inSystemDrivePath);
        }

        return null;
    }

    private static boolean isAtLeastVista() {
        // Windows 2003 is a server variant of XP. Let's not dive into pre 2K, though it might be possible to run
        // Java 8 there.
        return !(SystemUtils.IS_OS_WINDOWS_2000 || SystemUtils.IS_OS_WINDOWS_XP || SystemUtils.IS_OS_WINDOWS_2003);
    }
}
