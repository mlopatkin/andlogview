/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.parsers.dumpstate;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities to match various dumpstate file elements - section names, etc.
 */
final class DumpstateElements {
    private static final Pattern SECTION_NAME_PATTERN = Pattern.compile("^------ (.*) ------\\s*$");
    // TODO(mlopatkin): this pattern no longer matches the modern dumpstate's output
    private static final Pattern SECTION_END_PATTERN = Pattern.compile("^\\[.*: .* elapsed]$");
    private static final String DUMPSTATE_HEADER_BORDER = "========================================================";
    private static final String DUMPSTATE_HEADER_TITLE_PREFIX = "== dumpstate";

    // Logcat section naming is a bit inconsistent.
    private static final ImmutableMap<String, LogRecord.Buffer> LOGCAT_SECTION_TO_BUFFER = ImmutableMap.of(
            "MAIN", LogRecord.Buffer.MAIN,  // haven't seen this in the wild
            "SYSTEM", LogRecord.Buffer.MAIN,  // SYSTEM LOG is actually the default buffer (main + system)
            "EVENTS", LogRecord.Buffer.EVENTS,  // haven't seen this in the wild
            "EVENT", LogRecord.Buffer.EVENTS,
            "RADIO", LogRecord.Buffer.RADIO
    );

    private DumpstateElements() {}

    // Dumpstate header is three-line. Sometimes there is a trash output in the beginning of the first line.
    // Example:
    // ========================================================
    // == dumpstate: 2022-10-02 21:17:38
    // ========================================================
    // From the git history we can deduce that the timestamp was introduced later, it wasn't present in the first
    // release.

    /**
     * Checks if the line is a "border" of the dumpstate header (maybe with a prefix).
     *
     * @param line the line to check
     * @return {@code true} if the line is a border, {@code false} otherwise
     */
    public static boolean isDumpstateHeaderBorder(CharSequence line) {
        return CharMatcher.whitespace().trimFrom(line).endsWith(DUMPSTATE_HEADER_BORDER);
    }

    /**
     * Checks if the line is a title ({@code == dumpstate: ...}) of the dumpstate header.
     *
     * @param line the line to check
     * @return {@code true} if the line is a title, {@code false} otherwise
     */
    public static boolean isDumpstateHeaderTitle(CharSequence line) {
        return line.toString().startsWith(DUMPSTATE_HEADER_TITLE_PREFIX);
    }

    /**
     * Extracts the name of the dumpstate file section if possible.
     *
     * @param line the line to parse
     * @return the name of the section or {@code null} if line is not a section header
     */
    public static @Nullable String tryGetSectionName(CharSequence line) {
        Matcher m = SECTION_NAME_PATTERN.matcher(line);
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * Checks if the line is the end of the section. The actual format of such a line differs between various versions
     * of Android. Typically, this line summarizes how long it took to collect the section data.
     *
     * @param line the line to parse
     * @return {@code true} if the line is recognizable section end, {@code false} otherwise
     */
    public static boolean isSectionEnd(CharSequence line) {
        return SECTION_END_PATTERN.matcher(line).matches();
    }

    /**
     * Checks if the section is a process list section.
     *
     * @param sectionName the name of the section
     * @return {@code true} if the section is a process list section
     */
    public static boolean isProcessSection(String sectionName) {
        return "PROCESSES (ps -P)".equals(sectionName);
    }

    /**
     * Checks if the section is a logcat section.
     *
     * @param sectionName the name of the section
     * @return {@code true} if the section is a logcat section
     */
    public static boolean isLogcatSection(String sectionName) {
        return LOGCAT_SECTION_TO_BUFFER.keySet().stream().anyMatch(
                sectionNamePart -> isLogcatSectionName(sectionName, sectionNamePart));
    }

    /**
     * Tries to find the appropriate logcat buffer based on the section name. Returns empty optional if the section name
     * is not a logcat section or the buffer cannot be determined.
     *
     * @param sectionName the section name
     * @return buffer if the section name can be recognized as a logcat section name, or empty Optional
     */
    public static Optional<LogRecord.Buffer> getBufferFromLogcatSectionName(String sectionName) {
        return LOGCAT_SECTION_TO_BUFFER.entrySet()
                .stream()
                .filter(sectionToBuffer -> isLogcatSectionName(sectionName, sectionToBuffer.getKey()))
                .map(Map.Entry::getValue)
                .findAny();
    }

    private static boolean isLogcatSectionName(String sectionName, String bufferSectionNamePart) {
        return sectionName.startsWith(bufferSectionNamePart + " LOG");
    }
}
