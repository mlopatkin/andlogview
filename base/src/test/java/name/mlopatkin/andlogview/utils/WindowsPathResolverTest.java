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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

class WindowsPathResolverTest {
    private static final String WINDOWS_PATH_SEPARATOR = ";";

    @TempDir
    File curDir;

    @TempDir
    File tempDir;

    private File oldCurDir;

    @BeforeAll
    static void checkPreconditions() {
        Assumptions.assumeTrue(SystemUtils.IS_OS_WINDOWS, "Contains windows-only PATH manipulations");
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
    void nothingIsFoundIfPathVarIsEmpty() {
        SystemPathResolver resolver = resolver(
                withPathElements()
        );
        assertFalse(resolver.resolveExecutablePath("adb.exe").isPresent());
    }

    @Test
    void nothingIsFoundIfPathDirIsEmpty() {
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir)
        );
        assertFalse(resolver.resolveExecutablePath("adb.exe").isPresent());
    }

    @Test
    void fileIsFoundByItsNameWithExtension() throws IOException {
        File adbFile = withFileIn(tempDir, "adb.exe");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir)
        );
        assertEquals(Optional.of(adbFile), resolver.resolveExecutablePath("adb.exe"));
    }

    @Test
    void fileIsFoundByItsNameWithoutExtension() throws IOException {
        File adbFile = withFileIn(tempDir, "adb.exe");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir),
                withPathExt(".exe")
        );
        assertEquals(Optional.of(adbFile), resolver.resolveExecutablePath("adb"));
    }

    @Test
    void pathExtsAreIgnoredIfExtensionIsGiven() throws IOException {
        withFileIn(tempDir, "adb.exe.bat");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir),
                withPathExt(".exe", ".bat")
        );
        assertEquals(Optional.empty(), resolver.resolveExecutablePath("adb.exe"));
    }

    @Test
    void fileIsFoundByItsNameWithoutExtensionIfMultipleElementsAreInPathExt() throws IOException {
        File adbFile = withFileIn(tempDir, "adb.bat");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir),
                withPathExt(".exe", ".bat")
        );
        assertEquals(Optional.of(adbFile), resolver.resolveExecutablePath("adb"));
    }

    @Test
    void fileWithoutExtensionIsIgnoredIfNotInPathExt() throws IOException {
        withFileIn(tempDir, "adb");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir),
                withPathExt(".exe", ".bat")
        );
        assertEquals(Optional.empty(), resolver.resolveExecutablePath("adb"));
    }

    @Test
    void fileWithoutExtensionIsUsedIfInPathExt() throws IOException {
        // This is a corner case. Windows allows empty extension in PATHEXT and tries to start a file without extension
        // then. However, it is useless because it isn't possible to set up a file association. Maybe some registry
        // hacking can allow to actually start the file.
        File adbPath = withFileIn(tempDir, "adb");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir),
                withPathExt("", ".exe", ".bat")
        );
        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath("adb"));
    }

    @Test
    void fileInCurrentDirIsPreferred() throws IOException {
        File adbPath = withFileIn(curDir, "adb.exe");
        withFileIn(tempDir, "adb.exe");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir)
        );
        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath("adb.exe"));
    }

    @Test
    void fileInCurrentDirIsPreferredWithoutExtension() throws IOException {
        File adbPath = withFileIn(curDir, "adb.exe");
        withFileIn(tempDir, "adb.exe");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir)
        );
        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath("adb"));
    }

    @Test
    void fileInCurrentDirIsPreferredIfExtensionIsLaterInPathExt() throws IOException {
        File adbPath = withFileIn(curDir, "adb.exe");
        withFileIn(tempDir, "adb.bat");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir),
                withPathExt(".bat", ".exe")
        );
        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath("adb"));
    }

    @Test
    void fileInCurrentDirIsPreferredIfCurrentDirIsLaterInPath() throws IOException {
        File adbPath = withFileIn(curDir, "adb.exe");
        withFileIn(tempDir, "adb.exe");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir, curDir)
        );
        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath("adb.exe"));
    }

    @Test
    void allPathElementsAreSearchedForFile(@TempDir File firstPathElement) throws IOException {
        File adbPath = withFileIn(curDir, "adb.exe");
        SystemPathResolver resolver = resolver(
                withPathElements(firstPathElement, tempDir)
        );
        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath("adb.exe"));
        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath("adb"));
    }

    @Test
    void firstPathElementWinsEvenIfLaterInPathextOrder(@TempDir File secondPathElement) throws IOException {
        File adbPath = withFileIn(curDir, "adb.exe");
        withFileIn(secondPathElement, "adb.bat");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir, secondPathElement),
                withPathExt(".bat", ".exe")
        );
        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath("adb.exe"));
        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath("adb"));
    }

    @Test
    void curdirThatCanBeNormalizedAwayFallsBackToPathLookup() throws IOException {
        File adbPath = withFileIn(tempDir, "adb.exe");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir)
        );
        // This is a quirky behavior of CMD. It normalizes the given path before deciding if the PATH lookup is
        // necessary, i.e. whether the naked filename is given.
        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath(".\\adb.exe"));
        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath(".\\.\\adb.exe"));
        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath(".\\1234\\..\\adb.exe"));
    }

    @Test
    void nonNormalizableCurdirReferencesDisablePathLookup() throws IOException {
        withFileIn(tempDir, "adb.exe");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir)
        );
        String curDirName = curDir.getName();
        assertEquals(Optional.empty(), resolver.resolveExecutablePath("..\\" + curDirName + "\\adb.exe"));
    }

    @Test
    void pathVarIsIgnoredIfAbsPathIsGiven() throws IOException {
        withFileIn(curDir, "adb.exe");
        SystemPathResolver resolver = resolver(
                withPathElements(curDir)
        );
        assertEquals(Optional.empty(),
                resolver.resolveExecutablePath(new File(tempDir, "adb.exe").getAbsolutePath()));
    }

    @Test
    void explicitPathIgnoresPathVarAndPathext() throws IOException {
        File adbPath = withFileIn(tempDir, "adb.exe");
        withFileIn(curDir, "adb.bat");
        SystemPathResolver resolver = resolver(
                withPathElements(curDir),
                withPathExt(".bat")
        );
        assertEquals(Optional.of(adbPath),
                resolver.resolveExecutablePath(new File(tempDir, "adb.exe").getAbsolutePath()));
    }

    @Test
    void curDirIsNotResolvedToFile() throws IOException {
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir)
        );
        assertEquals(Optional.empty(), resolver.resolveExecutablePath("."));
    }

    @Test
    void absCurDirIsNotResolvedToFile() throws IOException {
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir)
        );
        assertEquals(Optional.empty(), resolver.resolveExecutablePath(curDir.getAbsolutePath()));
    }

    @Test
    void explicitDirIsPreferredIfExtensionOmitted() throws IOException {
        File adbPath = withFileIn(tempDir, "adb.exe");
        withFileIn(curDir, "adb.exe");
        SystemPathResolver resolver = resolver(
                withPathElements(curDir)
        );
        assertEquals(Optional.of(adbPath),
                resolver.resolveExecutablePath(new File(tempDir, "adb").getAbsolutePath()));
    }

    @Test
    void explicitDirIsNotPreferredIfExtensionOmittedAndNotInPathext() throws IOException {
        withFileIn(tempDir, "adb.exe");
        SystemPathResolver resolver = resolver(
                withPathElements(curDir),
                withPathExt(".bat")
        );
        assertEquals(Optional.empty(),
                resolver.resolveExecutablePath(new File(tempDir, "adb").getAbsolutePath()));
    }

    @Test
    void dotIsResolvedToCurdir() throws IOException {
        File adbPath = withFileIn(curDir, "adb.exe");
        SystemPathResolver resolver = resolver(
                withPathElements(tempDir)
        );
        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath(".\\adb"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "\"%s",
            "%s\"",
            "\"\"%s",
            "\"%s\"",
    })
    void canHandleQuotesInPathElements(String pattern) throws IOException {
        var adbPath = withFileIn(tempDir, "adb.exe");
        var resolver = resolver(
                String.format(pattern, tempDir.getAbsolutePath())
        );

        assertEquals(Optional.of(adbPath), resolver.resolveExecutablePath("adb"));
    }

    @Test
    void semicolonsInQuotedElementArePartOfPath() throws IOException {
        withFileIn(tempDir, "adb.exe");
        var resolver = resolver(
                String.format("\"%1$s;%1$s\"", tempDir.getAbsolutePath())
        );

        assertEquals(Optional.empty(), resolver.resolveExecutablePath("adb"));
    }

    private SystemPathResolver resolver(String path) {
        return resolver(path, ".exe");
    }

    private SystemPathResolver resolver(String path, String pathExt) {
        return new SystemPathResolver.WindowsPathResolver(curDir, path, pathExt);
    }

    private static String withPathElements(File... pathElements) {
        return Arrays.stream(pathElements)
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(WINDOWS_PATH_SEPARATOR));
    }

    private static String withPathExt(String... pathExts) {
        return String.join(WINDOWS_PATH_SEPARATOR, pathExts);
    }

    private static File withFileIn(File dir, String filename) throws IOException {
        File f = new File(dir, filename);
        if (!f.createNewFile()) {
            throw new AssertionError(
                    String.format("File %s already exists in the temp dir %s", filename, dir.getAbsolutePath()));
        }
        return f;
    }
}
