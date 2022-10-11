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
import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.PushParser;
import name.mlopatkin.andlogview.parsers.logcat.LogcatParseEventsHandler;
import name.mlopatkin.andlogview.parsers.ps.PsParseEventsHandler;

import java.util.Optional;

/**
 * A handler for the high-level dumpstate parse events. Provides means to parse logcat and process sections of the
 * dumpstate file.
 */
public interface DumpstateParseEventsHandler extends PushParser.ParseEventsHandler {
    /**
     * Called when a logcat section is detected for some buffer. The type of buffer is passed as an argument to this
     * method. Due to the way logcat is collected for the dumpstate, it is not possible to have separate {@code main}
     * and {@code system} buffers, as both reside in a single section. The {@code system} buffer is only available
     * since Android 3.0.
     * <p>
     * If the implementor wants to handle the logcat content, the handler should be returned in an {@link Optional}.
     * The {@link ParserControl} returned by the handler will only affect parsing of the section, so returning
     * {@link ParserControl#stop()} wouldn't affect parsing of the other sections of the dumpstate file, only the
     * current section would be skipped. The same logic applies to {@link LogcatParseEventsHandler#documentEnded()}, it
     * only means that the logcat section has ended and not necessary the whole dumpstate file.
     *
     * @param buffer the type of buffer
     * @return the handler to process logcat lines in this section or empty Optional to skip the section
     */
    default Optional<LogcatParseEventsHandler> logcatSectionBegin(LogRecord.Buffer buffer) {
        return Optional.empty();
    }

    /**
     * Called when a logcat section is detected but its content cannot be parsed.
     *
     * @return ParserControl instance to proceed with parsing or to stop parsing the whole dumpstate file
     */
    default ParserControl unparseableLogcatSection(LogRecord.Buffer buffer) {
        return ParserControl.proceed();
    }

    /**
     * Called when a process section is detected.
     * <p>
     * If the implementor wants to handle the ps content, the handler should be returned in an {@link Optional}.
     * The {@link ParserControl} returned by the handler will only affect parsing of the section, so returning
     * {@link ParserControl#stop()} wouldn't affect parsing of the other sections of the dumpstate file, only the
     * current section would be skipped.
     *
     * @return the handler to process ps lines in this section or empty Optional to skip the section
     */
    default Optional<PsParseEventsHandler> psSectionBegin() {
        return Optional.empty();
    }
}
