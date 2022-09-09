/*
 * Copyright 2022 Mikhail Lopatkin
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import name.mlopatkin.andlogview.thirdparty.systemutils.SystemUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

class PosixPathResolverTest {
    private static final String POSIX_PATH_SEPARATOR = ":";

    @TempDir
    File curDir;

    @TempDir
    File tempDir;

    private File oldCurDir;

    @BeforeAll
    static void checkPreconditions() {
        Assumptions.assumeFalse(SystemUtils.IS_OS_WINDOWS, "Contains POSIX-only PATH manipulations");
    }

    @BeforeEach
    void setUp() {
        oldCurDir = new File(System.getProperty("user.dir"));
        System.setProperty("user.dir", curDir.getAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", oldCurDir.getAbsolutePath());
    }

    @Test
    void resolvesExecutableByAbsolutePath() throws IOException {
        File adbFile = withExecutableFileIn(curDir, "adb");

        SystemPathResolver resolver = resolver(withPathElements(tempDir));

        assertEquals(Optional.of(adbFile), resolver.resolveExecutablePath(adbFile.getAbsolutePath()));
    }

    @Test
    void resolvesExecutableByRelativePath() throws IOException {
        File adbFile = withExecutableFileIn(curDir, "adb");

        SystemPathResolver resolver = resolver(withPathElements(tempDir));

        assertEquals(Optional.of(adbFile), resolver.resolveExecutablePath("./adb"));
    }

    @Test
    void doesntUsePathIfAbsolutePathIsSpecified() throws IOException {
        withExecutableFileIn(tempDir, "adb");

        SystemPathResolver resolver = resolver(withPathElements(tempDir));

        assertFalse(resolver.resolveExecutablePath(new File(curDir, "adb").getAbsolutePath()).isPresent());
    }

    @Test
    void doesntUsePathIfRelativePathIsSpecified() throws IOException {
        withExecutableFileIn(tempDir, "adb");

        SystemPathResolver resolver = resolver(withPathElements(tempDir));

        assertFalse(resolver.resolveExecutablePath("./adb").isPresent());
    }

    @Test
    void resolvesExecutableByItsNameIfInPath() throws IOException {
        File adbFile = withExecutableFileIn(tempDir, "adb");

        SystemPathResolver resolver = resolver(withPathElements(tempDir));

        assertEquals(Optional.of(adbFile), resolver.resolveExecutablePath("adb"));
    }


    @Test
    void resolvesNothingIfFileNotExecutable() throws IOException {
        File adbFile = withFileIn(tempDir, "adb");

        SystemPathResolver resolver = resolver(withPathElements(tempDir));

        assertFalse(resolver.resolveExecutablePath(adbFile.getAbsolutePath()).isPresent());
    }

    @Test
    void resolvesNothingIfCurdirIsNotInPath() throws IOException {
        withExecutableFileIn(curDir, "adb");

        SystemPathResolver resolver = resolver(withPathElements(tempDir));

        assertFalse(resolver.resolveExecutablePath("adb").isPresent());
    }

    @Test
    void emptyPrefixRepresentsCurDir() throws IOException {
        File adbFile = withExecutableFileIn(curDir, "adb");

        SystemPathResolver resolver = resolver(":" + withPathElements(tempDir));

        assertEquals(Optional.of(adbFile), resolver.resolveExecutablePath("adb"));
    }

    @Test
    void emptySuffixRepresentsCurDir() throws IOException {
        File adbFile = withExecutableFileIn(curDir, "adb");

        SystemPathResolver resolver = resolver(withPathElements(tempDir) + ":");

        assertEquals(Optional.of(adbFile), resolver.resolveExecutablePath("adb"));
    }

    @Test
    void emptyMiddleRepresentsCurDir() throws IOException {
        File adbFile = withExecutableFileIn(curDir, "adb");

        SystemPathResolver resolver = resolver(withPathElements(tempDir) + "::" + withPathElements(tempDir));

        assertEquals(Optional.of(adbFile), resolver.resolveExecutablePath("adb"));
    }

    private SystemPathResolver resolver(String path) {
        return new SystemPathResolver.PosixPathResolver(curDir.toPath(), path);
    }

    private static String withPathElements(File... pathElements) {
        return Arrays.stream(pathElements)
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(POSIX_PATH_SEPARATOR));
    }

    private static File withFileIn(File dir, String filename) throws IOException {
        File f = new File(dir, filename);
        if (!f.createNewFile()) {
            throw new AssertionError(
                    String.format("File %s already exists in the temp dir %s", filename, dir.getAbsolutePath()));
        }
        return f;
    }

    private static File withExecutableFileIn(File dir, String filename) throws IOException {
        File f = withFileIn(dir, filename);
        if (!f.setExecutable(true)) {
            throw new AssertionError(
                    String.format("File %s cannot be made executable", f.getAbsolutePath()));
        }
        return f;
    }
}
