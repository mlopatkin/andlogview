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

import static org.assertj.core.api.Assertions.assertThat;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DumpstateElementsTest {

    @Test
    void headerBorderIsRecognized() {
        assertThat(DumpstateElements.isDumpstateHeaderBorder(
                "========================================================")).isTrue();
    }

    @Test
    void headerBorderIsRecognizedWithPrefix() {
        assertThat(DumpstateElements.isDumpstateHeaderBorder(
                "GarBaGe ========================================================")).isTrue();
    }

    @Test
    void headerBorderIsRecognizedWithTrailingSpace() {
        assertThat(DumpstateElements.isDumpstateHeaderBorder(
                "========================================================   ")).isTrue();
    }

    @Test
    void shorterBorderIsNotRecognized() {
        assertThat(DumpstateElements.isDumpstateHeaderBorder(
                "=======================================================")).isFalse();
    }

    @Test
    void oldStyleDumpstateTitleIsRecognized() {
        assertThat(DumpstateElements.isDumpstateHeaderTitle("== dumpstate")).isTrue();
    }

    @Test
    void oldStyleDumpstateTitleIsRecognizedWithTrailingSpace() {
        assertThat(DumpstateElements.isDumpstateHeaderTitle("== dumpstate    ")).isTrue();
    }

    @Test
    void dumpstateTitleWithTimestampIsRecognized() {
        assertThat(DumpstateElements.isDumpstateHeaderTitle("== dumpstate: 2022-10-03 21:10:47")).isTrue();
    }

    @Test
    void dumpstateTitleWithTimestampIsRecognizedWithTrailingSpace() {
        assertThat(DumpstateElements.isDumpstateHeaderTitle("== dumpstate: 2022-10-03 21:10:47    ")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "=== dumpstate",
            "== dumpstat",
            "-- dumpstate",
    })
    void invalidDumpstateHeaderIsNotRecognized(String headerTitle) {
        assertThat(DumpstateElements.isDumpstateHeaderTitle(headerTitle)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "------ PROCRANK (procrank) ------,PROCRANK (procrank)",
            "------ PROCRANK ------,PROCRANK",
            "------ PROCRANK ------         ,PROCRANK",
            "------ CPU INFO (top -n 1 -d 1 -m 30 -t) ------,CPU INFO (top -n 1 -d 1 -m 30 -t)",
            "------ CPU INFO ------,CPU INFO",
            "------ VM TRACES JUST NOW (/data/anr/traces.txt.bugreport: 2022-10-02 21:17:37) ------,VM TRACES JUST "
                    + "NOW (/data/anr/traces.txt.bugreport: 2022-10-02 21:17:37)"
    })
    void canExtractSectionName(String sectionName, String expectedSectionName) {
        assertThat(DumpstateElements.tryGetSectionName(sectionName)).isEqualTo(expectedSectionName);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "--------- beginning of /dev/log/main",
            "-------------------------------------------------------------------------------",
            "*** /proc/slabinfo: Permission denied",
            "----- pid 1302 at 2022-10-02 21:17:37 -----",
            "----- end 1235 -----"
    })
    void cannotExtractSectionNameFromInvalidString(String notSectionName) {
        assertThat(DumpstateElements.tryGetSectionName(notSectionName)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "[top: 1.1s elapsed]"
    })
    void canDetermineSectionEnd(String line) {
        assertThat(DumpstateElements.isSectionEnd(line)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "----- end 1235 -----",
            "*** /proc/slabinfo: Permission denied",
            "--------- beginning of /dev/log/main",
            "------ PROCRANK (procrank) ------",
            "------ PROCRANK ------",
    })
    void canDetermineNotSectionEnd(String line) {
        assertThat(DumpstateElements.isSectionEnd(line)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "PROCESSES (ps -P)",
            "PROCESSES",
            "PROCESSES (ps -P --abi)",
            // TODO(mlopatkin) This need more supported cases
    })
    void canDetermineProcessSection(String sectionName) {
        assertThat(DumpstateElements.isProcessSection(sectionName)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "PROCRANK",
            "PROCESSES AND THREADS (ps -t -p -P)",
            "PROCESSES AND THREADS",
    })
    void canDetermineNotProcessSection(String sectionName) {
        assertThat(DumpstateElements.isProcessSection(sectionName)).isFalse();
    }


    @ParameterizedTest
    @CsvSource(value = {
        "SYSTEM LOG (logcat -v time -d *:v),MAIN",
        "EVENT LOG (logcat -b events -v time -d *:v),EVENTS",
        "RADIO LOG (logcat -b radio -v time -d *:v),RADIO"
    })
    void canDetermineLogcatSection(String sectionName, String expectedBufferName) {
        LogRecord.Buffer expectedBuffer = LogRecord.Buffer.valueOf(expectedBufferName);

        assertThat(DumpstateElements.isLogcatSection(sectionName)).isTrue();
        assertThat(DumpstateElements.getBufferFromLogcatSectionName(sectionName)).hasValue(expectedBuffer);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "PROCRANK",
            "PROCESSES AND THREADS (ps -t -p -P)",
            "PROCESSES AND THREADS",
            "PROCESSES",
            "KERNEL LOG (dmesg)",
            "KERNEL LOG",
            "EVENT LOG TAGS",
            "BINDER FAILED TRANSACTION LOG (/sys/kernel/debug/binder/failed_transaction_log)",
            "LAST RADIO LOG (parse_radio_log /proc/last_radio_log)"  // TODO(mlopatkin) Can we parse it?
    })
    void canDetermineNotLogcatSection(String sectionName) {
        assertThat(DumpstateElements.isLogcatSection(sectionName)).isFalse();
        assertThat(DumpstateElements.getBufferFromLogcatSectionName(sectionName)).isEmpty();
    }
}
